package com.app.dixon.facorites.core.data.service

import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.io.toEntry
import com.app.dixon.facorites.core.data.bean.io.toJson
import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.util.Ln
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 全路径：com.app.dixon.facorites.core.data.save
 * 类描述：数据存取服务
 * 创建人：xuzheng
 * 创建时间：3/17/22 8:09 PM
 *
 * 目录 -> 分类 -> 条目
 *
 * 数据结构
 * - mark 文件夹
 * --- category_info  目录 一个File 里边是个完整的Json
 * --- id_000000000   分类 一个File 里边每行记录一条Json，即一行对应一个条目
 * --- id_000000001
 * --- id_000000002
 */
object DataService : IService {

    private const val ROOT_PATH = "mark" // 根Dir
    private const val CATEGORY_INFO_PATH = "category_info"

    // 单线程写入 外部只读 不需要加锁
    // ？一个线程读，一个线程写（删除），读的线程越界异常？
    private val categoryList = ArrayList<CategoryInfoBean>()
    private val entryMap = HashMap<Long, MutableList<BaseEntryBean>>()

    // 回调 由UI线程进行注册、回调、删除等
    private val categoryCallbacks = ArrayList<WeakReference<ICategoryChanged>>()
    private val entryCallbacks = ArrayList<WeakReference<IEntryChanged>>()
    private val globalEntryCallbacks = ArrayList<WeakReference<IGlobalEntryChanged>>()

    private val ioService: WorkService = WorkService()

    /**
     * 初始化ArrArrAsdsa
     */
    override fun runService() {
        ioService.runService()
        ioService.postEvent {
            // 0.检测是不是首次启动 首次启动初始化
            if (!FileUtils.exists("$ROOT_PATH/$CATEGORY_INFO_PATH")) {
                if (FileUtils.createDir(ROOT_PATH)) {
                    FileUtils.createNewFile("$ROOT_PATH/$CATEGORY_INFO_PATH")
                }
                // 初次初始化，创建默认收藏夹
                doCreateCategory("默认收藏夹")
            }
            // 1.获取所有分类 并添加到内存
            val categoryInfoJson = FileUtils.readString("$ROOT_PATH/$CATEGORY_INFO_PATH")
            categoryInfoJson.ifEmpty {
                // 说明没有分类
                return@postEvent
            }
            Ln.i("DataService", "init category info: $categoryInfoJson")
            (Gson().fromJson(
                categoryInfoJson,
                object : TypeToken<List<CategoryInfoBean>>() {}.type
            ) as List<CategoryInfoBean>).forEach {
                addCategoryData(it)
            }
            // 2.获取所有分类的所有条目 并添加到内存
            categoryList.forEach { category ->
                val json = FileUtils.readStringByLine("$ROOT_PATH/${category.id}")
                json.forEach { entryJson ->
                    val bean = entryJson.toEntry()
                    bean?.let { mark ->
                        entryMap[category.id]?.add(mark)
                    }
                }
                Ln.i(
                    "DataService",
                    "init category entry: ${category.name} ${entryMap[category.id]}"
                )
            }
        }
    }

    private fun addCategoryData(categoryInfoBean: CategoryInfoBean) {
        categoryList.add(categoryInfoBean)
        entryMap[categoryInfoBean.id] = mutableListOf()
    }

    /**
     * 创建新分类
     */
    fun createCategory(name: String, callback: ((Long) -> Unit)? = null) {
        ioService.postEvent {
            doCreateCategory(name, callback)
        }
    }

    private fun doCreateCategory(name: String, callback: ((Long) -> Unit)? = null) {
        // 创建文件夹
        val dirId = Date().time
        if (!FileUtils.createNewFile("$ROOT_PATH/$dirId")) {
            callback?.backUi { invoke(-1L) }
            return
        }
        // 使用临时数据更新文件
        val saveList = categoryList.toMutableList() // 创建副本
        val categoryBean = CategoryInfoBean(dirId, name)
        saveList.add(categoryBean)
        val isSuccess =
            FileUtils.saveString(
                "$ROOT_PATH/$CATEGORY_INFO_PATH",
                Gson().toJson(saveList)
            )
        if (isSuccess) {
            // 文件写入成功后，才能更新内存数据
            addCategoryData(categoryBean)
            callback?.backUi { invoke(dirId) }
            // 回调注册
            backUi {
                callbackRegister(categoryCallbacks) {
                    it.onDataCreated(categoryBean)
                }
            }
        } else {
            callback?.backUi { invoke(-1L) }
        }
        Ln.i("DataService") { "创建分类后的分类信息：${FileUtils.readString("$ROOT_PATH/$CATEGORY_INFO_PATH")}" }
    }

    /**
     * 创建新条目
     *
     * @param id 分类id
     */
    fun createEntry(id: Long, bean: BaseEntryBean, callback: Callback<BaseEntryBean>? = null) {
        ioService.postEvent {
            doCreateEntry(id, bean, callback)
        }
    }

    /**
     * 删除条目
     */
    fun deleteEntry(categoryId: Long, bean: BaseEntryBean, callback: Callback<BaseEntryBean>? = null) {
        ioService.postEvent {
            doDeleteEntry(categoryId, bean, callback)
        }
    }

    private fun doDeleteEntry(categoryId: Long, bean: BaseEntryBean, callback: Callback<BaseEntryBean>?) {
        val filter = categoryList.filter { it.id == categoryId }
        if (filter.isNotEmpty()) {
            // 移除本地数据
            val jsonList = FileUtils.readStringByLine("$ROOT_PATH/$categoryId").toMutableList()
            val iterator = jsonList.iterator()
            var exit = false
            while (iterator.hasNext() && !exit) {
                val next = iterator.next()
                next.toEntry()?.let { entry ->
                    if (bean == entry) {
                        iterator.remove()
                        exit = true
                    }
                }
            }
            if (!exit) {
                callback?.backUi { onFail("查找条目失败") }
                return
            }
            var jsonString = ""
            jsonList.forEach { jsonString += "$it\n" }
            val success = FileUtils.saveString("$ROOT_PATH/$categoryId", jsonString)
            if (success) {
                // 移除内存数据
                entryMap[categoryId]?.remove(bean)
                // 回调注册
                backUi {
                    callback?.onSuccess(bean)
                    callbackRegister(entryCallbacks) {
                        it.onDataDeleted(bean)
                    }
                    callbackRegister(globalEntryCallbacks) {
                        it.onDataDeleted(bean)
                    }
                }
            } else {
                callback?.backUi { onFail("文件写入失败") }
            }
        } else {
            callback?.backUi { onFail("查找分类失败") }
        }
    }


    /**
     * 更新条目
     */
    fun updateEntry() {

    }

    private fun doCreateEntry(
        id: Long,
        bean: BaseEntryBean,
        callback: Callback<BaseEntryBean>? = null
    ) {
        val list = entryMap[id]
        // 存在指定ID的分类才能添加条目
        list?.let {
            val result = bean.toJson() + "\n"
            Ln.i("DataService", "保存条目：$result 当前线程 ${Thread.currentThread()}")
            val isSuccess = FileUtils.appendString("$ROOT_PATH/$id", result)
            if (isSuccess) {
                // 文件写入成功后，才能更新内存数据
                it.add(bean)
                callback?.backUi { onSuccess(bean) }
                backUi {
                    // 回调对应ID的EntryCreate事件
                    callbackRegister(entryCallbacks) { register ->
                        if (register.id == id) {
                            register.onDataCreated(bean)
                        }
                    }
                    // 回调全局EntryCreate事件
                    callbackRegister(globalEntryCallbacks) { register ->
                        register.onDataCreated(bean)
                    }
                }
            } else {
                callback?.backUi { onFail("写入条目失败") }
            }
        } ?: callback?.backUi { onFail("查找分类失败") }

        Ln.i("DataService") { "创建条目后的条目数据：${FileUtils.readString("$ROOT_PATH/$id")}" }
    }

    /*
     * 对于Kotlin，返回的虽然是List，但是其真实类型是MutableList。
     * 对内，数据可以添加，返回的List也能正常同步到；
     * 对外，由于是List，所以不能被随意增删。
     * 这正是想要的效果：只返回数据集，并能同步更新，但不允许外部擅自修改。
     */

    /**
     * 获取所有分类信息
     *
     */
    fun getCategoryList(): List<CategoryInfoBean> = categoryList

    /**
     * 获取某分类下的条目列表
     */
    fun getEntryList(id: Long): List<BaseEntryBean>? = entryMap[id]

    /**
     * 获取默认分类
     */
    fun getDefaultCategory(): CategoryInfoBean? {
        if (categoryList.isEmpty()) {
            return null
        }
        return categoryList[0]
    }

    /**
     * 注册分类变化的监听
     */
    fun register(categoryChanged: ICategoryChanged) {
        categoryCallbacks.add(WeakReference(categoryChanged))
    }

    /**
     * 注册某分类下条目变化的监听
     */
    fun register(entryChanged: IEntryChanged) {
        entryCallbacks.add(WeakReference(entryChanged))
    }

    /**
     * 注册全局条目变化监听
     */
    fun register(globalEntryChanged: IGlobalEntryChanged) {
        globalEntryCallbacks.add(WeakReference(globalEntryChanged))
    }

    private fun <T> callbackRegister(list: ArrayList<WeakReference<T>>, action: (T) -> Unit) {
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val register = iterator.next().get()
            register?.let {
                action.invoke(it)
            } ?: iterator.remove()
        }
    }

    interface IDataChanged<T> {

        fun onDataCreated(bean: T)

        fun onDataDeleted(bean: T)

        fun onDataUpdated(bean: T)
    }

    // 分类变化时的回调
    interface ICategoryChanged : IDataChanged<CategoryInfoBean>

    // 分类下某一Entry变化时的回调
    abstract class IEntryChanged(val id: Long) : IDataChanged<BaseEntryBean>

    // 全局任一Entry变化时的回调
    interface IGlobalEntryChanged : IDataChanged<BaseEntryBean>
}
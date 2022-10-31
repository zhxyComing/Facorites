package com.app.dixon.facorites.core.data.service

import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.data.bean.*
import com.app.dixon.facorites.core.data.bean.io.toEntry
import com.app.dixon.facorites.core.data.bean.io.toJson
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.ex.callbackRegister
import com.app.dixon.facorites.core.ex.findByCondition
import com.app.dixon.facorites.core.ex.removeByCondition
import com.app.dixon.facorites.core.util.Ln
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference
import java.util.*

/**
 * 全路径：com.app.dixon.facorites.core.data.save
 * 类描述：数据存取服务
 * 创建人：xuzheng
 * 创建时间：3/17/22 8:09 PM
 *
 * 需要优先启动的服务
 *
 * (data/data/xxx/file/mark)
 *
 * 目录 -> 分类 -> 条目
 *
 * 数据结构
 * - mark 文件夹
 * --- category_info  目录 一个File 里边是个完整的Json
 * --- id_000000000   分类 一个File 里边每行记录一条Json，即一行对应一个条目
 * --- id_000000001
 * --- id_000000002
 *
 * 对于真实的SD卡存储，其实没有层级结构
 * 层级结构是代码里定义的
 */
object DataService : IService {

    private const val ROOT_PATH = "mark" // 根Dir
    private const val CATEGORY_INFO_PATH = "category_info"

    // 单线程写入 外部只读 不需要加锁
    // ？一个线程读，一个线程写（删除），读的线程越界异常？
    private val categoryList = ArrayList<CategoryInfoBean>()  // 所有分类的列表
    private val entryMap = HashMap<Long, MutableList<BaseEntryBean>>() // 所有分类对应的收藏

    // 回调 由UI线程进行注册、回调、删除等
    private val categoryCallbacks = ArrayList<WeakReference<ICategoryChanged>>()
    private val entryCallbacks = ArrayList<WeakReference<IEntryChanged>>()
    private val globalEntryCallbacks = ArrayList<WeakReference<IGlobalEntryChanged>>()

    private val ioService: WorkService = WorkService()

    /**
     * 初始化
     */
    override fun runService() {
        ioService.runService()
        ioService.postEvent {
            Ln.i("InitMonitor", "111")
            val initStartTime = System.currentTimeMillis()
            // 0.检测是不是首次启动 首次启动初始化
            if (!FileUtils.exists("$ROOT_PATH/$CATEGORY_INFO_PATH")) {
                if (FileUtils.createDir(ROOT_PATH)) {
                    FileUtils.createNewFile("$ROOT_PATH/$CATEGORY_INFO_PATH")
                }
                // 初次初始化，创建默认收藏夹
                doCreateCategory("默认收藏夹")
                backUi {
                    initCallback?.let {
                        it.forEach { action ->
                            action.invoke()
                        }
                        it.clear()
                        initCallback = null
                    }
                }
                return@postEvent
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
            Ln.i("DataService", "init cost time ${System.currentTimeMillis() - initStartTime}")
            // 初始化完成，清除回调，用不着了
            Ln.i("InitMonitor", "init over start callback")
            backUi {
                initCallback?.let {
                    it.forEach { action ->
                        action.invoke()
                    }
                    it.clear()
                    initCallback = null
                }
                Ln.i("InitMonitor", "init callback over")
            }
        }
    }

    private var initCallback: MutableList<() -> Unit>? = mutableListOf()

    /**
     * 添加初始化回调
     *
     * 进入页面初始化没完成：添加回调，初始化完成后回调
     * 进入页面初始化已结束：callback 被置空，无法添加回调，此时直接回调即可
     */
    fun addInitCompleteListener(initComplete: () -> Unit) = initCallback?.add(initComplete) ?: initComplete.invoke()

    private fun addCategoryData(categoryInfoBean: CategoryInfoBean) {
        categoryList.add(categoryInfoBean)
        entryMap[categoryInfoBean.id] = mutableListOf()
    }

    /**
     * 创建新分类
     */
    fun createCategory(name: String, bgPath: String? = null, callback: ((Long) -> Unit)? = null) {
        ioService.postEvent {
            doCreateCategory(name, bgPath, callback)
        }
    }

    private fun doCreateCategory(name: String, bgPath: String? = null, callback: ((Long) -> Unit)? = null) {
        // 创建文件夹
        val dirId = Date().time
        if (!FileUtils.createNewFile("$ROOT_PATH/$dirId")) {
            callback?.backUi { invoke(-1L) }
            return
        }
        // 使用临时数据更新文件
        val saveList = categoryList.toMutableList() // 创建副本
        val categoryBean = CategoryInfoBean(dirId, name, bgPath)
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
        Ln.safeI("DataService") { "创建分类后的分类信息：${FileUtils.readString("$ROOT_PATH/$CATEGORY_INFO_PATH")}" }
    }

    // 创建子文件用 没有回调全局
    private fun doCreateChildCategory(categoryInfoBean: CategoryInfoBean, callback: ((Long) -> Unit)? = null) {
        // 创建文件夹
        if (!FileUtils.createNewFile("$ROOT_PATH/${categoryInfoBean.id}")) {
            callback?.backUi { invoke(-1L) }
            return
        }
        // 使用临时数据更新文件
        val saveList = categoryList.toMutableList() // 创建副本
        saveList.add(categoryInfoBean)
        val isSuccess =
            FileUtils.saveString(
                "$ROOT_PATH/$CATEGORY_INFO_PATH",
                Gson().toJson(saveList)
            )
        if (isSuccess) {
            // 文件写入成功后，才能更新内存数据
            addCategoryData(categoryInfoBean)
            callback?.backUi { invoke(categoryInfoBean.id) }
        } else {
            callback?.backUi { invoke(-1L) }
        }
        Ln.safeI("DataService") { "创建分类后的分类信息：${FileUtils.readString("$ROOT_PATH/$CATEGORY_INFO_PATH")}" }
    }

    /**
     * 删除分类
     */
    fun deleteCategory(categoryId: Long, callback: ((Long) -> Unit)? = null) {
        ioService.postEvent {
            doDeleteCategory(categoryId, callback)
        }
    }

    private fun doDeleteCategory(categoryId: Long, callback: ((Long) -> Unit)?) {
        // 1.删除本地的分类信息、分类文件
        val deleteCategoryInfo = categoryList.findByCondition { it.id == categoryId } ?: let {
            Ln.e("DeleteCategory", "未找到分类")
            callback?.backUi { invoke(-1L) }
            return
        }
        val saveList = categoryList.toMutableList() // 创建副本
        val hasRemove = saveList.removeByCondition {
            it.id == categoryId
        }
        if (hasRemove) {
            if (FileUtils.saveString("$ROOT_PATH/$CATEGORY_INFO_PATH", Gson().toJson(saveList))
                && FileUtils.deleteFile("$ROOT_PATH/$categoryId")
            ) {
                // 文件删除成功
                // 2.删除内存的分类信息、分类文件
                categoryList.removeByCondition { it.id == categoryId }
                val deleteEntries = entryMap.remove(categoryId)
                // 删除子文件夹
                deleteEntries?.forEach {
                    (it as? CategoryEntryBean)?.let { categoryEntryBean ->
                        doDeleteChildCategory(categoryEntryBean.categoryInfoBean.id)
                    }
                }
                // 3.回调
                backUi {
                    callback?.invoke(categoryId)
                    callbackRegister(categoryCallbacks) {
                        it.onDataDeleted(deleteCategoryInfo)
                    }
                    callbackRegister(globalEntryCallbacks) {
                        it.onDataRefresh()
                    }
                }
                // 删除图片 包括分类自身的 和条目的
                deleteCategoryInfo.bgPath?.let {
                    FileIOService.deleteFile(it)
                }
                deleteEntries?.forEach {
                    (it as? ImageEntryBean)?.let { _ ->
                        FileIOService.deleteFile(it.path)
                    }
                    (it as? GalleryEntryBean)?.path?.forEach { path ->
                        FileIOService.deleteFile(path)
                    }
                }
            } else {
                Ln.e("DeleteCategory", "分类文件删除失败")
                // 未找到该分类
                callback?.backUi { invoke(-1L) }
            }
        } else {
            Ln.e("DeleteCategory", "未找到分类")
            // 未找到该分类
            callback?.backUi { invoke(-1L) }
        }

    }

    private fun doDeleteChildCategory(categoryId: Long, callback: ((Long) -> Unit)? = null) {
        // 1.删除本地的分类信息、分类文件
        val deleteCategoryInfo = categoryList.findByCondition { it.id == categoryId } ?: let {
            Ln.e("DeleteCategory", "未找到分类")
            callback?.backUi { invoke(-1L) }
            return
        }
        val saveList = categoryList.toMutableList() // 创建副本
        val hasRemove = saveList.removeByCondition {
            it.id == categoryId
        }
        if (hasRemove) {
            if (FileUtils.saveString("$ROOT_PATH/$CATEGORY_INFO_PATH", Gson().toJson(saveList))
                && FileUtils.deleteFile("$ROOT_PATH/$categoryId")
            ) {
                // 文件删除成功
                // 2.删除内存的分类信息、分类文件
                categoryList.removeByCondition { it.id == categoryId }
                val deleteEntries = entryMap.remove(categoryId)
                // 循环删除子文件夹
                deleteEntries?.forEach {
                    // 子文件夹下的所有文件/文件夹已经全部删除
                    (it as? CategoryEntryBean)?.let { categoryEntryBean ->
                        doDeleteChildCategory(categoryEntryBean.categoryInfoBean.id)
                    }
                }
                // 删除图片 包括分类自身的 和条目的
                deleteCategoryInfo.bgPath?.let {
                    FileIOService.deleteFile(it)
                }
                deleteEntries?.forEach {
                    (it as? ImageEntryBean)?.let { _ ->
                        FileIOService.deleteFile(it.path)
                    }
                    (it as? GalleryEntryBean)?.path?.forEach { path ->
                        FileIOService.deleteFile(path)
                    }
                }
                // 3.回调
                callback?.invoke(categoryId)
            } else {
                Ln.e("DeleteCategory", "分类文件删除失败")
                // 未找到该分类
                callback?.backUi { invoke(-1L) }
            }
        } else {
            Ln.e("DeleteCategory", "未找到分类")
            // 未找到该分类
            callback?.backUi { invoke(-1L) }
        }

    }

    /**
     * 修改分类
     */
    fun updateCategory(originBean: CategoryInfoBean, newBean: CategoryInfoBean, callback: ((Long) -> Unit)? = null) {
        ioService.postEvent {
            doUpdateCategory(originBean, newBean, callback)
        }
    }

    private fun doUpdateCategory(originBean: CategoryInfoBean, newBean: CategoryInfoBean, callback: ((Long) -> Unit)?) {
        // 更新category_info存储的数据即可
        // 1.内存里找不到该分类，return
        val deleteBean = categoryList.findByCondition { it.id == originBean.id } ?: let {
            Ln.e("UpdateCategory", "未找到分类")
            callback?.backUi { invoke(-1L) }
            return
        }
        // 2.正确做法是先创建副本，修改值，保存到本地成功后，才能更新内存数据
        // 这里取巧：先修改内存，再保存本地，如果本地保存失败了，再把内存数据改回去
        val index = categoryList.indexOf(deleteBean)
        if (index != -1) {
            categoryList[index] = newBean
        }
        if (FileUtils.saveString("$ROOT_PATH/$CATEGORY_INFO_PATH", Gson().toJson(categoryList))) {
            // 3.文件更新成功 回调
            backUi {
                callback?.invoke(originBean.id)
                callbackRegister(categoryCallbacks) {
                    it.onDataUpdated(newBean)
                }
            }
        } else {
            categoryList[index] = originBean
            Ln.e("UpdateCategory", "分类文件更新失败")
            callback?.backUi { invoke(-1L) }
            callback?.backUi { invoke(-1L) }
        }
        // 删除旧的封面图
        if (originBean.bgPath != null && originBean.bgPath != newBean.bgPath) {
            FileIOService.deleteFile(originBean.bgPath)
        }
    }

    private fun doUpdateChildCategory(originBean: CategoryInfoBean, newBean: CategoryInfoBean, callback: ((Long) -> Unit)?) {
        // 更新category_info存储的数据即可
        // 1.内存里找不到该分类，return
        val deleteBean = categoryList.findByCondition { it.id == originBean.id } ?: let {
            Ln.e("UpdateCategory", "未找到分类")
            callback?.backUi { invoke(-1L) }
            return
        }
        // 2.正确做法是先创建副本，修改值，保存到本地成功后，才能更新内存数据
        // 这里取巧：先修改内存，再保存本地，如果本地保存失败了，再把内存数据改回去
        val index = categoryList.indexOf(deleteBean)
        if (index != -1) {
            categoryList[index] = newBean
        }
        if (FileUtils.saveString("$ROOT_PATH/$CATEGORY_INFO_PATH", Gson().toJson(categoryList))) {
            // 3.文件更新成功 回调
            backUi {
                callback?.invoke(originBean.id)
            }
        } else {
            categoryList[index] = originBean
            Ln.e("UpdateCategory", "分类文件更新失败")
            callback?.backUi { invoke(-1L) }
            callback?.backUi { invoke(-1L) }
        }
        // 删除旧的封面图
        if (originBean.bgPath != null && originBean.bgPath != newBean.bgPath) {
            FileIOService.deleteFile(originBean.bgPath)
        }
    }

    /**
     * 创建新条目
     */
    fun createEntry(bean: BaseEntryBean, callback: Callback<BaseEntryBean>? = null) {
        ioService.postEvent {
            doCreateEntry(bean, callback)
        }
    }

    /**
     * 删除条目
     */
    fun deleteEntry(bean: BaseEntryBean, callback: Callback<BaseEntryBean>? = null) {
        ioService.postEvent {
            doDeleteEntry(bean, callback)
        }
    }

    /**
     * 更新条目
     */
    fun updateEntry(origin: BaseEntryBean, bean: BaseEntryBean, callback: Callback<BaseEntryBean>? = null) {
        ioService.postEvent {
            doUpdateEntry(origin, bean, callback)
        }
    }

    // 如果是文件夹Entry，不仅要修改Entry，还要修改文件夹信息
    private fun doUpdateEntry(origin: BaseEntryBean, updater: BaseEntryBean, callback: Callback<BaseEntryBean>?) {
        // 同一文件夹做修改
        if (origin.belongTo == updater.belongTo) {
            val categoryId = updater.belongTo
            val filter = categoryList.filter { it.id == categoryId }
            if (filter.isNotEmpty()) {
                // 更新本地数据
                val jsonList = FileUtils.readStringByLine("$ROOT_PATH/$categoryId").toMutableList()
                var updateIndex: Int? = null
                jsonList.forEachIndexed { index, str ->
                    str.toEntry()?.let { entry ->
                        if (updater == entry) {
                            updateIndex = index
                        }
                    }
                }
                updateIndex?.let { index ->
                    jsonList.removeAt(index)
                    jsonList.add(index, updater.toJson())
                    var jsonString = ""
                    jsonList.forEach { jsonString += "$it\n" }
                    val success = FileUtils.saveString("$ROOT_PATH/$categoryId", jsonString)
                    if (success) {
                        // 更新内存数据
                        entryMap[categoryId]?.set(index, updater)
                        if (origin is CategoryEntryBean && updater is CategoryEntryBean) {
                            doUpdateChildCategory(origin.categoryInfoBean, updater.categoryInfoBean) {
                                callback?.onSuccess(updater)
                                callbackRegister(entryCallbacks) {
                                    if (it.id == categoryId) {
                                        it.onDataUpdated(updater)
                                    }
                                }
                                callbackRegister(globalEntryCallbacks) {
                                    it.onDataUpdated(updater)
                                }
                            }
                            return
                        }
                        // 回调注册
                        backUi {
                            callback?.onSuccess(updater)
                            callbackRegister(entryCallbacks) {
                                if (it.id == categoryId) {
                                    it.onDataUpdated(updater)
                                }
                            }
                            callbackRegister(globalEntryCallbacks) {
                                it.onDataUpdated(updater)
                            }
                        }
                    } else {
                        callback?.backUi { onFail("文件写入失败") }
                    }
                } ?: callback?.backUi { onFail("查找条目失败") }
            } else {
                callback?.backUi { onFail("查找分类失败") }
            }
        } else {
            // 不同文件夹做修改
            // 1. 先找到旧的Entry并删掉
            val filter = categoryList.filter { it.id == origin.belongTo }
            if (filter.isNotEmpty()) {
                // 移除本地数据
                val jsonList = FileUtils.readStringByLine("$ROOT_PATH/${origin.belongTo}").toMutableList()
                val iterator = jsonList.iterator()
                var exit = false
                while (iterator.hasNext() && !exit) {
                    val next = iterator.next()
                    next.toEntry()?.let { entry ->
                        if (updater == entry) {
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
                val success = FileUtils.saveString("$ROOT_PATH/${origin.belongTo}", jsonString)
                if (success) {
                    entryMap[origin.belongTo]?.remove(updater)
                }
            }
            // 2. 写入到新文件夹下
            val success = FileUtils.appendString("$ROOT_PATH/${updater.belongTo}", updater.toJson() + "\n")
            if (success) {
                entryMap[updater.belongTo]?.add(updater)
                if (origin is CategoryEntryBean && updater is CategoryEntryBean) {
                    doUpdateChildCategory(origin.categoryInfoBean, updater.categoryInfoBean) {
                        callback?.onSuccess(updater)
                        callbackRegister(entryCallbacks) {
                            if (it.id == origin.belongTo || it.id == updater.belongTo) {
                                it.onDataUpdated(updater)
                            }
                        }
                        callbackRegister(globalEntryCallbacks) {
                            it.onDataUpdated(updater)
                        }
                    }
                    return
                }
                // 回调注册
                backUi {
                    callback?.onSuccess(updater)
                    callbackRegister(entryCallbacks) {
                        if (it.id == origin.belongTo || it.id == updater.belongTo) {
                            // 对于原先分类 走更新
                            it.onDataUpdated(updater)
                        }
                    }
                    callbackRegister(globalEntryCallbacks) {
                        it.onDataUpdated(updater)
                    }
                }
            }
        }
        // 图片条目，如果更新了图片，则删除旧图片
        (origin as? ImageEntryBean)?.let { originImageBean ->
            (updater as? ImageEntryBean)?.let { updaterImageBean ->
                if (originImageBean.path != updaterImageBean.path) {
                    FileIOService.deleteFile(originImageBean.path)
                }
            }
        }
        // 筛选被移除掉的图片 然后删掉
        (origin as? GalleryEntryBean)?.let { originGalleryBean ->
            (updater as? GalleryEntryBean)?.let { updaterGalleryBean ->
                originGalleryBean.path.forEach { originPath ->
                    if (!updaterGalleryBean.path.contains(originPath)) {
                        FileIOService.deleteFile(originPath)
                    }
                }
            }
        }
    }

    private fun doCreateEntry(
        bean: BaseEntryBean,
        callback: Callback<BaseEntryBean>? = null
    ) {
        val categoryId = bean.belongTo
        val list = entryMap[categoryId]
        // 存在指定ID的分类才能添加条目
        list?.let {
            val result = bean.toJson() + "\n"
            Ln.i("DataService", "保存条目：$result 当前线程 ${Thread.currentThread()}")
            val isSuccess = FileUtils.appendString("$ROOT_PATH/$categoryId", result)
            if (isSuccess) {
                // 文件写入成功后，才能更新内存数据
                it.add(bean)
                // 创建子文件夹
                if (bean is CategoryEntryBean) {
                    doCreateChildCategory(bean.categoryInfoBean) {
                        // 已经在主线程
                        callback?.onSuccess(bean)
                        // 回调对应ID的EntryCreate事件
                        callbackRegister(entryCallbacks) { register ->
                            if (register.id == categoryId) {
                                register.onDataCreated(bean)
                            }
                        }
                        // 回调全局EntryCreate事件
                        callbackRegister(globalEntryCallbacks) { register ->
                            register.onDataCreated(bean)
                        }
                    }
                    return@let
                }
                callback?.backUi { onSuccess(bean) }
                backUi {
                    // 回调对应ID的EntryCreate事件
                    callbackRegister(entryCallbacks) { register ->
                        if (register.id == categoryId) {
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

        Ln.safeI("DataService") { "创建条目后的条目数据：${FileUtils.readString("$ROOT_PATH/$categoryId")}" }
    }

    // 特殊 给导入使用
    fun createEntryForIE(
        beanList: List<BaseEntryBean>,
    ): Boolean {
        beanList.forEach { bean ->
            val categoryId = bean.belongTo
            val list = entryMap[categoryId]
            // 存在指定ID的分类才能添加条目
            list?.let {
                val result = bean.toJson() + "\n"
                Ln.i("DataService", "保存条目：$result 当前线程 ${Thread.currentThread()}")
                val isSuccess = FileUtils.appendString("$ROOT_PATH/$categoryId", result)
                if (isSuccess) {
                    // 文件写入成功后，才能更新内存数据
                    it.add(bean)
                    // 创建子文件夹
                    if (bean is CategoryEntryBean) {
                        doCreateChildCategory(bean.categoryInfoBean)
                    }
                    // 回调对应ID的EntryCreate事件
                    callbackRegister(entryCallbacks) { register ->
                        if (register.id == categoryId) {
                            register.backUi { onDataCreated(bean) }
                        }
                    }
                }
            }
            Ln.safeI("DataService") { "创建条目后的条目数据：${FileUtils.readString("$ROOT_PATH/$categoryId")}" }
        }
        // 回调全局EntryCreate事件
        callbackRegister(globalEntryCallbacks) { register ->
            register.backUi { onDataRefresh() }
        }
        return true
    }

    private fun doDeleteEntry(bean: BaseEntryBean, callback: Callback<BaseEntryBean>?) {
        val categoryId = bean.belongTo
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
                if (bean is CategoryEntryBean) {
                    // 删除子文件夹
                    doDeleteChildCategory(bean.categoryInfoBean.id)
                    // 回调注册
                    backUi {
                        callback?.onSuccess(bean)
                        callbackRegister(entryCallbacks) {
                            if (it.id == categoryId) {
                                it.onDataDeleted(bean)
                            }
                        }
                        callbackRegister(globalEntryCallbacks) {
                            it.onDataRefresh()
                        }
                    }
                    return
                }
                // 回调注册
                backUi {
                    callback?.onSuccess(bean)
                    callbackRegister(entryCallbacks) {
                        if (it.id == categoryId) {
                            it.onDataDeleted(bean)
                        }
                    }
                    callbackRegister(globalEntryCallbacks) {
                        it.onDataDeleted(bean)
                    }
                }
                // 如果是图片/视频Entry，删除本地转存的图片/视频
                (bean as? ImageEntryBean)?.let {
                    FileIOService.deleteFile(it.path)
                }
                (bean as? GalleryEntryBean)?.path?.forEach { path ->
                    FileIOService.deleteFile(path)
                }
                (bean as? VideoEntryBean)?.let {
                    FileIOService.deleteFile((it.path))
                }
            } else {
                callback?.backUi { onFail("文件写入失败") }
            }
        } else {
            callback?.backUi { onFail("查找分类失败") }
        }
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
    interface IGlobalEntryChanged : IDataChanged<BaseEntryBean> {

        // 全局数据需要刷新的回调
        fun onDataRefresh()
    }

    override fun toString(): String {
        callbackRegister(globalEntryCallbacks) {

        }
        return "$globalEntryCallbacks "
    }
}
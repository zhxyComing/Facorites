package com.app.dixon.facorites.core.data.service

import com.app.dixon.facorites.core.data.bean.NoteBean
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.ex.callbackRegister
import com.app.dixon.facorites.core.ex.findByCondition
import com.google.gson.Gson
import java.lang.ref.WeakReference
import java.util.*

/**
 * 全路径：com.app.dixon.facorites.core.data.service
 * 类描述：快捷笔记读取
 * 创建人：xuzheng
 * 创建时间：2022/4/24 17:17
 *
 * 需要优先启动的服务
 *
 * (data/data/xxx/file/note)
 */
object NoteService : IService {

    private const val ROOT_PATH = "note" // 根Dir

    private val ioService: WorkService = WorkService()

    private val callbacks = ArrayList<WeakReference<INoteChanged>>()
    private val notes = arrayListOf<NoteBean>()

    override fun runService() {
        ioService.runService()
        if (!FileUtils.exists(ROOT_PATH)) {
            FileUtils.createDir(ROOT_PATH)
        }
        // 获取所有的笔记
        ioService.postEvent {
            val list = FileUtils.readDir(ROOT_PATH)
            list.forEach {
                val noteBean = Gson().fromJson(it, NoteBean::class.java)
                notes.add(noteBean)
            }
        }
    }

    /**
     * 创建笔记
     */
    fun create(content: String, belongToEntry: Long) {
        ioService.postEvent {
            val id = Date().time
            val filePath = "${ROOT_PATH}/$id"
            if (!FileUtils.createNewFile(filePath)) {
                return@postEvent
            }
            val noteBean = NoteBean(id, content, belongToEntry)
            val saveJson = Gson().toJson(noteBean)
            val isSuccess = FileUtils.saveString(filePath, saveJson)
            if (isSuccess) {
                notes.add(0, noteBean)
                backUi {
                    callbackRegister(callbacks) {
                        it.onDataCreated(noteBean)
                    }
                }
            }
        }
    }

    /**
     * 删除笔记
     */
    fun delete(noteBean: NoteBean) {
        ioService.postEvent {
            val filePath = "${ROOT_PATH}/${noteBean.id}"
            if (!FileUtils.exists(filePath)) {
                return@postEvent
            }
            val success = FileUtils.deleteFile(filePath)
            if (success) {
                notes.findByCondition { it.id == noteBean.id }?.let {
                    notes.remove(it)
                }
                backUi {
                    callbackRegister(callbacks) {
                        it.onDataDeleted(noteBean)
                    }
                }
            }
        }
    }

    /**
     * 返回不可修改的数组
     */
    fun obtainNotes() = notes.toList()

    // 注册监听
    fun register(noteChanged: INoteChanged) {
        callbacks.add(WeakReference(noteChanged))
    }

    interface INoteChanged {

        fun onDataCreated(bean: NoteBean)

        fun onDataDeleted(bean: NoteBean)

        fun onDataUpdated(bean: NoteBean)
    }
}
package com.app.dixon.facorites.core.data.service

import android.net.Uri
import android.telecom.Call
import android.webkit.MimeTypeMap
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.ProgressCallback
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.util.Ln
import java.util.*


/**
 * 全路径：com.app.dixon.facorites.core.data.service
 * 类描述：文件保存、读取
 * 创建人：xuzheng
 * 创建时间：2022/10/26 15:52
 *
 * (data/data/xxx/file/file)
 */
object FileIOService : IService {

    private const val ROOT_PATH = "file" // 根Dir

    private val ioService: WorkService = WorkService()

    override fun runService() {
        ioService.runService()
        if (!FileUtils.exists(ROOT_PATH)) {
            FileUtils.createDir(ROOT_PATH)
        }
    }

    /**
     * 保存文件
     */
    fun saveFile(uri: Uri, callback: ProgressCallback<String>) {
        ioService.postEvent {
            val mime = ContextAssistant.application().contentResolver.getType(uri)
            val type = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
            Ln.i("saveFile", "mime:$mime type:$type")
            val savePath = createFileSavePath(type)
            FileUtils.saveFile(uri, savePath, object : ProgressCallback<String> {
                override fun onProgress(progress: Int) {
                    backUi { callback.onProgress(progress) }
                }

                override fun onSuccess(data: String) {
                    backUi { callback.onSuccess(data) }
                }

                override fun onFail(msg: String) {
                    backUi { callback.onFail(msg) }
                }
            })
        }
    }

    /**
     * 创建文件的保存路径
     */
    private fun createFileSavePath(type: String?): String {
        return type?.let {
            FileUtils.createFileSavePath("$ROOT_PATH/${Date().time}.$type")
        } ?: let {
            FileUtils.createFileSavePath("$ROOT_PATH/${Date().time}")
        }
    }

    /**
     * 删除文件
     */
    fun deleteFile(absolutePath: String) {
        ioService.postEvent {
            FileUtils.deleteFileAbs(absolutePath)
        }
    }

}
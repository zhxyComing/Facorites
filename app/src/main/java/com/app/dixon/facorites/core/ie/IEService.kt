package com.app.dixon.facorites.core.ie

import android.os.Environment
import com.app.dixon.facorites.core.common.SuccessCallback
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.ex.process
import java.io.File

/**
 * 全路径：com.app.dixon.facorites.core.ie
 * 类描述：导入导出服务
 * 创建人：xuzheng
 * 创建时间：6/28/22 11:27 AM
 *
 * 不需要先行启动的服务
 */
object IEService : IService {

    private val ROOT_PATH = "${getSDPath()}/收藏夹子"

    private val ioService: WorkService = WorkService()

    private fun getSDPath(): String {
        var sdDir: File? = null
        val sdCardExist = (Environment.getExternalStorageState()
                == Environment.MEDIA_MOUNTED) //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory() //获取根目录
        }
        return sdDir.toString()
    }

    override fun runService() {
        ioService.runService()
    }

    // 因为可能没有SD卡权限，所以要在导出时再尝试创建文件夹目录
    private fun createDir(): Boolean {
        // 首次启动初始化
        if (!FileUtils.existsAbs(ROOT_PATH)) {
            return FileUtils.createDirAbs(ROOT_PATH)
        }
        return true
    }

    private fun execute(block: () -> Unit) {
        // 只有根目录存在才能执行
        if (createDir()) {
            ioService.postEvent(block)
        }
    }

    // 导出书签 可带进度
    fun exportBookmark(onProgress: ((progress: Int) -> Unit)? = null, onFail: ((msg: String) -> Unit)? = null, onSuccess: (path: String) -> Unit) = execute {
        realExportBookmark(onProgress, onFail, onSuccess)
    }

    private fun realExportBookmark(onProgress: ((progress: Int) -> Unit)? = null, onFail: ((msg: String) -> Unit)? = null, onSuccess: (path: String) -> Unit) {
        // 创建空文件
        val filePath = "$ROOT_PATH/收藏夹子书签_${System.currentTimeMillis()}.html"
        if (!FileUtils.createNewFileAbs(filePath)) {
            backUi { onFail?.invoke("备份文件创建失败") }
            return
        }
        // 读取文件前缀并写入
        // 防止字符串超限，直接写入文件
        val prefix = FileUtils.readAssets("exportprefix.html") ?: return
        FileUtils.appendStringAbs(filePath, prefix)
        // 开始写入分类及书签 图片类型忽略
        DataService.getCategoryList().forEachIndexed { index, categoryInfo ->
            val categoryContent = "            <DT><H3 ADD_DATE=\"${categoryInfo.id}\" LAST_MODIFIED=\"${categoryInfo.id}\">" +
                    "${categoryInfo.name}</H3>\n" +
                    "              <DL><p>\n"
            FileUtils.appendStringAbs(filePath, categoryContent)
            DataService.getEntryList(categoryInfo.id)?.forEach {
                it.process({ linkEntry ->
                    val linkContent = "                 <DT><A HREF=\"${linkEntry.link}\" ADD_DATE=\"${linkEntry.date}\" " +
                            "ICON=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAA1ElEQVQ4jeWTIQ7CQBBF/9+0JMUgm2B7AzQcAYFAVvQUoCqqQHKCijqOQXqNSppUFsGGlGQQBELZNixYvvyz7+/sZJbLvQyqQidChBAZw0ZkSUHmB17sVIVOBLKCWKF3iYwFWFWFhhIi/AJt5xCh09d2NHWR5g0O6+HTq7VgvtOtTlQfHE1dwx95NDwj4AFfrgJlnjfkvBtp3iDNGygCi4lR/hzw+mYbdc7g3wNOun+n644aZ5vz0foTGTRLRUH2EwyAgkz5gRcT3IIsv7mZ4NYPvPgGL+xIUqFKOAoAAAAASUVORK5CYII=\"" +
                            ">${linkEntry.title}</A>\n"
                    FileUtils.appendStringAbs(filePath, linkContent)
                }, {})
            }
            FileUtils.appendStringAbs(filePath, "              </DL><p>\n")
            val progress = (((index.toFloat() + 1f) / DataService.getCategoryList().size.toFloat()) * 100).toInt()
            backUi { onProgress?.invoke(progress) }
        }
        FileUtils.appendStringAbs(filePath, "        </DL><p>\n</DL><p>")
        backUi { onProgress?.invoke(100) }
        backUi { onSuccess.invoke(filePath) }
    }

    // 导入书签
    fun importBookmark(callback: SuccessCallback<String>) = execute {
        realImportBookmark(callback)
    }

    private fun realImportBookmark(callback: SuccessCallback<String>) {
        // TODO
    }

}
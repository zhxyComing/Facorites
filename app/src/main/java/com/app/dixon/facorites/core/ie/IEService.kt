package com.app.dixon.facorites.core.ie

import androidx.documentfile.provider.DocumentFile
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.EXPORT_ROOT_CATEGORY
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.base.DocumentFileUtils
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.ex.process
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * 全路径：com.app.dixon.facorites.core.ie
 * 类描述：导入导出服务
 * 创建人：xuzheng
 * 创建时间：6/28/22 11:27 AM
 *
 * 不需要先行启动的服务
 */
object IEService : IService {

    private val ROOT_PATH = "${FileUtils.getSDPath()}/$EXPORT_ROOT_CATEGORY"

    private val ioService: WorkService = WorkService()

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
        val fileName =  SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒", Locale.CHINA).format(Date())
        val filePath = "$ROOT_PATH/收藏夹子书签_$fileName.html"
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
    fun importBookmark(file: File, onProgress: ((progress: Int) -> Unit)? = null, onFail: ((msg: String) -> Unit)? = null, onSuccess: () -> Unit) = execute {
        realImportBookmark(file, onProgress, onFail, onSuccess)
    }

    private fun realImportBookmark(file: File, onProgress: ((progress: Int) -> Unit)? = null, onFail: ((msg: String) -> Unit)? = null, onSuccess: () -> Unit) {
        val size = parseLinkNumber(FileUtils.readStringAbs(file.absolutePath))
        if (size == 0) {
            backUi { onFail?.invoke("未找到有效链接") }
            return
        }
        val lineList = FileUtils.readStringByLineAbs(file.absolutePath)
        val currentTimeFormat = SimpleDateFormat("yyyy_MM_dd", Locale.CHINA).format(Date())
        DataService.createCategory("导入的书签_$currentTimeFormat") { id ->
            execute {
                var index = 0
                val baseID = System.currentTimeMillis()
                val importList = mutableListOf<BaseEntryBean>()
                lineList.forEach {
                    // 说明是收藏条目
                    val trimString = it.trimStart()
                    if (trimString.contains("<DT><A HREF")) {
                        val title = parseTextFromLinkCode(trimString)
                        val link = parseLinkFromLinkCode(trimString)
                        importList.add(
                            LinkEntryBean(
                                link = link,
                                title = title,
                                remark = "",
                                date = baseID + index,
                                belongTo = id
                            )
                        )
                        index++
                        backUi { onProgress?.invoke(50) }
                    }
                }
                DataService.createEntry(
                    importList,
                    callback = object : Callback<List<BaseEntryBean>> {
                        override fun onSuccess(data: List<BaseEntryBean>) {
                            backUi { onProgress?.invoke(100) }
                            backUi { onSuccess.invoke() }
                        }

                        override fun onFail(msg: String) {
                            backUi { onFail?.invoke("收藏创建失败") }
                        }
                    }
                )
            }
        }
    }

    // 导入书签
    fun importBookmark(file: DocumentFile, onProgress: ((progress: Int) -> Unit)? = null, onFail: ((msg: String) -> Unit)? = null, onSuccess: () -> Unit) = execute {
        realImportBookmark(file, onProgress, onFail, onSuccess)
    }

    private fun realImportBookmark(documentFile: DocumentFile, onProgress: ((progress: Int) -> Unit)? = null, onFail: ((msg: String) -> Unit)? = null, onSuccess: () -> Unit) {
        if (!documentFile.canRead()) {
            backUi { onFail?.invoke("文件不可读") }
            return
        }
        // 转存DocumentFile到File，方便操作
        val tempFilePath = FileUtils.createTempFile()
        if (tempFilePath == null) {
            backUi { onFail?.invoke("临时缓存文件创建失败") }
            return
        }
        val executableFile = DocumentFileUtils.exchangeDFtoFile(documentFile, tempFilePath)
        backUi { onProgress?.invoke(10) } // 转存完文件 进度+10
        executableFile?.let { file ->
            val size = parseLinkNumber(FileUtils.readStringAbs(file.absolutePath))
            if (size == 0) {
                backUi { onFail?.invoke("未找到有效链接") }
                file.delete()
                return
            }
            val lineList = FileUtils.readStringByLineAbs(file.absolutePath)
            val currentTimeFormat = SimpleDateFormat("yyyy_MM_dd", Locale.CHINA).format(Date())
            DataService.createCategory("导入的书签_$currentTimeFormat") { id ->
                // 这里回到了主线程
                execute {
                    var index = 0
                    val baseID = System.currentTimeMillis()
                    val importList = mutableListOf<BaseEntryBean>()
                    lineList.forEach {
                        // 说明是收藏条目
                        val trimString = it.trimStart()
                        if (trimString.contains("<DT><A HREF")) {
                            val title = parseTextFromLinkCode(trimString)
                            val link = parseLinkFromLinkCode(trimString)
                            importList.add(
                                LinkEntryBean(
                                    link = link,
                                    title = title,
                                    remark = "",
                                    date = baseID + index,
                                    belongTo = id
                                )
                            )
                            index++
                            backUi { onProgress?.invoke(50) }
                        }
                    }
                    DataService.createEntry(
                        importList,
                        callback = object : Callback<List<BaseEntryBean>> {
                            override fun onSuccess(data: List<BaseEntryBean>) {
                                backUi { onProgress?.invoke(100) }
                                backUi { onSuccess.invoke() }
                                // 删除临时文件
                                file.delete()
                            }

                            override fun onFail(msg: String) {
                                backUi { onFail?.invoke("收藏创建失败") }
                                // 删除临时文件
                                file.delete()
                            }
                        }
                    )
                }
            }
        } ?: backUi { onFail?.invoke("文件转存失败") }
    }

    // 从类似下面的代码中解析出内容
    // <DT><A HREF="https://www.gamersky.com/news/202206/1494920.shtml" ADD_DATE="1656470590007" >xxx</A>
    // 原理是找到<>区间内的字符串并删掉，剩余的就是有效内容
    private fun parseTextFromLinkCode(str: String): String {
        val stringBuilder = StringBuilder()
        var ignore = false
        str.forEach {
            if (it == '<') {
                ignore = true
                return@forEach
            } else if (it == '>') {
                ignore = false
                return@forEach
            }
            if (!ignore) {
                stringBuilder.append(it)
            }
        }
        return stringBuilder.toString()
    }

    // 从类似下面的代码中解析出链接（该链接不一定是URL）
    // <DT><A HREF="https://www.gamersky.com/news/202206/1494920.shtml" ADD_DATE="1656470590007" >xxx</A>
    private fun parseLinkFromLinkCode(str: String): String {
        val regex =
            "HREF=\\\"((?!\\\").)+\\\""
        val matcher = Pattern.compile(regex).matcher(str)
        if (matcher.find()) {
            val temp = matcher.group()
            // 去掉 HREF=" "
            return temp.substring(6, temp.length - 1)
        }
        return ""
    }

    private fun parseLinkNumber(str: String): Int {
        val regex =
            "HREF=\\\"((?!\\\").)+\\\""
        val matcher = Pattern.compile(regex).matcher(str)
        var count = 0
        while (matcher.find()) {
            val temp = matcher.group()
            if (!temp.isNullOrEmpty()) {
                count++
            }
        }
        return count
    }
}
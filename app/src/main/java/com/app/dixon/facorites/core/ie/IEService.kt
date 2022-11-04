package com.app.dixon.facorites.core.ie

import androidx.documentfile.provider.DocumentFile
import com.app.dixon.facorites.core.common.EXPORT_ROOT_CATEGORY
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.base.DocumentFileUtils
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.ex.process
import com.app.dixon.facorites.core.util.Ln
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

private const val RETRACT = "    "

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

    /**
     * 每一级菜单都这样：
     * <DT><H3 ADD_DATE="1645856537" LAST_MODIFIED="1645859199">收藏夹子</H3>
     * <DL><p>
     *     xxx
     * </DL><p>
     */
    private fun realExportBookmark(onProgress: ((progress: Int) -> Unit)? = null, onFail: ((msg: String) -> Unit)? = null, onSuccess: (path: String) -> Unit) {
        // 创建空文件
        val fileName = SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒", Locale.CHINA).format(Date())
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
        DataService.getCategoryList()
            .filter { categoryInfoBean -> categoryInfoBean.belongTo == null } // 根目录
            .forEachIndexed { index, categoryInfo ->
                // 分类标题
                appendCategory(filePath, categoryInfo, 2)
                val progress = (((index.toFloat() + 1f) / DataService.getCategoryList().size.toFloat()) * 100).toInt()
                backUi { onProgress?.invoke(progress) }
            }
        FileUtils.appendStringAbs(filePath, "$RETRACT</DL><p>\n</DL><p>")
        backUi { onProgress?.invoke(100) }
        backUi { onSuccess.invoke(filePath) }
    }

    // 写入分类
    private fun appendCategory(filePath: String, categoryInfo: CategoryInfoBean, categoryRetract: Int) {
        // 分类标题
        val fatherRetract = RETRACT.repeat(categoryRetract)
        val childRetract = RETRACT.repeat(categoryRetract + 1)
        val categoryContent = "$fatherRetract<DT><H3 ADD_DATE=\"${categoryInfo.id}\" LAST_MODIFIED=\"${categoryInfo.id}\">" +
                "${categoryInfo.name}</H3>\n" +
                "$fatherRetract<DL><p>\n"
        FileUtils.appendStringAbs(filePath, categoryContent)
        // 分类下的数据
        DataService.getEntryList(categoryInfo.id)?.forEach {
            it.process({ linkEntry ->
                // 分类下的链接类型收藏
                val linkContent = "$childRetract<DT><A HREF=\"${linkEntry.link}\" ADD_DATE=\"${linkEntry.date}\" " +
                        "ICON=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAA1ElEQVQ4jeWTIQ7CQBBF/9+0JMUgm2B7AzQcAYFAVvQUoCqqQHKCijqOQXqNSppUFsGGlGQQBELZNixYvvyz7+/sZJbLvQyqQidChBAZw0ZkSUHmB17sVIVOBLKCWKF3iYwFWFWFhhIi/AJt5xCh09d2NHWR5g0O6+HTq7VgvtOtTlQfHE1dwx95NDwj4AFfrgJlnjfkvBtp3iDNGygCi4lR/hzw+mYbdc7g3wNOun+n644aZ5vz0foTGTRLRUH2EwyAgkz5gRcT3IIsv7mZ4NYPvPgGL+xIUqFKOAoAAAAASUVORK5CYII=\"" +
                        ">${linkEntry.title}</A>\n"
                FileUtils.appendStringAbs(filePath, linkContent)
            }, {
               // 图片不参与书签导出
            }, { categoryEntry ->
                // 分类下的子文件夹
                appendCategory(filePath, categoryEntry.categoryInfoBean, categoryRetract + 1)
            }, {
                // 一句话不参与书签导出
            }, {
                // 相册集不参与书签导出
            }, {
                // 视频不参与书签导出
            }, {
                // 文件不参与书签导出
            })
        }
        FileUtils.appendStringAbs(filePath, "$fatherRetract</DL><p>\n")
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
        var filterRoot = false // 过滤根分类
        DataService.createCategory("书签_$currentTimeFormat") { id ->
            // 这里回到了主线程
            execute {
                var offset = 0
                val baseID = System.currentTimeMillis()
                // 根节点其实没有BelongTo的概念，且也不是BaseEntryBean的子类
                // 这里为了方便存储，将根节点作为CategoryEntryBean
                val rootCategory = CategoryEntryBean(
                    CategoryInfoBean(id, "导入书签_$currentTimeFormat"),
                    date = id,
                    belongTo = id
                )
                val rootChildList: MutableList<ImportNode> = mutableListOf()
                var executeNode = ImportNode(
                    data = rootCategory,
                    childList = rootChildList,
                    father = null
                )
                Ln.i("ImportDataExchange", "开始 Root_$executeNode")
                // 将文件转为Node结构，方便导入
                lineList.forEach {
                    Ln.i("ImportDataExchange", "遍历 Line_$it")
                    // 说明是收藏条目
                    val trimString = it.trimStart()
                    if (executeNode.data is CategoryEntryBean && executeNode.childList != null) {
                        // 普通条目 直接添加
                        if (trimString.contains("<DT><A HREF")) { // 条目
                            val title = parseTextFromLinkCode(trimString)
                            val link = parseLinkFromLinkCode(trimString)
                            Ln.i("ImportDataExchange", "添加条目 Title_$title")
                            offset += 1
                            executeNode.childList?.add(
                                ImportNode(
                                    data = LinkEntryBean(
                                        link = link,
                                        title = title,
                                        remark = "",
                                        date = baseID + offset,
                                        belongTo = executeNode.data.date
                                    ),
                                    childList = null,
                                    father = executeNode
                                )
                            )
                        }
                        // 分类开始
                        if (trimString.startsWith("<DT>") && !trimString.startsWith("<DT><A HREF")) {
                            if (!filterRoot) {
                                Ln.i("ImportDataExchange", "过滤根文件夹")
                                filterRoot = true
                                return@forEach
                            }
                            val title = parseTextFromLinkCode(trimString)
                            val moreLowerChildList = mutableListOf<ImportNode>()
                            offset += 1
                            executeNode.childList?.let { list ->
                                val node = ImportNode(
                                    CategoryEntryBean(
                                        CategoryInfoBean(baseID + offset, title, belongTo = executeNode.data.date),
                                        date = baseID + offset,
                                        belongTo = executeNode.data.date
                                    ),
                                    childList = moreLowerChildList,
                                    father = executeNode
                                )
                                list.add(node)
                                // 开始下一层文件夹条目的添加
                                executeNode = node
                                Ln.i("ImportDataExchange", "赋值子文件夹_$executeNode")
                            }
                        }
                        // 分类结束
                        if (trimString.startsWith("</DL><p>")) {
                            if (!filterRoot) return@forEach
                            // 返回上一级
                            executeNode.father?.let { node ->
                                executeNode = node
                                Ln.i("ImportDataExchange", "赋值父文件夹 $executeNode")
                            }
                        }
                    }
                    backUi { onProgress?.invoke(50) }
                }
                Ln.i("ImportDataExchange", "结束 $executeNode")
                if (doImport(executeNode)) {
                    backUi { onProgress?.invoke(100) }
                    backUi { onSuccess.invoke() }
                }
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
            var filterRoot = false // 过滤根分类
            DataService.createCategory("书签_$currentTimeFormat") { id ->
                // 这里回到了主线程
                execute {
                    var offset = 0
                    val baseID = System.currentTimeMillis()
                    // 根节点其实没有BelongTo的概念，且也不是BaseEntryBean的子类
                    // 这里为了方便存储，将根节点作为CategoryEntryBean
                    val rootCategory = CategoryEntryBean(
                        CategoryInfoBean(id, "导入书签_$currentTimeFormat"),
                        date = id,
                        belongTo = id
                    )
                    val rootChildList: MutableList<ImportNode> = mutableListOf()
                    var executeNode = ImportNode(
                        data = rootCategory,
                        childList = rootChildList,
                        father = null
                    )
                    Ln.i("ImportDataExchange", "开始 Root_$executeNode")
                    // 将文件转为Node结构，方便导入
                    lineList.forEach {
                        Ln.i("ImportDataExchange", "遍历 Line_$it")
                        // 说明是收藏条目
                        val trimString = it.trimStart()
                        if (executeNode.data is CategoryEntryBean && executeNode.childList != null) {
                            // 普通条目 直接添加
                            if (trimString.contains("<DT><A HREF")) { // 条目
                                val title = parseTextFromLinkCode(trimString)
                                val link = parseLinkFromLinkCode(trimString)
                                Ln.i("ImportDataExchange", "添加条目 Title_$title")
                                offset += 1
                                executeNode.childList?.add(
                                    ImportNode(
                                        data = LinkEntryBean(
                                            link = link,
                                            title = title,
                                            remark = "",
                                            date = baseID + offset,
                                            belongTo = executeNode.data.date
                                        ),
                                        childList = null,
                                        father = executeNode
                                    )
                                )
                            }
                            // 分类开始
                            if (trimString.startsWith("<DT>") && !trimString.startsWith("<DT><A HREF")) {
                                if (!filterRoot) {
                                    Ln.i("ImportDataExchange", "过滤根文件夹")
                                    filterRoot = true
                                    return@forEach
                                }
                                val title = parseTextFromLinkCode(trimString)
                                val moreLowerChildList = mutableListOf<ImportNode>()
                                offset += 1
                                executeNode.childList?.let { list ->
                                    val node = ImportNode(
                                        CategoryEntryBean(
                                            CategoryInfoBean(baseID + offset, title, belongTo = executeNode.data.date),
                                            date = baseID + offset,
                                            belongTo = executeNode.data.date
                                        ),
                                        childList = moreLowerChildList,
                                        father = executeNode
                                    )
                                    list.add(node)
                                    // 开始下一层文件夹条目的添加
                                    executeNode = node
                                    Ln.i("ImportDataExchange", "赋值子文件夹_$executeNode")
                                }
                            }
                            // 分类结束
                            if (trimString.startsWith("</DL><p>")) {
                                if (!filterRoot) return@forEach
                                // 返回上一级
                                executeNode.father?.let { node ->
                                    executeNode = node
                                    Ln.i("ImportDataExchange", "赋值父文件夹 $executeNode")
                                }
                            }
                        }
                        backUi { onProgress?.invoke(50) }
                    }
                    Ln.i("ImportDataExchange", "结束 $executeNode")
                    file.delete()
                    if (doImport(executeNode)) {
                        backUi { onProgress?.invoke(100) }
                        backUi { onSuccess.invoke() }
                    }
                }
            }
        } ?: backUi { onFail?.invoke("文件转存失败") }
    }

    private fun doImport(importNode: ImportNode): Boolean {
        val childEntry = importNode.childList?.map {
            it.data
        }
        val childCategory = importNode.childList?.filter {
            it.data is CategoryEntryBean
        }
        // 其实目前即使数据丢失，也会返回true，但这是后续DataService创建优化的点，和这里逻辑无关
        var success = true
        childEntry?.let {
            if (DataService.createEntryForIE(it)) {
                if (!childCategory.isNullOrEmpty()) {
                    childCategory.forEach { node ->
                        if (!doImport(node)) {
                            // 创建下级目录的条目失败
                            success = false
                        }
                    }
                }
            } else {
                // 创建本级目录的条目失败
                success = false
            }
        }
        return success
    }

    private class ImportNode(val data: BaseEntryBean, val childList: MutableList<ImportNode>? = null, val father: ImportNode? = null) {

        // 避免循环输出
        override fun toString(): String {
            if (data is LinkEntryBean) {
                return "entry_${data.title}"
            }
            return "category_${(data as? CategoryEntryBean)?.categoryInfoBean?.name} child_$childList"
        }
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
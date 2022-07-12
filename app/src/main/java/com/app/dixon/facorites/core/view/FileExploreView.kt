package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.EXPORT_ROOT_CATEGORY
import com.app.dixon.facorites.core.data.service.base.DocumentFileUtils
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.ex.hide
import com.app.dixon.facorites.core.ex.show
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_file_explore_lsit_item.view.*
import kotlinx.android.synthetic.main.item_file_explore.view.*
import java.io.File
import java.util.*

/**
 * 文件浏览View
 *
 * TODO 优化启动速度
 */
@SuppressLint("NotifyDataSetChanged")
class FileExploreView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // qq存储目录
    private val qqFilePath = "${FileUtils.getSDPath()}/Android/data/com.tencent.mobileqq/Tencent/QQfile_recv"

    // 微信存储目录
    private val wxFilePath = "${FileUtils.getSDPath()}/Android/data/com.tencent.mm/MicroMsg/Download"

    private var fileClickListener: ((File?, DocumentFile?) -> Unit)? = null

    private val dataList = mutableListOf<FileWrapper>() // 当前列表的数据集
    private val stack = Stack<History>() // 操作历史
    private var currentFile: File? = File(FileUtils.getSDPath()) // 当前浏览的文件
    private var currentDocumentFile: DocumentFile? = null // 当前浏览的文件
    private var currentPosition: Int = 0 // 当前滚动位置
    private var currentOffset: Int = 0 // 当前滚动位置的偏移
    private val coreList = mutableListOf<FileWrapper>() // 核心目录列表 方便使用的目录

    // 申请隐私文件权限之后，刷新核心文件夹
    private val askDocumentPermissionCallback = {
        refreshCoreDirIfNecessary()
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_file_explore, this, true)
        container.mediumFont()
        val sdPath = FileUtils.getSDPath()
        val fileArray = FileUtils.getFileArrayByPathAbs(sdPath)
        fileArray?.let {
            dataList.clear()
            dataList.addAll(sortFile(it.toList()).toWrapperForFile())
            addCoreDir() // 添加核心目录，方便查找
            rvList.layoutManager = LinearLayoutManager(context)
            rvList.adapter = FileExploreAdapter(context, dataList) { file, documentFile ->
                parseFileClick(file, documentFile)
            }
        }
        ivBack.setOnClickListener {
            if (stack.isNotEmpty()) {
                val history = stack.pop()
                history.file?.let {
                    updateListByFile(history.file, history.position, history.offset)
                } ?: history.documentFile?.let {
                    updateListByDocumentFile(history.documentFile, history.position, history.offset)
                }
            } else {
                ToastUtil.toast("已经是根目录了～")
            }
        }
        rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                rvList.layoutManager?.let {
                    updatePositionAndOffset()
                }
            }
        })
    }

    // 记录当前 RV 的位置
    private fun updatePositionAndOffset() {
        //获取可视的第一个view
        rvList.layoutManager?.getChildAt(0)?.let {
            //获取与该view的顶部的偏移量
            currentOffset = it.top
            //得到该View的数组位置
            currentPosition = rvList.layoutManager!!.getPosition(it)
        }
    }

    // 处理文件/文件夹点击
    private fun parseFileClick(file: File?, documentFile: DocumentFile?) {
        if (file == null && documentFile == null) {
            // 这种情况一般是指定了核心路径，但是该路径其实并不存在
            ToastUtil.toast("应用未安装")
            return
        }
        if ((file?.isDirectory ?: documentFile?.isDirectory) == true) {
            file?.let {
                // android11 起不允许访问 Android/data 应用私有目录，申请权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && file.absolutePath.contains(DocumentFileUtils.documentPath)) {
                    // 场景1：Android11以上点击隐私文件夹，有权限
                    // 最初的 DocumentFile
                    if (DocumentFileUtils.isGrant(context)) {
                        DocumentFileUtils.findDocumentFileByPath(context, DocumentFileUtils.documentPath)?.let { documentFile ->
                            recordHistory()
                            updateListByDocumentFile(documentFile)
                        }
                    } else {
                        // 场景2：Android11以上点击隐私文件夹，无权限
                        ContextAssistant.activity()?.let {
                            OptionDialog(
                                context = it,
                                title = "手动授权提醒",
                                desc = it.resources.getString(R.string.app_android_r_storage_ask_tip),
                                rightString = "授权",
                                leftString = "关闭",
                                rightClick = {
                                    DocumentFileUtils.askPermission(it, askDocumentPermissionCallback)
                                }).show()
                        }
                    }
                } else {
                    // 场景3：任意版本点击普通文件夹
                    recordHistory()
                    updateListByFile(file)
                }
            } ?: documentFile?.let {
                // 场景4：Android11以上在申请权限后点击隐私文件夹
                recordHistory()
                updateListByDocumentFile(documentFile)
            }
        } else {
            fileClickListener?.invoke(file, documentFile)
        }
    }

    private fun recordHistory() {
        // 正向点击记录
        stack.add(History(currentFile, currentDocumentFile, currentPosition, currentOffset))
    }

    private fun updateListByDocumentFile(file: DocumentFile, position: Int = 0, offset: Int = 0) {
        if (!hasDocumentPermission()) {
            return
        }
        currentFile = null
        currentDocumentFile = file
        currentPosition = position
        currentOffset = offset
        val newArray = file.listFiles()
        if (newArray.isNotEmpty()) {
            val displayList = newArray.toList()
            if (displayList.isNotEmpty()) {
                dataList.clear()
                dataList.addAll(displayList.toWrapperForDocumentFile())
                llEmptyTip.hide()
            } else {
                showEmptyList()
            }
        } else {
            showEmptyList()
        }
        rvList.adapter?.notifyDataSetChanged()
        Ln.i("FileExploreView", "恢复位置：$position $offset")
        // 恢复位置
        (rvList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, offset)
    }

    private fun hasDocumentPermission(): Boolean {
        ContextAssistant.activity()?.let {
            return if (!DocumentFileUtils.isGrant(it)) {
                // 没权限，跳转页面申请权限
                DocumentFileUtils.askPermission(it, askDocumentPermissionCallback)
                false
            } else {
                // 有权限，展示内容
                true
            }
        }
        return false
    }

    // 更新展示的文件列表
    private fun updateListByFile(file: File, position: Int = 0, offset: Int = 0) {
        currentFile = file
        currentDocumentFile = null
        currentPosition = position
        currentOffset = offset
        val newArray = FileUtils.getFileArrayByPathAbs(file.absolutePath)
        if (!newArray.isNullOrEmpty()) {
            val displayList = sortFile(newArray.toList())
            if (displayList.isNotEmpty()) {
                dataList.clear()
                dataList.addAll(displayList.toWrapperForFile())
                // 根目录 添加核心文件夹
                if (file.absolutePath == FileUtils.getSDPath()) {
                    addCoreDir()
                }
                llEmptyTip.hide()
            } else {
                showEmptyList()
            }
        } else {
            showEmptyList()
        }
        rvList.adapter?.notifyDataSetChanged()
        Ln.i("FileExploreView", "恢复位置：$position $offset")
        // 恢复位置
        (rvList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, offset)
    }

    // 添加核心文件夹目录
    private fun addCoreDir() {
        if (coreList.isEmpty()) {
            tvTip.show()
            // TODO IO线程类
            Thread {
                val tempList = mutableListOf<FileWrapper>()
                tempList.add(FileWrapper(File("${FileUtils.getSDPath()}/$EXPORT_ROOT_CATEGORY"), null, true, "收藏夹子"))
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    tempList.add(FileWrapper(File(qqFilePath), null, true))
                    tempList.add(FileWrapper(File(wxFilePath), null, true))
                } else if (DocumentFileUtils.isGrant(context)) {
                    // 耗时
                    val qqFile = DocumentFileUtils.findDocumentFileByPath(context, qqFilePath)
                    tempList.add(FileWrapper(null, qqFile, true, "QQ接收文件"))
                    val wxFile = DocumentFileUtils.findDocumentFileByPath(context, wxFilePath)
                    tempList.add(FileWrapper(null, wxFile, true, "微信接收文件"))
                } else {
                    val qqFile = FileWrapper(File(DocumentFileUtils.documentPath), null, true, "QQ接收文件目录（需申请访问权限）")
                    tempList.add(qqFile)
                    val wxFile = FileWrapper(File(DocumentFileUtils.documentPath), null, true, "微信接收文件目录（需申请访问权限）")
                    tempList.add(wxFile)
                }
                backUi {
                    tvTip.hide()
                    if (currentFile?.absolutePath == FileUtils.getSDPath()) {
                        coreList.clear()
                        coreList.addAll(0, tempList)
                        dataList.addAll(0, coreList)
                        rvList.adapter?.notifyDataSetChanged()
                    }
                }
            }.start()
        }
        dataList.addAll(0, coreList)
    }

    // 刷新核心文件夹目录
    // 用在申请权限之后
    private fun refreshCoreDirIfNecessary() {
        if (currentFile?.absolutePath == FileUtils.getSDPath()) {
            for (i in 0..coreList.size) {
                dataList.removeAt(0)
            }
            coreList.clear()
            addCoreDir()
            rvList.adapter?.notifyDataSetChanged()
        }
    }

    // 空列表（空文件夹）
    private fun showEmptyList() {
        dataList.clear()
        llEmptyTip.show()
    }

    private class FileExploreAdapter(val context: Context, val data: List<FileWrapper>, val itemClick: (File?, DocumentFile?) -> Unit) :
        RecyclerView.Adapter<FileExploreAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val item = LayoutInflater.from(context).inflate(R.layout.app_file_explore_lsit_item, parent, false)
            item.normalFont()
            return ViewHolder(item)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val wrapper = data[position]
            val file = wrapper.file
            val documentFile = wrapper.documentFile
            holder.itemView.tvContent.text = wrapper.rename ?: file?.name ?: documentFile?.name
            if ((file?.isDirectory ?: documentFile?.isDirectory) == true) {
                holder.itemView.ivTag.setImageResource(R.drawable.app_icon_dir)
            } else {
                holder.itemView.ivTag.setImageResource(R.drawable.app_icon_file)
            }
            holder.itemView.itemContainer.setOnClickListener {
                itemClick.invoke(file, documentFile)
            }
            if (wrapper.core) {
                holder.itemView.tvContent.setTextColor(context.resources.getColor(R.color.app_theme_green_dark))
            } else {
                holder.itemView.tvContent.setTextColor(Color.parseColor("#404040"))
            }
        }

        override fun getItemCount(): Int = data.size

        class ViewHolder(item: View) : RecyclerView.ViewHolder(item)
    }

    private fun sortFile(list: List<File>): List<File> {
        // 1.筛选掉.开头的文件
        // 2.按字母顺序排序
        // 3.文件夹放前，文件放后
        return list.filter { !it.name.startsWith(".") }.sortedBy { it.name }.sortedBy { it.isFile }
    }

    private fun List<File>.toWrapperForFile(core: Boolean = false) = map { FileWrapper(it, null, core) }

    private fun List<DocumentFile>.toWrapperForDocumentFile(core: Boolean = false) = map { FileWrapper(null, it, core) }

    fun setOnFileClickListener(listener: (File?, DocumentFile?) -> Unit) {
        fileClickListener = listener
    }

    /**
     * @param file 文件
     * @param documentFile 也是文件 但是注意，二者file优先级更高，只有Android11以后的安全限制文件才是documentFile
     */
    private data class History(
        val file: File?,
        val documentFile: DocumentFile?,
        val position: Int,
        val offset: Int
    )

    private data class FileWrapper(
        val file: File?,
        val documentFile: DocumentFile?,
        val core: Boolean,
        val rename: String? = null
    )
}
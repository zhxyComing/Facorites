package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.common.ProgressCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.FileEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.FileIOService
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.normalFont
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_create_entry_content.*
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.*
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.categoryChoose
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.etFileTitle
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.flFileShowView
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.llContainer
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.tvCreate
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.tvFileLayoutTip
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.tvFileSize
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.tvFileSubLayout
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.tvFileTitle
import kotlinx.android.synthetic.main.app_dialog_create_file_entry_content.tvFileType
import java.io.File
import java.util.*

// 创建用构造函数
class CreateFileEntryDialog(
    context: Context,
    private val uri: Uri,
    private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！"),
) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_create_file_entry_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    // 视频
    private var tempFilePath: String? = null // 新导入的临时视频 如果后续选择取消 则要删除掉这些新导入的视频

    // 是否点击了确认
    private var hasSave = false

    override fun initDialog() {
        initCommonLogic()
    }

    private fun initCommonLogic() {
        llContainer.normalFont()

        backUi {
            saveFileToLocal()
        }

        tvFileLayoutTip.setOnClickListener {
            TipDialog(
                context,
                content = "导入收藏夹子的文件有以下特性：\n\n1.应用外不可见；\n" +
                        "\n2.删除手机的原文件不会影响到收藏文件。",
                title = "文件收藏提示"
            ).show()
        }
        flFileShowView.setOnClickListener { }

        tvCreate.setOnClickListener {
            saveVideo()
        }
        // 分类的下拉列表
        initSpinner()
    }

    private fun saveFileToLocal() {
        if (uri.path?.endsWith(".apk") == true ||
            DocumentFile.fromSingleUri(context, uri)?.name?.endsWith(".apk") == true
        ) {
            ToastUtil.toast("导入应用程序功能将在后续版本支持，敬请谅解..")
            backUi {
                dismiss()
            }
            return
        }
        val dialog = ProgressDialog(ContextAssistant.activity()!!, "文件导入中..").apply { show() }
        FileIOService.saveFile(FileIOService.FileType.FILE, uri, object : ProgressCallback<String> {
            override fun onSuccess(data: String) {
                dialog.dismiss()
                ToastUtil.toast("导入文件成功")
                tempFilePath = data
                val appointName = DocumentFile.fromSingleUri(context, uri)?.name ?: "未知文件"
                showFile(File(data), appointName)
            }

            override fun onFail(msg: String) {
                dialog.dismiss()
                ToastUtil.toast("导入视频失败 $msg")
            }

            override fun onProgress(progress: Int) {
                dialog.setProgress(progress)
            }
        })
    }

    // 保存图片
    private fun saveVideo() {
        val title = etFileTitle.text.toString()
        val categoryId = categoryChoose.getSelectionData<CategoryInfoBean>()?.id
        if (title.isNotEmpty() && categoryId != null && !tempFilePath.isNullOrEmpty()) {
            DataService.createEntry(
                FileEntryBean(
                    path = tempFilePath!!,
                    title = title,
                    date = Date().time,
                    belongTo = categoryId,
                ),
                callback
            )
            hasSave = true
            dismiss()
        } else {
            // 未填数据提示
            etFileTitle.shakeTipIfEmpty()
            // 没选图或者导入过程中均不允许创建或更新
            if (tempFilePath.isNullOrEmpty()) {
                tvFileSubLayout.shakeTip()
            }
        }
    }

    // 下拉选择框
    private fun initSpinner() {
        val expendInfoList = mutableListOf<CustomSpinner.ExpandInfo<CategoryInfoBean>>()
        DataService.getCategoryList().forEach {
            expendInfoList.add(CustomSpinner.ExpandInfo(it.name, it.bgPath, it))
        }
        categoryChoose.setData(expendInfoList)
        categoryChoose.setShowPos(CustomSpinner.ShowPos.TOP)
    }

    override fun onDetachedFromWindow() {
        if (!hasSave) {
            deleteExpiredFile()
        }
        super.onDetachedFromWindow()
    }

    // 删除过期的导入图片
    private fun deleteExpiredFile() {
        tempFilePath?.let {
            FileIOService.deleteFile(it)
            tempFilePath = null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showFile(file: File, appointName: String) {
        ivFileRemove.show()
        flFileShowView.show()
        tvFileImportTip.hide()
        val simpleName = if (appointName.lastIndexOf(".") != -1) {
            appointName.substring(0, appointName.lastIndexOf("."))
        } else {
            appointName
        }
        tvFileTitle.text = simpleName
        etFileTitle.setText(simpleName)
        tvFileSize.text = "文件大小：${byteToString(file.length())}"
        tvFileType.text = "文件类型：${
            if (file.path.lastIndexOf(".") != -1) {
                file.path.substring(file.path.lastIndexOf(".") + 1)
            } else {
                "未知"
            }
        }"
        tvFileType.setOnLongClickListener {
            ToastUtil.toast(file.name)
            true
        }
    }
}
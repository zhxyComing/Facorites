package com.app.dixon.facorites.core.view

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.app.dixon.facorites.R
import com.dixon.dlibrary.util.ScreenUtil
import kotlinx.android.synthetic.main.app_dialog_file_explore_content.*
import java.io.File

/**
 * 文件浏览弹窗
 */
class FileExploreDialog(context: Context, private val onSelectFileListener: (File?, DocumentFile?) -> Unit) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_file_explore_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        fileExploreView.setOnFileClickListener { file, documentFile ->
            onSelectFileListener.invoke(file, documentFile)
            dismiss()
        }
    }
}
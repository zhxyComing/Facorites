package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.ex.dp
import com.app.dixon.facorites.core.util.mediumFont
import kotlinx.android.synthetic.main.app_dialog_progress.*


// 进度条弹窗
class ProgressDialog(context: Context, val title: String) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = 300.dp

    override fun isCancelOnOutSide(): Boolean = false

    override fun contentLayout(): Int = R.layout.app_dialog_progress

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun gravity(): Int = Gravity.CENTER

    @SuppressLint("SetTextI18n")
    override fun initDialog() {
        llContainer.mediumFont()
        progressBar.max = 100
        progressBar.progress = 0
        tvTitle.text = title
    }

    fun setProgress(progress: Int) {
        progressBar.progress = progress
    }

    override fun onBackPressed() {
        // do nothing
    }
}
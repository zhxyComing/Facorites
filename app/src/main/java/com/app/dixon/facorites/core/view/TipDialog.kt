package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.ex.dp
import com.app.dixon.facorites.core.util.mediumFont
import kotlinx.android.synthetic.main.app_dialog_tip.*


// 提示用弹窗
class TipDialog(context: Context, val content: String) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = 300.dp

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_tip

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun gravity(): Int = Gravity.CENTER

    @SuppressLint("SetTextI18n")
    override fun initDialog() {
        llContainer.mediumFont()
        tvContent.text = content
    }

}
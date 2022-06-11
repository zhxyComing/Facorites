package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.ex.dp
import kotlinx.android.synthetic.main.app_dialog_agreement_content.*

// 隐私协议弹窗
class AgreementDialog(context: Context) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = 300.dp

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_agreement_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun gravity(): Int = Gravity.CENTER

    @SuppressLint("SetTextI18n")
    override fun initDialog() {
        // TODO 子线程处理
        val content = FileUtils.readAssets("agreement.txt")
        tvAgreement.text = content
    }
}
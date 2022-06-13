package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.AGREEMENT_CONFIRM
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.ex.dp
import com.dixon.dlibrary.util.SharedUtil
import com.umeng.commonsdk.UMConfigure
import kotlinx.android.synthetic.main.app_dialog_agreement_content.*


// 隐私协议弹窗
class AgreementDialog(context: Context) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = 300.dp

    override fun isCancelOnOutSide(): Boolean = false

    override fun contentLayout(): Int = R.layout.app_dialog_agreement_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun gravity(): Int = Gravity.CENTER

    @SuppressLint("SetTextI18n")
    override fun initDialog() {
        // 富文本内容
        val content = FileUtils.readAssets("agreement.txt")
        val textSpanned = SpannableString(content)
        textSpanned.setSpan(JumpClick("user.md"), 30, 36, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textSpanned.setSpan(JumpClick("privacy.md"), 37, 43, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvAgreement.movementMethod = LinkMovementMethod.getInstance();
        tvAgreement.text = textSpanned

        tvAgree.setOnClickListener {
            SharedUtil.putBoolean(AGREEMENT_CONFIRM, true)
            // 初次进入 因为隐私协议不同意 所以要延迟初始化
            UMConfigure.init(context, UMConfigure.DEVICE_TYPE_PHONE, "")
            dismiss()
        }

        tvCancel.setOnClickListener {
            // 直接退出主页面 在协议未确认之前也没有其他页面可以点开
            ContextAssistant.activity()?.finish()
        }
    }

    private inner class JumpClick(val assetsName: String) : ClickableSpan() {

        override fun onClick(widget: View) {
            PageJumper.openMarkdownPage(context, assetsName)
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = Color.RED
            ds.isUnderlineText = false
        }
    }

    override fun onBackPressed() {
        // do nothing
    }
}
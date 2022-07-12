package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.ex.dp
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import com.dixon.dlibrary.util.FontUtil
import kotlinx.android.synthetic.main.app_dialog_option.*


// 选择弹窗
class OptionDialog(
    context: Context,
    private val title: String,
    private val desc: String,
    private val descClick: (() -> Unit)? = null,
    private val rightString: String,
    private val leftString: String,
    private val rightClick: (() -> Unit)? = null,
    private val leftClick: (() -> Unit)? = null
) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = 300.dp

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_option

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun gravity(): Int = Gravity.CENTER

    @SuppressLint("SetTextI18n")
    override fun initDialog() {
        llContainer.mediumFont()
        tvDetail.normalFont()
//        FontUtil.font("Yun-Book.ttf", tvDetail)
        tvTitle.text = title
        tvDetail.text = desc
        btnRight.text = rightString
        btnLeft.text = leftString
        tvDetail.setOnClickListener {
            descClick?.invoke()
        }
        btnRight.setOnClickListener {
            rightClick?.invoke()
            dismiss()
        }
        btnLeft.setOnClickListener {
            leftClick?.invoke()
            dismiss()
        }
    }

}
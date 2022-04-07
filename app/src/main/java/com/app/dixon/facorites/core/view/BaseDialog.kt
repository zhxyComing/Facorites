package com.app.dixon.facorites.core.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.app.dixon.facorites.R


abstract class BaseDialog(context: Context, themeResId: Int = R.style.Dialog) : Dialog(context, themeResId) {

    companion object {
        const val PX_AUTO = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentLayout())
        setCanceledOnTouchOutside(isCancelOnOutSide())

        val window: Window? = window
        if (window != null) {
            val lp: WindowManager.LayoutParams = window.attributes
            lp.gravity = Gravity.BOTTOM
            if (widthPx() != PX_AUTO) {
                lp.width = widthPx()
            }
            if (heightPx() != PX_AUTO) {
                lp.height = heightPx()
            }
            window.attributes = lp
            if (windowAnimStyle() != 0) {
                window.setWindowAnimations(windowAnimStyle())
            }
        }

        initDialog()
    }

    protected abstract fun initDialog()

    protected open fun windowAnimStyle(): Int {
        return 0
    }

    protected abstract fun heightPx(): Int

    protected abstract fun widthPx(): Int

    protected abstract fun isCancelOnOutSide(): Boolean

    protected abstract fun contentLayout(): Int
}
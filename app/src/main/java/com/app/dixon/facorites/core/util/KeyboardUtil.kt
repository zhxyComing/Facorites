package com.app.dixon.facorites.core.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.app.dixon.facorites.core.ex.backUi


/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：软键盘工具类
 * 创建人：xuzheng
 * 创建时间：6/11/22 3:27 PM
 */
object KeyboardUtil {

    /**
     * 自动弹软键盘
     *
     * @param context
     * @param et
     */
    fun showSoftInput(context: Context, et: EditText) {
        backUi(200) {
            (context as Activity).runOnUiThread {
                et.isFocusable = true
                et.isFocusableInTouchMode = true
                //请求获得焦点
                et.requestFocus()
                //调用系统输入法
                val inputManager: InputMethodManager = et.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(et, 0)
            }
        }
    }

    /**
     * 自动关闭软键盘
     * @param activity
     */
    fun closeKeyboard(activity: Activity) {
        val imm: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
    }

    /**
     * 打开关闭相互切换
     * @param activity
     */
    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            if (activity.currentFocus!!.windowToken != null) {
                imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }
}
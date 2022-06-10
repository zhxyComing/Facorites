package com.app.dixon.facorites.core.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * 全路径：com.app.dixon.facorites.core.view
 * 类描述：放在列表里的TextView 优先滑动
 * 创建人：xuzheng
 * 创建时间：6/9/22 11:53 AM
 */
class ScrollTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyle) {

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(event)
    }
}
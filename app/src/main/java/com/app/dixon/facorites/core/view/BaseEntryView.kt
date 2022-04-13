package com.app.dixon.facorites.core.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * 全路径：com.app.dixon.facorites.core.view
 * 类描述：基础条目View
 * 创建人：xuzheng
 * 创建时间：4/13/22 4:58 PM
 */
open class BaseEntryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {

    companion object {

        // 所有条目类型组件共用一个点击时间监听
        var globalClickTime = 0L
    }
}
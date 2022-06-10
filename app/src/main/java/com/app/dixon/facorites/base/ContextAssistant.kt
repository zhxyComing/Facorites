package com.app.dixon.facorites.base

import android.app.Activity
import android.app.Application
import android.content.Context

/**
 * 全路径：com.app.dixon.facorites.base
 * 类描述：用户安全的获取 context
 * 创建人：xuzheng
 * 创建时间：6/9/22 10:37 AM
 */
object ContextAssistant {

    fun activity(): Activity? = BaseApplication.currentActivity.get()

    fun application(): Application = BaseApplication.application

    fun asContext(action: (context: Context) -> Unit) {
        BaseApplication.currentActivity.get()?.let {
            action.invoke(it)
        }
    }
}
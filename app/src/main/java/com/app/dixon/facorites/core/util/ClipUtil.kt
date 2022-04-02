package com.app.dixon.facorites.core.util

import android.content.ClipboardManager
import android.content.Context

/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：剪贴板工具类
 * 创建人：xuzheng
 * 创建时间：3/29/22 8:36 PM
 */
object ClipUtil {

    fun obtainPasteText(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val item = clipboard.primaryClip?.getItemAt(0)
        return item?.text?.toString() ?: ""
    }
}
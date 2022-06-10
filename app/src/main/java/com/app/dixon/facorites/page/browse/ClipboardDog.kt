package com.app.dixon.facorites.page.browse

import android.content.ClipboardManager
import androidx.fragment.app.FragmentActivity
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.util.Ln

/**
 * 全路径：com.app.dixon.facorites.page.browse
 * 类描述：用于监听复制/剪切操作
 * 创建人：xuzheng
 * 创建时间：6/8/22 7:14 PM
 */
class ClipboardDog {

    private var clipboardManager: ClipboardManager? = null
    private var onPrimaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    // 会莫名多次触发 做时间间隔(0.5s)过滤
    private var clipTime: Long = 0

    fun register(callback: (String) -> Unit) {
        clipboardManager = ContextAssistant.application().getSystemService(FragmentActivity.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager?.let {
            onPrimaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
                if (System.currentTimeMillis() - clipTime < 500) {
                    return@OnPrimaryClipChangedListener
                }
                clipTime = System.currentTimeMillis()
                if (it.hasPrimaryClip() && it.primaryClip?.itemCount ?: 0 > 0) {
                    // 获取复制、剪切的文本内容
                    val content: CharSequence = it.primaryClip?.getItemAt(0)?.text ?: ""
                    if (content.isNotEmpty()) {
                        Ln.i("ClipService", "复制、剪切的内容为：$content")
                        callback.invoke(content.toString())
                    }
                }
            }
            it.addPrimaryClipChangedListener(onPrimaryClipChangedListener)
        }
    }

    fun unregister() {
        if (clipboardManager != null && onPrimaryClipChangedListener != null) {
            clipboardManager!!.removePrimaryClipChangedListener(onPrimaryClipChangedListener)
        }
    }

    /**
     * 执行动作期间不响应复制/剪切的监听操作
     */
    fun shield(action: () -> Unit) {
        if (clipboardManager != null && onPrimaryClipChangedListener != null) {
            clipboardManager!!.removePrimaryClipChangedListener(onPrimaryClipChangedListener)
        }
        action.invoke()
        backUi(500) {
            clipboardManager?.addPrimaryClipChangedListener(onPrimaryClipChangedListener)
        }
    }
}
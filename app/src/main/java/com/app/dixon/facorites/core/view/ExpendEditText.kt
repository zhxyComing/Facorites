package com.app.dixon.facorites.core.view

import android.R
import android.content.Context
import android.util.AttributeSet
import com.app.dixon.facorites.core.util.ClipUtil
import com.app.dixon.facorites.core.util.Ln


/**
 * 全路径：com.app.dixon.facorites.core.view
 * 类描述：
 * 创建人：xuzheng
 * 创建时间：3/29/22 5:50 PM
 */
class ExpendEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = R.attr.editTextStyle) :
    androidx.appcompat.widget.AppCompatEditText(context, attrs, defStyle) {

    private var onPasteCallback: ((String) -> Unit)? = null

    override fun onTextContextMenuItem(id: Int): Boolean {
        when (id) {
            R.id.cut -> {
                // 剪切
            }
            R.id.copy -> {
                // 复制
            }
            R.id.paste -> {
                // 粘贴
                if (onPasteCallback != null) {
                    val text = ClipUtil.obtainPasteText(context)
                    Ln.i("ClipPasteGet", text)
                    onPasteCallback?.invoke(text)
                }
            }
        }
        return super.onTextContextMenuItem(id)
    }

    fun setOnPasteListener(onPasteCallback: (String) -> Unit) {
        this.onPasteCallback = onPasteCallback
    }
}

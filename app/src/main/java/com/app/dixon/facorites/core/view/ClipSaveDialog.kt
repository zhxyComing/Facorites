package com.app.dixon.facorites.core.view

import android.content.Context
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.service.NoteService
import com.app.dixon.facorites.core.ex.shakeTip
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import com.dixon.dlibrary.util.ScreenUtil
import kotlinx.android.synthetic.main.app_dialog_clip_save_content.*

/**
 * 保存复制内容的快捷笔记弹窗
 */
class ClipSaveDialog(context: Context, val clip: String, val entryId: Long) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_clip_save_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        llContainer.normalFont()
        tvTitle.mediumFont()
        etClipContent.setText(clip)
        tvSave.setOnClickListener {
            val content = etClipContent.text.toString()
            if (content.isNotEmpty()) {
                NoteService.create(content, entryId)
                dismiss()
            } else {
                etClipContent.shakeTip()
            }
        }
    }
}
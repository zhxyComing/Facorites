package com.app.dixon.facorites.core.view

import android.content.Context
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.findByCondition
import com.app.dixon.facorites.page.browse.SchemeJumper
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_scheme_jump_content.*

// 是否跳转外部APP的Dialog，暂时没用到
class SchemeJumpDialog(context: Context, val scheme: String, val entryId: Long, val categoryId: Long) : BaseDialog(context) {

    private var saveSchemeJump = true

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_scheme_jump_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        tvJump.setOnClickListener {
            saveSchemeJumpIfNecessary()
            SchemeJumper.jumpByScheme(context, scheme)
            dismiss()
        }

        tvCancel.setOnClickListener {
            saveSchemeJumpIfNecessary()
            dismiss()
        }

        llSaveStatus.setOnClickListener {
            saveSchemeJump = !saveSchemeJump
            if (saveSchemeJump) {
                ivSaveStatus.setImageResource(R.drawable.app_select_press)
            } else {
                ivSaveStatus.setImageResource(R.drawable.app_select_normal)
            }
        }
    }

    private fun saveSchemeJumpIfNecessary() {
        if (saveSchemeJump) {
            ToastUtil.toast("已保存跳转外链，下次可在收藏卡片直接跳转")
            val entryBean = DataService.getEntryList(categoryId)?.findByCondition { it.date == entryId }
            entryBean?.let {
                // 只有链接才能保存scheme
                (entryBean as? LinkEntryBean)?.let {
                    it.schemeJump = scheme
                    DataService.updateEntry(entryBean, entryBean)
                }
            }
        }
    }

}
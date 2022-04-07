package com.app.dixon.facorites.core.view

import android.content.Context
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.shakeTip
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_create_category_content.*

/**
 * 创建分类（收藏夹）的弹窗
 */
class CreateCategoryDialog(context: Context, private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！")) :
    BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_create_category_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        tvCreate.setOnClickListener {
            val text = etInput.text.toString()
            if (text.isNotEmpty()) {
                DataService.createCategory(text) {
                    if (it != -1L) {
                        ToastUtil.toast("创建收藏夹成功")
                    }
                }
                dismiss()
            } else {
                etInput.shakeTip()
            }
        }
    }
}
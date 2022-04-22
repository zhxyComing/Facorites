package com.app.dixon.facorites.core.view

import android.content.Context
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.shakeTip
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_update_category_content.*

/**
 * 创建分类（收藏夹）的弹窗
 */
class UpdateCategoryDialog(context: Context, val categoryInfoBean: CategoryInfoBean) :
    BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_update_category_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        etInput.setText(categoryInfoBean.name)
        tvUpdate.setOnClickListener {
            val newTitle = etInput.text.toString()
            if (newTitle.isEmpty()) {
                etInput.shakeTip()
                return@setOnClickListener
            }
            val newCategoryInfoBean = CategoryInfoBean(categoryInfoBean.id, newTitle)
            DataService.updateCategory(categoryInfoBean, newCategoryInfoBean) {
                if (it != -1L) {
                    ToastUtil.toast("更新收藏夹成功")
                }
                dismiss()
            }
        }
    }
}
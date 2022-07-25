package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.show
import com.app.dixon.facorites.core.util.normalFont
import com.dixon.dlibrary.util.ScreenUtil
import kotlinx.android.synthetic.main.app_dialog_category_edit_content.*

// 选择分类操作的提醒弹窗
class EditCategoryDialog(context: Context, val categoryInfoBean: CategoryInfoBean) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_category_edit_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    @SuppressLint("SetTextI18n")
    override fun initDialog() {
        llContainer.normalFont()
        tvDelete.setOnClickListener {
            // 打开删除提醒弹窗
            DeleteCategoryDialog(context, categoryInfoBean).show()
            dismiss()
        }

        tvUpdate.setOnClickListener {
            // 打开更新弹窗
            UpdateCategoryDialog(context, categoryInfoBean).show()
            dismiss()
        }

        tvTop.setOnClickListener {
            val newCategoryInfoBean = CategoryInfoBean(categoryInfoBean.id, categoryInfoBean.name, categoryInfoBean.bgPath, topTimeMs = System.currentTimeMillis())
            DataService.updateCategory(categoryInfoBean, newCategoryInfoBean)
            dismiss()
        }

        if (categoryInfoBean.topTimeMs != 0L) {
            tvCancelTop.show()
            tvCancelTop.setOnClickListener {
                val newCategoryInfoBean = CategoryInfoBean(categoryInfoBean.id, categoryInfoBean.name, categoryInfoBean.bgPath, topTimeMs = 0L)
                DataService.updateCategory(categoryInfoBean, newCategoryInfoBean)
                dismiss()
            }
        }
    }
}
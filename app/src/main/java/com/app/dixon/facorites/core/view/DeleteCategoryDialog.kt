package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.service.DataService
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_category_delete_content.*

// 删除分类的提醒弹窗
class DeleteCategoryDialog(context: Context, val categoryInfoBean: CategoryInfoBean) : BaseDialog(context) {

    private var saveSchemeJump = true

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_category_delete_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    @SuppressLint("SetTextI18n")
    override fun initDialog() {
        tvTipTitle.text = "确认删除${categoryInfoBean.name}?"
        tvDelete.setOnClickListener {
            if (DataService.getCategoryList().size == 1) {
                ToastUtil.toast("请至少保留一个收藏夹！")
                dismiss()
                return@setOnClickListener
            }
            // 注意回调里的方法不能使用 data[position]，因为位置可能变（比如增删），但是回调里用的是 final 数据是不变的。
            DataService.deleteCategory(categoryInfoBean.id) {
                if (it != -1L) {
                    ToastUtil.toast("删除分类成功！")
                }
            }
            dismiss()
        }

        tvCancel.setOnClickListener {
            dismiss()
        }
    }
}
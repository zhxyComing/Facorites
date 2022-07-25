package com.app.dixon.facorites.core.view

import android.content.Context
import android.net.Uri
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.ImageEntryBean
import com.app.dixon.facorites.core.data.service.BitmapIOService
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.setImageByUri
import com.app.dixon.facorites.core.ex.shakeTip
import com.app.dixon.facorites.core.util.ImageSelectHelper
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.page.category.event.CategoryImageCompleteEvent
import com.app.dixon.facorites.page.home.CATEGORY_BG_IMAGE_REQUEST
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_update_category_content.*
import kotlinx.android.synthetic.main.app_dialog_update_category_content.bgView
import kotlinx.android.synthetic.main.app_dialog_update_category_content.etInput
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

/**
 * 创建分类（收藏夹）的弹窗
 */
class UpdateCategoryDialog(context: Context, val categoryInfoBean: CategoryInfoBean) :
    BaseDialog(context) {

    private var bgUri: Uri? = null

    private var hasSave: Boolean = false

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_update_category_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        EventBus.getDefault().register(this)
        flContainer.normalFont()
        etInput.setText(categoryInfoBean.name)
        tvUpdate.setOnClickListener {
            val newTitle = etInput.text.toString()
            val newBgUri = bgUri
            if (newTitle.isEmpty()) {
                etInput.shakeTip()
                return@setOnClickListener
            }
            val newCategoryInfoBean = CategoryInfoBean(categoryInfoBean.id, newTitle, newBgUri?.path, categoryInfoBean.topTimeMs)
            DataService.updateCategory(categoryInfoBean, newCategoryInfoBean) {
                if (it != -1L) {
                    ToastUtil.toast("更新收藏夹成功")
                }
                hasSave = true
                dismiss()
            }
        }
        categoryInfoBean.bgPath?.let {
            bgUri = Uri.fromFile(File(it))
            bgView.setImageByUri(bgUri)
        }
        selectImage.setOnClickListener {
            // 打开图片选择
            ImageSelectHelper.openImageSelectPage(CATEGORY_BG_IMAGE_REQUEST)
        }
    }

    override fun onDetachedFromWindow() {
        if (!hasSave) {
            deleteExpiredImportImage()
        }
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onImageSelectComplete(event: CategoryImageCompleteEvent) {
        deleteExpiredImportImage()
        bgUri = event.uri
        bgView.setImageByUri(bgUri)
    }

    // 删除过期的导入图片
    private fun deleteExpiredImportImage() {
        bgUri?.path?.let {
            var deleteExpiredImage = true
            categoryInfoBean.bgPath?.let { bg ->
                // 带入更新的图片在确认修改之前不删除
                if (bg == it) {
                    deleteExpiredImage = false
                }
            }
            if (deleteExpiredImage) {
                BitmapIOService.deleteBitmap(it)
            }
        }
    }
}
package com.app.dixon.facorites.core.view

import android.content.Context
import android.net.Uri
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.FileIOService
import com.app.dixon.facorites.core.ex.setImageByUri
import com.app.dixon.facorites.core.ex.shakeTip
import com.app.dixon.facorites.core.util.ImageSelectHelper
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.page.category.event.CategoryImageCompleteEvent
import com.app.dixon.facorites.page.home.CATEGORY_BG_IMAGE_REQUEST
import com.dixon.dlibrary.util.Ln
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_create_category_content.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


/**
 * 创建分类（收藏夹）的弹窗
 */
class CreateCategoryDialog(context: Context, val belongTo: Long? = null, private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！")) :
    BaseDialog(context) {

    private var bgUri: Uri? = null

    private var hasSave: Boolean = false

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_create_category_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        EventBus.getDefault().register(this)
        flContainer.normalFont()
        tvCreate.setOnClickListener {
            val text = etInput.text.toString()
            if (text.isNotEmpty()) {
                belongTo?.let {
                    val id = Date().time
                    val tempCategoryInfoBean = CategoryInfoBean(id, text, bgUri?.path, belongTo = it)
                    DataService.createEntry(CategoryEntryBean(tempCategoryInfoBean, id, belongTo), object : Callback<BaseEntryBean> {
                        override fun onSuccess(data: BaseEntryBean) {
                            ToastUtil.toast("创建收藏夹成功")
                        }

                        override fun onFail(msg: String) {
                            ToastUtil.toast("创建收藏夹失败")
                        }
                    })
                } ?: let {
                    DataService.createCategory(text, bgUri?.path) {
                        if (it != -1L) {
                            ToastUtil.toast("创建收藏夹成功")
                        }
                    }
                }
                hasSave = true
                dismiss()
            } else {
                etInput.shakeTip()
            }
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
        Ln.i("onImageSelect", "Complete ${event.uri}")
        deleteExpiredImportImage()
        bgUri = event.uri
        bgView.setImageByUri(bgUri)
    }

    // 删除过期的导入图片
    private fun deleteExpiredImportImage() {
        bgUri?.path?.let {
            FileIOService.deleteFile(it)
        }
    }
}
package com.app.dixon.facorites.core.view

import android.content.Context
import android.graphics.Bitmap
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.ImageEntryBean
import com.app.dixon.facorites.core.data.service.BitmapIOService
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.findIndexByCondition
import com.app.dixon.facorites.core.ex.shakeTip
import com.app.dixon.facorites.core.ex.shakeTipIfEmpty
import com.app.dixon.facorites.core.util.ImageSelectHelper
import com.app.dixon.facorites.core.util.normalFont
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_create_entry_content.*
import java.util.*

// 创建用构造函数
class CreateImageEntryDialog(
    context: Context,
    private val bitmap: Bitmap,
    private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！"),
    private val defaultCategory: Long? = null
) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_create_image_entry_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    // 选图
    private var imagePath: String? = null

    // 图片转存状态
    private var imageImporting: Boolean = false

    // 是否点击了确认
    private var hasSave = false

    override fun initDialog() {
        initCommonLogic()
    }

    private fun initCommonLogic() {
        llContainer.normalFont()

        saveBitmapToLocal()

        tvCreate.setOnClickListener {
            saveImage()
        }
        // 分类的下拉列表
        initSpinner()
    }

    private fun saveBitmapToLocal() {
        val absolutePath = BitmapIOService.createBitmapSavePath()
        tvTip.text = "转存图片中，请耐心等待"
        imageImporting = true
        selectImage.isEnabled = false
        BitmapIOService.saveBitmap(absolutePath, bitmap, object : Callback<String> {
            override fun onSuccess(data: String) {
                imageImporting = false
                imagePath = data
                bgView.setImageBitmap(bitmap)
                tvTip.text = ""
                selectImage.isEnabled = true
            }

            override fun onFail(msg: String) {
                ToastUtil.toast("图片转存失败，无法添加收藏")
                imageImporting = false
                tvTip.text = ""
                selectImage.isEnabled = true
            }
        })
    }

    // 保存图片
    private fun saveImage() {
        val title = etImageTitle.text.toString()
        val path = imagePath
        val categoryId = categoryChoose.getSelectionData<CategoryInfoBean>()?.id
        if (title.isNotEmpty() && !path.isNullOrEmpty() && !imageImporting && categoryId != null) {
            DataService.createEntry(
                ImageEntryBean(
                    path = path,
                    title = title,
                    date = Date().time,
                    belongTo = categoryId
                ),
                callback
            )
            hasSave = true
            dismiss()
        } else {
            etImageTitle.shakeTipIfEmpty()
            // 没选图或者导入过程中均不允许创建或更新
            if (path.isNullOrEmpty() || imageImporting) {
                selectImage.shakeTip()
            }
        }
    }

    // 下拉选择框
    private fun initSpinner() {
        val expendInfoList = mutableListOf<CustomSpinner.ExpandInfo<CategoryInfoBean>>()
        DataService.getCategoryList().forEach {
            expendInfoList.add(CustomSpinner.ExpandInfo(it.name, it.bgPath, it))
        }
        categoryChoose.setData(expendInfoList)
        categoryChoose.setShowPos(CustomSpinner.ShowPos.TOP)
        defaultCategory?.let { id ->
            expendInfoList.findIndexByCondition {
                it.data.id == id
            }?.let { index ->
                categoryChoose.setSelection(index)
            }
        }
    }

    override fun onDetachedFromWindow() {
        if (!hasSave) {
            deleteExpiredImportImage()
        }
        super.onDetachedFromWindow()
    }

    // 删除过期的导入图片
    private fun deleteExpiredImportImage() {
        imagePath?.let {
            BitmapIOService.deleteBitmap(it)
        }
    }
}
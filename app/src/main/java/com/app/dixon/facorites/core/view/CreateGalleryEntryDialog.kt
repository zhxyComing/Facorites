package com.app.dixon.facorites.core.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.GalleryEntryBean
import com.app.dixon.facorites.core.data.service.BitmapIOService
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.ex.shakeTip
import com.app.dixon.facorites.core.ex.shakeTipIfEmpty
import com.app.dixon.facorites.core.util.ImageSelectHelper
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.page.gallery.adapter.GalleryImportAdapter
import com.dixon.dlibrary.util.Ln
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_create_gallery_entry_content.*
import java.util.*

/**
 * 从外部分享创建Gallery
 * 与传统创建的区别的是图片还没导入，都是uri，而且不能导入新图
 */
class CreateGalleryEntryDialog(
    context: Context,
    private val import: List<Uri>,
    private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！")
) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_create_gallery_entry_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    // 是否点击了保存
    private var hasSave = false
    private var tempGalleryPath = mutableListOf<String>() // 新导入的临时图片 如果后续选择取消 则要删除掉这些新导入的图

    override fun initDialog() {
        llContainer.normalFont()
        tvCreate.setOnClickListener {
            saveGallery()
        }
        // 分类的下拉列表
        initSpinner()
        // 初始化列表
        initGalleryLayout()

        backUi {
            // 开始导入图片
            importGallery()
        }
    }

    private fun importGallery() {
        val dialog = ProgressDialog(context, "图片导入中..").apply { show() }
        val max = import.size
        var progress = 0f
        import.forEach { uri ->
            // 图片信息
            var rotate = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.contentResolver.openInputStream(uri)?.let {
                    val imageRotation = ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                    if (imageRotation == ExifInterface.ORIENTATION_ROTATE_90 || imageRotation == ExifInterface.ORIENTATION_ROTATE_270) {
                        rotate = true
                    }
                }
            }
            // 转存图片到本地
            var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            // iOS 拍出的图片带旋转角，要在导入时转为旋转后的图片
            if (rotate) {
                val m = Matrix()
                m.setRotate(90f, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
                try {
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                } catch (ex: OutOfMemoryError) {
                    Ln.e("OutOfMemoryError", "转存图片OOM")
                }
            }
            val absolutePath = BitmapIOService.createBitmapSavePath()
            BitmapIOService.saveBitmap(absolutePath, bitmap, object : Callback<String> {
                override fun onSuccess(data: String) {
                    tempGalleryPath.add(data)
                    progress++
                    dialog.setProgress((progress / max * 100).toInt())
                    if (progress.toInt() == max) {
                        dialog.dismiss()
                        ToastUtil.toast("图片导入完成")
                        rvGalleryList.adapter?.notifyDataSetChanged()
                    }
                }

                override fun onFail(msg: String) {
                    // 导入失败，删除存图的文件
                    BitmapIOService.deleteBitmap(absolutePath)
                    progress++
                    dialog.setProgress((progress / max * 100).toInt())
                    if (progress.toInt() == max) {
                        dialog.dismiss()
                        ToastUtil.toast("图片导入完成")
                        rvGalleryList.adapter?.notifyDataSetChanged()
                    }
                }
            })
        }
    }

    // 保存图片
    private fun saveGallery() {
        val title = etGalleryTitle.text.toString()
        val categoryId = categoryChoose.getSelectionData<CategoryInfoBean>()?.id
        if (title.isNotEmpty() && categoryId != null && tempGalleryPath.isNotEmpty()) {
            DataService.createEntry(
                GalleryEntryBean(
                    path = tempGalleryPath,
                    title = title,
                    date = Date().time,
                    belongTo = categoryId,
                ),
                callback
            )
            hasSave = true
            dismiss()
        } else {
            // 未填数据提示
            etGalleryTitle.shakeTipIfEmpty()
            // 没选图或者导入过程中均不允许创建或更新
            if (tempGalleryPath.isEmpty()) {
                rvGalleryList.shakeTip()
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
    }

    override fun onDetachedFromWindow() {
        if (!hasSave) {
            deleteExpiredGalleryImage()
        }
        super.onDetachedFromWindow()
    }

    // 删除过期的导入图片集
    private fun deleteExpiredGalleryImage() {
        tempGalleryPath.forEach {
            BitmapIOService.deleteBitmap(it)
        }
    }

    // 图片集类型
    private fun initGalleryLayout() {
        tvGalleryLayoutTip.setOnClickListener {
            TipDialog(
                context,
                content = "导入收藏夹子的图片有以下特性：\n\n1.应用外（如相册）不可见；\n" +
                        "\n2.删除手机（相册）的原图不会影响到收藏图。",
                title = "图片集收藏提示"
            ).show()
        }
        rvGalleryList.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        rvGalleryList.adapter = GalleryImportAdapter(
            context,
            tempGalleryPath,
            addClickAction = {
                // 打开图片选择
                ImageSelectHelper.openGallerySelectPage(ENTRY_GALLERY_REQUEST)
            },
            removeClickAction = {
                // 移除Item
                val removePath = tempGalleryPath.removeAt(it) // 删除galleryPath的数据，Adapter.data也会同步变化
                rvGalleryList.adapter?.notifyItemRemoved(it)
                // 临时导入的文件才删除
                // 如果是更新，则会带入图片，这些图片只有在确认更新时由DataService负责删除
                // 原则是：已保存的图片由数据管理器（DataService）负责删除，临时图片是业务产生的，由业务方（CreateEntryDialog）删除
                if (tempGalleryPath.contains(removePath)) {
                    BitmapIOService.deleteBitmap(removePath)
                    tempGalleryPath.remove(removePath)
                }
            },
            hideFooter = true
        )
    }
}
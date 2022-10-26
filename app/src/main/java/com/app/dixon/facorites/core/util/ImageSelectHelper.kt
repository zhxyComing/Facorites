package com.app.dixon.facorites.core.util

import android.app.Activity
import android.content.Intent
import android.content.Intent.EXTRA_ALLOW_MULTIPLE
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseApplication
import com.app.dixon.facorites.core.bean.CropInfo
import com.app.dixon.facorites.page.crop.CropActivity
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCrop.REQUEST_CROP
import com.yalantis.ucrop.UCropActivity
import java.io.File

/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：图片选择器
 * 创建人：xuzheng
 * 创建时间：2022/4/24 15:52
 */

object ImageSelectHelper {

    // 打开图片选择页
    fun openImageSelectPage(requestCode: Int) {
        // 使用意图直接调用手机相册
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        )
        // 打开手机相册,设置请求码
        BaseApplication.currentActivity.get()?.startActivityForResult(intent, requestCode)
    }

    // 打开图片选择器 可多选
    fun openGallerySelectPage(requestCode: Int) {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        // 打开手机相册,设置请求码
        BaseApplication.currentActivity.get()?.startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode)
    }

    //  打开图片裁剪页
    fun openImageCropPage(context: Activity, data: Uri, outputPath: String, cropInfo: CropInfo, requestCode: Int = REQUEST_CROP) {
        val intent = UCrop.of(data, Uri.fromFile(File(outputPath)))
            .withAspectRatio(cropInfo.aspectX, cropInfo.aspectY)
            .withMaxResultSize(cropInfo.outputX, cropInfo.outputY)
            .withOptions(UCrop.Options().apply {
                setToolbarTitle("裁剪")
                setToolbarColor(ContextCompat.getColor(context, R.color.black))
                setStatusBarColor(ContextCompat.getColor(context, R.color.black))
                setToolbarWidgetColor(ContextCompat.getColor(context, R.color.white))
            })
            .getIntent(context)
        // 替换为启动自己的裁剪页 方便定制UI
        intent.setClass(context, CropActivity::class.java)
        context.startActivityForResult(intent, requestCode)
    }
}
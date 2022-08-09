package com.app.dixon.facorites.base

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.bean.CropInfo
import com.app.dixon.facorites.core.data.service.BitmapIOService
import com.app.dixon.facorites.core.data.service.base.DocumentFileUtils
import com.app.dixon.facorites.core.ex.dp
import com.app.dixon.facorites.core.util.ImageSelectHelper
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.view.ENTRY_IMAGE_REQUEST
import com.app.dixon.facorites.page.category.event.CategoryImageCompleteEvent
import com.app.dixon.facorites.page.home.CATEGORY_BG_IMAGE_REQUEST
import com.dixon.dlibrary.util.StatusBarUtil
import com.yalantis.ucrop.UCrop
import org.greenrobot.eventbus.EventBus


/**
 * 全路径：com.app.dixon.facorites.base
 * 类描述：BaseActivity
 * 创建人：xuzheng
 * 创建时间：3/17/22 7:37 PM
 */
open class BaseActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ln.i("BaseActivity", "onCreate $this")
        val window: Window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (useStatusTransparent()) {
                Ln.i("StatusBar", "path A")
                StatusBarUtil.setColorForStatus(this)
            } else {
                Ln.i("StatusBar", "path B")
                val decorView = window.decorView
                val wic = WindowInsetsControllerCompat(window, decorView)
                wic.isAppearanceLightStatusBars = true
                window.statusBarColor = resources.getColor(statusBarColor(), null)
            }
        } else {
            Ln.i("StatusBar", "path C")
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        // 隐藏底部横条 navigation bar
        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Ln.i("BaseActivity", "onDestroy $this")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Ln.i("BaseActivity", "onNewIntent $this")
    }

    open fun statusBarColor(): Int = R.color.app_background_color

    // 透明沉浸式状态栏 状态栏不再占空间 同时颜色也变成半透明
    open fun useStatusTransparent(): Boolean = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            DocumentFileUtils.askPermissionCallback(contentResolver, requestCode, data)
            if (requestCode == CATEGORY_BG_IMAGE_REQUEST) {
                // 1.分类背景图选择完成
                it.data?.let { uri ->
                    openCategoryBgCrop(uri)
                }
            } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
                // 2.1 裁剪成功
                UCrop.getOutput(it)?.let { resultUri ->
                    Ln.i("CropResult", "success $resultUri")
                    EventBus.getDefault().post(CategoryImageCompleteEvent(resultUri))
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                // 2.2 裁剪失败
                val cropError = UCrop.getError(it)
                Ln.i("CropResult", "fail ${cropError.toString()}")
            } else if (requestCode == ENTRY_IMAGE_REQUEST) {
                // 图片收藏选图成功
                it.data?.let { uri ->
                    Ln.i("ImageResult", "$uri")
                    EventBus.getDefault().post(CategoryImageCompleteEvent(uri))
                }
            } else {
                // do nothing
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // 跳转分类背景图裁剪
    private fun openCategoryBgCrop(uri: Uri) {
        Ln.i("openCategoryBgCrop", "${400.dp} ${100.dp}")
        ImageSelectHelper.openImageCropPage(
            this, uri,
            BitmapIOService.createBitmapSavePath(),
            CropInfo(aspectX = 3f, aspectY = 1f, outputX = 390.dp, outputY = 130.dp)
        )
    }
}
package com.app.dixon.facorites.base

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.service.base.DocumentFileUtils
import com.app.dixon.facorites.core.util.Ln
import com.dixon.dlibrary.util.StatusBarUtil


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
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        DocumentFileUtils.askPermissionCallback(contentResolver, requestCode, data)
    }
}
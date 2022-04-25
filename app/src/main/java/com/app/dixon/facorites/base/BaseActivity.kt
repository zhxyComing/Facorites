package com.app.dixon.facorites.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
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
        StatusBarUtil.setColorForStatus(this)
        // 隐藏底部横条 navigation bar.
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
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
}
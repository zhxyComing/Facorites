package com.app.dixon.facorites.core.function.fromshare

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import com.app.dixon.facorites.base.BaseApplication
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.ex.tryExtractHttp
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.view.CreateEntryDialog
import com.dixon.dlibrary.util.ToastUtil

/**
 * 全路径：com.app.dixon.facorites.core.function.fromshare
 * 类描述：从其它应用分享到本App
 * 创建人：xuzheng
 * 创建时间：3/17/22 7:40 PM
 */
class FromShareHelper {

    fun parse(intent: Intent) {
        when {
            intent.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                } else if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent) // Handle single image being sent
                }
            }
            intent.action == Intent.ACTION_SEND_MULTIPLE
                    && intent.type?.startsWith("image/") == true -> {
                handleSendMultipleImages(intent) // Handle multiple images being sent
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            Ln.i("FromShare", it)
            ToastUtil.toast(it)
            // 获取到链接 走创建流程
            BaseApplication.currentActivity.get()?.let { context ->
                CreateEntryDialog(context, it.tryExtractHttp()).show()
            }
            // 记得清数据 以防跳转到其它页面退回后再次触发
            intent.removeExtra(Intent.EXTRA_TEXT)
        }
    }

    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            // Update UI to reflect image being shared
        }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            // Update UI to reflect multiple images being shared
        }
    }
}
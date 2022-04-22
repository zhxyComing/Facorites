package com.app.dixon.facorites.page.browse

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dixon.dlibrary.util.ToastUtil
import java.lang.Exception
import java.net.URISyntaxException

/**
 * 全路径：com.app.dixon.facorites.page.browse
 * 类描述：根据协议跳转到外部App
 * 创建人：xuzheng
 * 创建时间：2022/4/21 15:54
 */
object SchemeJumper {

    @SuppressLint("QueryPermissionsNeeded")
    fun jumpByScheme(context: Context, link: String): Boolean {
        try {
            // intent协议
            if (link.startsWith("intent://")) {
                val intent: Intent
                try {
                    intent = Intent.parseUri(link, Intent.URI_INTENT_SCHEME)
                    intent.addCategory("android.intent.category.BROWSABLE")
                    intent.component = null
                    intent.selector = null
                    val resolves = context.packageManager.queryIntentActivities(intent, 0)
                    if (resolves.size > 0) {
                        context.startActivity(intent)
                    }
                    return true
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }
            }
            // 处理自定义scheme协议
            if (!link.startsWith("http")) {
                try {
                    // 以下固定写法
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // 防止没有安装的情况
                    e.printStackTrace()
                    ToastUtil.toast("您所打开的第三方App未安装！")
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun isSchemeJumpLink(link: String): Boolean = !link.startsWith("http")
}
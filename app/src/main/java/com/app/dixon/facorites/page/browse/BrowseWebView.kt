package com.app.dixon.facorites.page.browse

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.*
import com.dixon.dlibrary.util.Ln
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.activity_browse.view.*

/**
 * 全路径：com.app.dixon.facorites.page.browse
 * 类描述：WebView
 * 创建人：xuzheng
 * 创建时间：4/15/22 8:37 PM
 *
 * 注意点：
 * 1. 不能使用三参数的构造函数，否则输入法无响应；
 * 2. 图片加载不出来，可能是Android9以上不支持Http导致的；
 */
@SuppressLint("SetJavaScriptEnabled")
class BrowseWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {

    init {
        // 注意：Android 9.0 开始，应用包括WebView只能请求https数据，除非Application添加android:usesCleartextTraffic="true"
        // https://blog.csdn.net/qq_32534441/article/details/103529449
        // 将图片调整到适合WebView的大小
        settings.useWideViewPort = true
        // 缩放至屏幕的大小
        settings.loadWithOverviewMode = true
        // 启用缩放功能
        settings.setSupportZoom(true)
        // 使用WebView内置的缩放功能
        settings.builtInZoomControls = true
        // 隐藏屏幕中的虚拟缩放按钮
        settings.displayZoomControls = false
        // 允许Https链接加载Http图片
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        settings.blockNetworkImage = false
        // 支持JS
        settings.javaScriptEnabled = true
        // 支持通过JS打开新窗口
        settings.javaScriptCanOpenWindowsAutomatically = true
        // 设置可以访问文件
        settings.allowFileAccess = true
        // 支持自动加载图片
        settings.loadsImagesAutomatically = true
        // 设置编码格式
        settings.defaultTextEncodingName = "utf-8"
        // 是否开启本地DOM存储 防止镶嵌h5页面加载显示白板
        settings.domStorageEnabled = true
        // 监听
        webViewClient = CustomWebViewClient()
        // 下载监听
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            // TODO 下载器
            ToastUtil.toast("暂不支持下载哦～")
        }
    }

    private inner class CustomWebViewClient : WebViewClient() {

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            Ln.i("WebViewRequest", "start ${request?.url.toString()}")
            return super.shouldInterceptRequest(view, request)
        }

        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            Ln.i("WebViewRequest", "error ${request?.url.toString()}")
            super.onReceivedHttpError(view, request, errorResponse)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            if (!hasFocus()) {
                Ln.i("WebViewRequest", "RequestFocus")
                requestFocus()
            }
        }
        return super.onTouchEvent(event)
    }
}
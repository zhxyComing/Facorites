package com.app.dixon.facorites.page.browse

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.BROWSE_LINK
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.ex.*
import com.dixon.dlibrary.util.FontUtil
import com.dixon.dlibrary.util.Ln
import kotlinx.android.synthetic.main.activity_browse.*


class BrowseActivity : BaseActivity() {

    private lateinit var link: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        FontUtil.font(window.decorView)
        parseIntentData()

        initView()
        initData()
    }

    private fun initView() {
        initWebView()
        initTitle()

        // 关闭按钮
        ivClose.setOnClickListener {
            finish()
        }
    }

    private fun initData() {
        webView.loadUrl(link)
    }

    private fun initTitle() {
        JSoupService.askTitle(link.try2URL(), { data ->
            tvTitle.text = data
        }, {
            tvTitle.text = link
        })
    }


    private fun initWebView() {
        webView.webChromeClient = CustomWebChromeClient()
    }

    private fun parseIntentData() {
        intent.getStringExtra(BROWSE_LINK)?.let {
            link = it
        } ?: finish()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private inner class CustomWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            // 进度条
            progressBar.progress = newProgress
            if (newProgress == 100) {
                progressBar.invisible()
            } else {
                if (progressBar.visibility == View.INVISIBLE) {
                    progressBar.show()
                }
            }
        }
    }
}
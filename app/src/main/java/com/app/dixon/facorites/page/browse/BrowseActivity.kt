package com.app.dixon.facorites.page.browse

import android.os.Bundle
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.BROWSE_LINK
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.ex.try2URL
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
        initTitle()
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

    override fun finish() {
        Ln.i("WebViewRequest", "CLEAR!!!")
        webView.clearCache(true)
        super.finish()
    }
}
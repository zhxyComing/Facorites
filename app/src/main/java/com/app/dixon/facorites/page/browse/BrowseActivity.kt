package com.app.dixon.facorites.page.browse

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.BROWSE_LINK
import com.app.dixon.facorites.core.common.CATEGORY_ID
import com.app.dixon.facorites.core.common.ENTRY_ID
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.ex.*
import com.dixon.dlibrary.util.AnimationUtil
import com.dixon.dlibrary.util.FontUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.activity_browse.*

class BrowseActivity : BaseActivity() {

    private var entryId: Long = 0
    private var categoryId: Long = 0
    private lateinit var link: String
    private var saveSchemeJump = true

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
        webView.setOnSchemeJumpListener { scheme ->
            if (schemeJumpLayout.isGone()) {
                showSchemeJumpLayout()
                initSchemeJumpLayout(scheme)
                // 8s后自动消失
                backUi(8000) {
                    hideSchemeJumpLayout()
                }
            }
        }
    }

    private fun initSchemeJumpLayout(scheme: String) {
        setSchemeSaveIcon()
        tvJump.setOnClickListener {
            saveSchemeJumpIfNecessary(scheme)
            SchemeJumper.jumpByScheme(this, scheme)
            hideSchemeJumpLayout()
        }

        tvCancel.setOnClickListener {
            saveSchemeJumpIfNecessary(scheme)
            hideSchemeJumpLayout()
        }

        llSaveStatus.setOnClickListener {
            saveSchemeJump = !saveSchemeJump
            setSchemeSaveIcon()
        }
    }

    private fun setSchemeSaveIcon() {
        if (saveSchemeJump) {
            ivSaveStatus.setImageResource(R.drawable.app_select_press)
        } else {
            ivSaveStatus.setImageResource(R.drawable.app_select_normal)
        }
    }

    private fun saveSchemeJumpIfNecessary(scheme: String) {
        if (saveSchemeJump) {
            ToastUtil.toast("已保存跳转外链，下次可在收藏卡片直接跳转")
            val entryBean = DataService.getEntryList(categoryId)?.findByCondition { it.date == entryId }
            entryBean?.let {
                (entryBean as? LinkEntryBean)?.let {
                    it.schemeJump = scheme
                    DataService.updateEntry(entryBean, entryBean)
                }
            }
        }
    }

    private fun parseIntentData() {
        intent.getStringExtra(BROWSE_LINK)?.let {
            link = it
        } ?: finish()
        entryId = intent.getLongExtra(ENTRY_ID, 0)
        categoryId = intent.getLongExtra(CATEGORY_ID, 0)
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

    private fun showSchemeJumpLayout() {
        schemeJumpLayout.show()
        AnimationUtil.tranY(schemeJumpLayout, 900f, 0f, 300L, DecelerateInterpolator(), null).start()
    }

    private fun hideSchemeJumpLayout() {
        AnimationUtil.tranY(schemeJumpLayout, 0f, 900f, 300L, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                schemeJumpLayout.hide()
            }
        }).start()
    }
}
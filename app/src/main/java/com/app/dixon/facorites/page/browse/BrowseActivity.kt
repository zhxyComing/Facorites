package com.app.dixon.facorites.page.browse

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.PopupWindow
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.BROWSE_LINK
import com.app.dixon.facorites.core.common.CATEGORY_ID
import com.app.dixon.facorites.core.common.ENTRY_ID
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.ClipUtil
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.core.view.ClipSaveDialog
import com.app.dixon.facorites.core.view.CreateEntryDialog
import com.dixon.dlibrary.util.AnimationUtil
import com.dixon.dlibrary.util.FontUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.activity_browse.*
import kotlinx.android.synthetic.main.app_content_web_pop.view.*


class BrowseActivity : BaseActivity() {

    private var entryId: Long = 0
    private var categoryId: Long = 0
    private lateinit var link: String
    private var saveSchemeJump = true

    private val clipboardDog = ClipboardDog()

    private lateinit var morePop: PopupWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        normalFont()
        parseIntentData()
        if (link.isEmpty()) {
            // TODO 后续修改为进入首页
            finish()
            return
        }

        initView()
        initData()
        initClipService()
    }

    // 监听浏览器的复制/剪切操作
    private fun initClipService() {
        clipboardDog.register { content ->
            ContextAssistant.asContext { context ->
                // 是链接的话 则弹出收藏窗口
                if (content.startsWith("http")) {
                    CreateEntryDialog(context, content.tryExtractHttpByMatcher()).show()
                    return@asContext
                }
                // 否则弹出笔记窗口
                // 复制/剪切回调
                ClipSaveDialog(context, content, entryId).show()
            }
        }
    }

    /**
     * 注销监听，避免内存泄漏。
     */
    override fun onDestroy() {
        super.onDestroy()
        clipboardDog.unregister()
    }

    private fun initView() {
        initWebView()
        initTitle()
        initMorePop()

        // 关闭按钮
        ivClose.setOnClickListener {
            finish()
        }

        // 更多按钮
        ivMore.setOnClickListener {
            morePop.showAsDropDown(ivMore)
        }
    }

    private fun initMorePop() {
        val contentView: View = LayoutInflater.from(this).inflate(R.layout.app_content_web_pop, null)
        // 添加至收藏
        contentView.llSave.setOnClickListener {
            ContextAssistant.asContext { context ->
                CreateEntryDialog(context, webView.url).show()
            }
        }
        contentView.llCopy.setOnClickListener {
            clipboardDog.shield {
                ClipUtil.copyToClip(this, webView.url.toString())
                ToastUtil.toast(webView.url.toString())
            }
        }
        morePop = PopupWindow(this)
        morePop.contentView = contentView
        morePop.width = ViewGroup.LayoutParams.WRAP_CONTENT
        morePop.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // popWindow 显示时，点击外部优先关闭 window
        morePop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        morePop.isOutsideTouchable = true
        morePop.isTouchable = true
//        morePop.isFocusable = true
    }

    private fun initData() {
        webView.loadUrl(link)
    }

    private fun initTitle() {
        JSoupService.askTitle(link.try2URL(), { data ->
            tvTitle.text = data
            tvTitle.show()
            AnimationUtil.alpha(tvTitle, 0f, 1f).start()
        }, {
            tvTitle.text = link
            tvTitle.show()
            AnimationUtil.alpha(tvTitle, 0f, 1f).start()
        })
    }

    private fun initWebView() {
        webView.webChromeClient = CustomWebChromeClient()
        webView.setOnSchemeJumpListener { scheme ->
            if (schemeJumpLayout.isGone()) {
                // 有的网站会在打开App的时候复制内容 比如B站
                clipboardDog.shield {
                    showSchemeJumpLayout()
                    initSchemeJumpLayout(scheme)
                }
                // 5s后自动消失
                backUi(5000) {
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
            tvCancel.text = "取消，仅保存快捷方式"
            tvJump.text = "前往，并保存快捷方式"
        } else {
            ivSaveStatus.setImageResource(R.drawable.app_select_normal)
            tvCancel.text = "取消"
            tvJump.text = "前往"
        }
    }

    private fun saveSchemeJumpIfNecessary(scheme: String) {
        if (saveSchemeJump) {
            ToastUtil.toast("已保存跳转外链，下次可在收藏卡片直接跳转")
            val entryBean = DataService.getEntryList(categoryId)?.findByCondition { it.date == entryId }
            entryBean?.let {
                // 只有链接才能更新scheme
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
        } ?: let {
            link = ""
        }
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
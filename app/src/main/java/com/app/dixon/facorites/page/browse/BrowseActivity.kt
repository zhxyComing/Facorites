package com.app.dixon.facorites.page.browse

import ShareUtil
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
import com.app.dixon.facorites.core.common.BROWSE_TITLE
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
    private var appointTitle: String? = null
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
            // TODO ???????????????????????????
            finish()
            return
        }

        initView()
        initData()
        initClipService()
    }

    // ????????????????????????/????????????
    private fun initClipService() {
        clipboardDog.register { content ->
            ContextAssistant.asContext { context ->
                // ??????????????? ?????????????????????
                if (content.startsWith("http")) {
                    CreateEntryDialog(context, content.tryExtractHttpByMatcher()).show()
                    return@asContext
                }
                if (entryId != 0L) {
                    // ????????????????????????
                    // ??????/????????????
                    ClipSaveDialog(context, content, entryId).show()
                }
            }
        }
    }

    /**
     * ????????????????????????????????????
     */
    override fun onDestroy() {
        super.onDestroy()
        clipboardDog.unregister()
    }

    private fun initView() {
        initWebView()
        initTitle()
        initMorePop()

        // ????????????
        ivClose.setOnClickListener {
            finish()
        }

        // ????????????
        ivMore.setOnClickListener {
            morePop.showAsDropDown(ivMore)
        }

        // ????????????????????????
        ivCloseSchemeJumpLayout.setOnClickListener {
            hideSchemeJumpLayout()
        }
    }

    private fun initMorePop() {
        val contentView: View = LayoutInflater.from(this).inflate(R.layout.app_content_web_pop, null)
        // ???????????????
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
        contentView.llShare.setOnClickListener {
            ShareUtil.shareUrl(this, webView.url)
        }
        morePop = PopupWindow(this)
        morePop.contentView = contentView
        morePop.width = ViewGroup.LayoutParams.WRAP_CONTENT
        morePop.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // popWindow ???????????????????????????????????? window
        morePop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        morePop.isOutsideTouchable = true
        morePop.isTouchable = true
//        morePop.isFocusable = true
    }

    private fun initData() {
        webView.loadUrl(link)
    }

    private fun initTitle() {
        if (!appointTitle.isNullOrEmpty()) {
            tvTitle.text = appointTitle
            tvTitle.show()
            return
        }
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
                // ????????????????????????App????????????????????? ??????B???
                clipboardDog.shield {
                    showSchemeJumpLayout()
                    initSchemeJumpLayout(scheme)
                }
                // 5s???????????????
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
        if (entryId == 0L) {
            llSaveStatus.hide()
            tvCancel.text = "??????"
            tvJump.text = "??????"
            saveSchemeJump = false // ??????ID ??????????????????
            return
        }
        if (saveSchemeJump) {
            ivSaveStatus.setImageResource(R.drawable.app_select_press)
            tvCancel.text = "??????????????????????????????"
            tvJump.text = "??????????????????????????????"
        } else {
            ivSaveStatus.setImageResource(R.drawable.app_select_normal)
            tvCancel.text = "??????"
            tvJump.text = "??????"
        }
    }

    private fun saveSchemeJumpIfNecessary(scheme: String) {
        if (saveSchemeJump) {
            ToastUtil.toast("????????????????????????????????????????????????????????????")
            val entryBean = DataService.getEntryList(categoryId)?.findByCondition { it.date == entryId }
            entryBean?.let {
                // ????????????????????????scheme
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
        appointTitle = intent.getStringExtra(BROWSE_TITLE)
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
            // ?????????
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
        if (schemeJumpLayout.isGone()) {
            schemeJumpLayout.show()
            AnimationUtil.tranY(schemeJumpLayout, 900f, 0f, 300L, DecelerateInterpolator(), null).start()
        }
    }

    private fun hideSchemeJumpLayout() {
        if (schemeJumpLayout.isVisible()) {
            AnimationUtil.tranY(schemeJumpLayout, 0f, 900f, 300L, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    schemeJumpLayout.hide()
                }
            }).start()
        }
    }
}
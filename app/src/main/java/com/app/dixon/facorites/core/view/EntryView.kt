package com.app.dixon.facorites.core.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.common.SuccessCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.ImageEntryBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.*
import com.app.dixon.facorites.page.browse.SchemeJumper
import com.dixon.dlibrary.util.AnimationUtil
import com.dixon.dlibrary.util.FontUtil
import com.dixon.dlibrary.util.ToastUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.android.synthetic.main.app_dialog_create_entry_content.*
import kotlinx.android.synthetic.main.app_view_link_card.view.*
import java.io.File


/**
 * 全路径：com.app.dixon.facorites.core.view
 * 类描述：链接类型的卡片
 * 创建人：xuzheng
 * 创建时间：3/31/22 10:21 AM
 */

private const val CLICK_DELAY_TIME = 500L

class EntryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : BaseEntryView(context, attrs, defStyle) {

    private val animMonitor = SwitchAnimStatusMonitor(SWITCH_STATUS_CLOSE)
    private var bean: BaseEntryBean? = null

    private var animChain: AnimChain? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.app_view_link_card, this, true)
        FontUtil.font(title)
        setOnClickListener { }

        tvJump.setOnClickListener {
            // 跳转 只有链接才能跳转
            (bean as? LinkEntryBean)?.let { linkBean ->
                if (linkBean.link.isValidUrl()) {
                    val uri: Uri = Uri.parse(linkBean.link)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                } else {
                    ClipUtil.copyToClip(context, linkBean.link)
                    ToastUtil.toast("非网页链接，已复制到剪贴板，请自行选择合适程序")
                }
            }
        }

        tvUpdate.setOnClickListener {
            // 修改
            bean?.let {
                CreateEntryDialog(context, it).show()
            }
        }

        tvDelete.setOnClickListener {
            // 删除
            // 收起面板
            hideSubCard {
                bean?.let { entry ->
                    DataService.deleteEntry(entry, SuccessCallback {
                        ToastUtil.toast("删除成功！")
                        bean = null
                    })
                }
            }
        }

        tvCopy.setOnClickListener {
            // 只有链接才能复制
            (bean as? LinkEntryBean)?.let { linkBean ->
                ClipUtil.copyToClip(context, linkBean.link)
                ToastUtil.toast("已复制到剪贴板")
            }
        }

        ivBrowse.setOnClickListener {
            bean?.process({ linkEntry ->
                if (linkEntry.link.isValidUrl()) {
                    PageJumper.openBrowsePage(context, linkEntry.belongTo, linkEntry.date, linkEntry.link, linkEntry.title)
                } else {
                    ClipUtil.copyToClip(context, linkEntry.link)
                    ToastUtil.toast("非网页链接，已复制到剪贴板，请自行选择合适程序")
                }
            }, { imageEntry ->
                PageJumper.openImagePage(context, imageEntry.path)
            })
        }

        // 点击标星
        ivStar.setOnClickListener {
            bean?.let {
                val star = !it.star
                bean?.process({ linkEntry ->
                    DataService.updateEntry(
                        it,
                        LinkEntryBean(
                            link = linkEntry.link,
                            title = linkEntry.title,
                            remark = linkEntry.remark,
                            schemeJump = linkEntry.schemeJump,
                            date = linkEntry.date,
                            belongTo = linkEntry.belongTo,
                            star = star
                        )
                    )
                }, { imageEntry ->
                    DataService.updateEntry(
                        it,
                        ImageEntryBean(
                            path = imageEntry.path,
                            title = imageEntry.title,
                            date = imageEntry.date,
                            belongTo = imageEntry.belongTo,
                            star = star
                        )
                    )
                })
            }
        }
    }

    fun setLinkEntry(bean: LinkEntryBean) {
        this.bean = bean
        title.text = bean.title
        val iconLink = bean.link.try2IconLink()
        Ln.i("icon_link", iconLink)
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setControllerListener(ControllerListener(iconLink))
            .setUri(iconLink) // other setters
            .build()
        icon.controller = controller
        tvCreateTime.text = TimeUtils.friendlyTime(bean.date)
        bean.schemeJump?.let { scheme ->
            tvSchemeJump.show()
            tvSchemeJump.setOnClickListener {
                SchemeJumper.jumpByScheme(context, scheme)
            }
        } ?: tvSchemeJump.hide()
        updateStarIcon()
        initLinkUi()
    }

    private fun updateStarIcon() {
        bean?.let {
            if (it.star)
                ivStar.setImageResource(R.drawable.app_star_real)
            else
                ivStar.setImageResource(R.drawable.app_star_hollow)
        }
    }

    private fun initLinkUi() {
        tvJump.show()
        tvUpdate.show()
        tvCopy.show()
        ivBrowse.show()
        tvDelete.show()
        entryBg.hide()
        entryBgMask.hide()
    }

    fun setImageEntry(bean: ImageEntryBean) {
        this.bean = bean
        // 加载缩略图
        Ln.i("ImagePath", bean.path)
        icon.setImageByPath(bean.path, 12, 12)
        entryBg.setImageByPath(bean.path, 300, 30)
        title.text = bean.title
        tvCreateTime.text = TimeUtils.friendlyTime(bean.date)
        updateStarIcon()
        initImageUi()
    }

    private fun initImageUi() {
        tvJump.hide()
        tvUpdate.show()
        tvCopy.hide()
        ivBrowse.show()
        tvDelete.show()
        entryBg.show()
        entryBgMask.show()
    }

    fun clear() {
        this.bean = null
        title.text = ""
        icon.setImageURI("")
    }

    private inner class ControllerListener(val link: String) : BaseControllerListener<ImageInfo>() {

        override fun onFailure(id: String?, throwable: Throwable?) {
            super.onFailure(id, throwable)
            // 图标加载失败
            Ln.i("icon_link", "$link ${throwable?.message.toString()}")
        }
    }

    private fun subCardLogic(runOnUiComplete: (() -> Unit)? = null) {
        if (bean == null) {
            // 空的 没有展开效果
            return
        }
        if (animMonitor.canOpen()) {
            animMonitor.setOpening()
            ivBrowse.invisible()
            subCard.show()
            subCard.alpha = 0f
            val heightAnim = AnimationUtil.height(subCard, 0f, 24.dpF, 300, DecelerateInterpolator(), null)
            val alphaAnim = AnimationUtil.alpha(subCard, 0f, 1f, 300, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    animMonitor.setOpen()
                    runOnUiComplete?.invoke()
                }
            })
            animChain = AnimChain().addAnimator(heightAnim).addAnimator(alphaAnim).apply { start() }
        } else if (animMonitor.canClose()) {
            animMonitor.setClosing()
            val alphaAnim = AnimationUtil.alpha(subCard, 1f, 0f, 300, DecelerateInterpolator(), null)
            val heightAnim = AnimationUtil.height(subCard, 24.dpF, 0f, 300, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    animMonitor.setClose()
                    subCard.hide()
                    ivBrowse.show()
                    runOnUiComplete?.invoke()
                }
            })
            animChain = AnimChain().addAnimator(alphaAnim).addAnimator(heightAnim).apply { start() }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener {
            globalThrottleClick {
                subCardLogic()
                l?.onClick(it)
            }
        }
    }

    fun hideSubCard(runOnUiComplete: (() -> Unit)? = null) {
        if (animMonitor.canClose()) {
            subCardLogic(runOnUiComplete)
        } else if (animMonitor.isOpening()) {
            // 直接取消动画 收起面板
            animChain?.cancel()
            val alphaAnim = AnimationUtil.alpha(subCard, subCard.alpha, 0f, 300, DecelerateInterpolator(), null)
            val heightAnim = AnimationUtil.height(subCard, subCard.height.toFloat(), 0f, 300, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    animMonitor.setClose()
                    subCard.hide()
                    ivBrowse.show()
                    runOnUiComplete?.invoke()
                }
            })
            animChain = AnimChain().addAnimator(alphaAnim).addAnimator(heightAnim).apply { start() }
        }
    }

    private fun globalThrottleClick(action: () -> Unit) {
        if (System.currentTimeMillis() - globalClickTime >= CLICK_DELAY_TIME) {
            globalClickTime = System.currentTimeMillis()
            action.invoke()
        }
    }

    fun openSubCardAtOnce() {
        if (animMonitor.isOpened()) {
            return
        }
        animChain?.cancel()
        val layoutParams: ViewGroup.LayoutParams = subCard.layoutParams
        layoutParams.height = 24.dp
        subCard.layoutParams = layoutParams
        subCard.alpha = 1f
        subCard.show()
        ivBrowse.invisible()
        animMonitor.setOpen()
    }

    fun closeSubCardAtOnce() {
        if (animMonitor.isClosed()) {
            return
        }
        animChain?.cancel()
        val layoutParams: ViewGroup.LayoutParams = subCard.layoutParams
        layoutParams.height = 0.dp
        subCard.layoutParams = layoutParams
        subCard.alpha = 0f
        subCard.hide()
        ivBrowse.show()
        animMonitor.setClose()
    }
}
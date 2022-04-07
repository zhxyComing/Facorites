package com.app.dixon.facorites.core.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.SuccessCallback
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.ClipUtil
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.util.SWITCH_STATUS_CLOSE
import com.app.dixon.facorites.core.util.SwitchAnimStatusMonitor
import com.dixon.dlibrary.util.AnimationUtil
import com.dixon.dlibrary.util.FontUtil
import com.dixon.dlibrary.util.ToastUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.imagepipeline.image.ImageInfo
import kotlinx.android.synthetic.main.app_view_link_card.view.*


/**
 * 全路径：com.app.dixon.facorites.core.view
 * 类描述：链接类型的卡片
 * 创建人：xuzheng
 * 创建时间：3/31/22 10:21 AM
 */
class LinkCardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    FrameLayout(context, attrs, defStyle) {

    private val animMonitor = SwitchAnimStatusMonitor(SWITCH_STATUS_CLOSE)
    private var bean: LinkEntryBean? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.app_view_link_card, this, true)
        FontUtil.font(title)
        setOnClickListener { }

        tvJump.setOnClickListener {
            // 跳转
            // TODO
            bean?.let { linkBean ->
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
        }

        tvDelete.setOnClickListener {
            // 删除
            bean?.let { linkBean ->
                DataService.deleteEntry(DataService.getCategoryList()[0].id, linkBean, SuccessCallback {
                    ToastUtil.toast("删除成功！")
                    // 收起面板
                    subCardLogic()
                    bean = null
                })
            }
        }

        tvCopy.setOnClickListener {
            bean?.let { linkBean ->
                ClipUtil.copyToClip(context, linkBean.link)
                ToastUtil.toast("已复制到剪贴板")
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
    }

    fun clear() {
        title.text = ""
        icon.setImageURI("")
    }

    private inner class ControllerListener(val link: String) : BaseControllerListener<ImageInfo>() {

        override fun onFailure(id: String?, throwable: Throwable?) {
            super.onFailure(id, throwable)
            Ln.i("icon_link", "$link ${throwable?.message.toString()}")
            // 图标加载失败 隐藏图标View
            icon.hide()
        }
    }

    private fun subCardLogic() {
        if (bean == null) {
            // 空的 没有展开效果
            return
        }
        if (animMonitor.canOpen()) {
            animMonitor.setChanging()
            subCard.show()
            subCard.alpha = 0f
            val heightAnim = AnimationUtil.height(subCard, 0f, 24.dpF, 300, DecelerateInterpolator(), null)
            val alphaAnim = AnimationUtil.alpha(subCard, 0f, 1f, 300, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    animMonitor.setOpen()
                }
            })
            AnimationUtil.Chain().addAnimator(heightAnim).addAnimator(alphaAnim).start()
        } else if (animMonitor.canClose()) {
            animMonitor.setChanging()
            val alphaAnim = AnimationUtil.alpha(subCard, 1f, 0f, 300, DecelerateInterpolator(), null)
            val heightAnim = AnimationUtil.height(subCard, 24.dpF, 0f, 300, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    animMonitor.setClose()
                    subCard.hide()
                }
            })
            AnimationUtil.Chain().addAnimator(alphaAnim).addAnimator(heightAnim).start()
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener {
            subCardLogic()
            l?.onClick(it)
        }
    }
}
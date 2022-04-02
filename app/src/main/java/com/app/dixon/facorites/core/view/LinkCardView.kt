package com.app.dixon.facorites.core.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.ex.hide
import com.app.dixon.facorites.core.ex.try2IconLink
import com.app.dixon.facorites.core.util.Ln
import com.dixon.dlibrary.util.FontUtil
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

    init {
        LayoutInflater.from(context).inflate(R.layout.app_view_link_card, this, true)
        FontUtil.font(title)
    }

    fun setLinkEntry(bean: LinkEntryBean) {
        title.text = bean.title
        val iconLink = bean.link.try2IconLink()
        Ln.i("icon_link", iconLink)
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setControllerListener(ControllerListener(iconLink))
            .setUri(iconLink) // other setters
            .build()
        icon.controller = controller
    }

    private inner class ControllerListener(val link: String) : BaseControllerListener<ImageInfo>() {

        override fun onFailure(id: String?, throwable: Throwable?) {
            super.onFailure(id, throwable)
            Ln.i("icon_link", "$link ${throwable?.message.toString()}")
            // 图标加载失败 隐藏图标View
            icon.hide()
        }
    }
}
package com.app.dixon.facorites.core.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
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
import com.app.dixon.facorites.core.data.bean.*
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.*
import com.app.dixon.facorites.page.browse.SchemeJumper
import com.dixon.dlibrary.util.AnimationUtil
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
 *
 * TODO 代码优化&拆解
 */

private const val CLICK_DELAY_TIME = 500L

class EntryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : BaseEntryView(context, attrs, defStyle) {

    private val animMonitor = SwitchAnimStatusMonitor(SWITCH_STATUS_CLOSE)
    private var bean: BaseEntryBean? = null

    private var animChain: AnimChain? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.app_view_link_card, this, true)
        container.normalFont()

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
                if (it is CategoryEntryBean) {
                    UpdateChildCategoryDialog(context, it).show()
                } else {
                    CreateEntryDialog(context, it).show()
                }
            }
        }

        tvDelete.setOnClickListener {
            // 删除
            // 收起面板
            hideSubCard {
                bean?.let { entry ->
                    // 文件夹不能直接删除 弹窗提醒
                    if (bean is CategoryEntryBean) {
                        OptionDialog(
                            context = context,
                            title = "确认删除？",
                            desc = "收藏夹下所有收藏将被一并删除！",
                            rightString = "仍要删除",
                            leftString = "取消删除",
                            rightClick = {
                                DataService.deleteEntry(entry, SuccessCallback {
                                    ToastUtil.toast("删除成功！")
                                    bean = null
                                })
                            },
                        ).show()
                        return@let
                    }
                    DataService.deleteEntry(entry, SuccessCallback {
                        ToastUtil.toast("删除成功！")
                        bean = null
                    })
                }
            }
        }

        tvCopy.setOnClickListener {
            bean?.process({ linkEntry ->
                ClipUtil.copyToClip(context, linkEntry.link)
            }, {
                // 图片不能复制
            }, {
                // 文件夹不能复制
            }, { wordEntry ->
                ClipUtil.copyToClip(context, wordEntry.content)
            }, {
                // 图片集不能复制
            }, {
                // 视频不能复制
            })
            ToastUtil.toast("已复制到剪贴板")
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
            }, { categoryEntry ->
                PageJumper.openEntryPage(context, categoryEntry.categoryInfoBean)
            }, { wordEntry ->
                PageJumper.openWordPage(context, wordEntry.content)
            }, { galleryEntry ->
                PageJumper.openGalleryPage(context, galleryEntry.path, galleryEntry.title)
            }, { videoEntry ->
                PageJumper.openVideoPlayerPage(context, videoEntry.path)
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
                }, { categoryEntry ->
                    DataService.updateEntry(
                        it,
                        CategoryEntryBean(
                            categoryInfoBean = categoryEntry.categoryInfoBean,
                            date = categoryEntry.date,
                            belongTo = categoryEntry.belongTo,
                            star = star
                        )
                    )
                }, { wordEntry ->
                    DataService.updateEntry(
                        it,
                        WordEntryBean(
                            content = wordEntry.content,
                            date = wordEntry.date,
                            belongTo = wordEntry.belongTo,
                            star = star
                        )
                    )
                }, { galleryEntry ->
                    DataService.updateEntry(
                        it,
                        GalleryEntryBean(
                            path = galleryEntry.path,
                            title = galleryEntry.title,
                            date = galleryEntry.date,
                            belongTo = galleryEntry.belongTo,
                            star = star
                        )
                    )
                }, { videoEntry ->
                    DataService.updateEntry(
                        it,
                        VideoEntryBean(
                            path = videoEntry.path,
                            title = videoEntry.title,
                            date = videoEntry.date,
                            belongTo = videoEntry.belongTo,
                            star = star
                        )
                    )
                })
            }
        }

        // 点击隐藏背景
        tvHideBg.setOnClickListener {
            bean?.let {
                bean?.process({
                    // 链接没有背景 所以也没隐藏背景功能
                }, { imageEntry ->
                    DataService.updateEntry(
                        it,
                        ImageEntryBean(
                            path = imageEntry.path,
                            title = imageEntry.title,
                            hideBg = !imageEntry.hideBg,
                            date = imageEntry.date,
                            belongTo = imageEntry.belongTo,
                            star = imageEntry.star
                        )
                    )
                }, {
                    // 暂不支持文件夹隐藏背景
                }, {
                    // 语录目前没有背景
                }, {
                    // 相册集没有背景
                }, {
                    // 视频没有背景
                })
            }
        }

        // 点击浏览层级
        tvMap.setOnClickListener {
            // 只有文件夹能浏览层级
            (bean as? CategoryEntryBean)?.let {
                PageJumper.openMapPage(context, it.categoryInfoBean)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setCategoryEntry(bean: CategoryEntryBean, categoryTagShow: Boolean = true) {
        initCategoryUi()
        this.bean = bean
        title.text = bean.categoryInfoBean.name
        title.mediumFont()
        if (categoryTagShow) {
            setCategoryTag(bean.belongTo)
        } else {
            hideCategoryTag()
        }
        icon.setActualImageResource(R.drawable.app_category_icon)
        bean.categoryInfoBean.bgPath?.let { bg ->
            entryBg.show()
            entryBgMask.show()
            entryBg.setImageByPath(bg, 300, 30)
        } ?: let {
            entryBg.hide()
            entryBgMask.hide()
        }
        tvCreateTime.text = TimeUtils.friendlyTime(bean.date)
        updateStarIcon()
    }

    private fun initCategoryUi() {
        tvJump.hide()
        tvHideBg.hide()
        tvUpdate.show()
        tvCopy.hide()
        ivBrowse.show()
        tvDelete.show()
        entryBg.show()
        entryBgMask.show()
        categoryTag.show()
        tvMap.show()
        vEntryTag.hide()
    }

    fun setLinkEntry(bean: LinkEntryBean, categoryTagShow: Boolean = true) {
        initLinkUi()
        this.bean = bean
        title.text = bean.title
        title.normalFont()
        if (categoryTagShow) {
            setCategoryTag(bean.belongTo)
        } else {
            hideCategoryTag()
        }
//        val iconLink = bean.link.try2IconLink()
//        Ln.i("icon_link", iconLink)
//        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
//            .setControllerListener(ControllerListener(iconLink))
//            .setUri(iconLink) // other setters
//            .build()
//        icon.controller = controller
        tvCreateTime.text = TimeUtils.friendlyTime(bean.date)
        bean.schemeJump?.let { scheme ->
            tvSchemeJump.show()
            tvSchemeJump.setOnClickListener {
                val jumpSuccess = SchemeJumper.jumpByScheme(context, scheme)
                if (!jumpSuccess) {
                    ToastUtil.toast("页面跳转失败，请校验路由的正确性")
                }
            }
        } ?: tvSchemeJump.hide()
        updateStarIcon()
        icon.setActualImageResource(R.drawable.app_icon_entry_view_tag_link)
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
        tvHideBg.hide()
        tvUpdate.show()
        tvCopy.show()
        ivBrowse.show()
        tvDelete.show()
        entryBg.hide()
        entryBgMask.hide()
        categoryTag.hide()
        tvMap.hide()
        vEntryTag.setBackgroundColor(resources.getColor(R.color.md_red_400))
    }

    fun setImageEntry(bean: ImageEntryBean, categoryTagShow: Boolean = true) {
        initImageUi()
        this.bean = bean
        // 加载缩略图
        Ln.i("ImagePath", bean.path)
//        icon.setImageByPath(bean.path, 12, 12)
        // 显示/隐藏背景图
        tvHideBgText.text = if (bean.hideBg) "显示背景" else "隐藏背景"
        if (bean.hideBg) {
            entryBg.hide()
            entryBgMask.hide()
        } else {
            entryBg.show()
            entryBgMask.show()
            entryBg.setImageByPath(bean.path, 300, 30)
        }
        title.text = bean.title
        title.normalFont()
        if (categoryTagShow) {
            setCategoryTag(bean.belongTo)
        } else {
            hideCategoryTag()
        }
        tvCreateTime.text = TimeUtils.friendlyTime(bean.date)
        updateStarIcon()
        icon.setActualImageResource(R.drawable.app_icon_entry_view_tag_image)
    }

    private fun setCategoryTag(belongTo: Long) {
        // 设置分类信息 分类不会很多，找到同一个ID的几乎不耗时
        DataService.getCategoryList().find {
            it.id == belongTo
        }?.let {
            if (it.name.isNotEmpty()) {
                tvCategorySimpleName.text = it.name.first().toString()
            } else {
                hideCategoryTag()
            }
        } ?: hideCategoryTag()
    }

    private fun hideCategoryTag() {
        tvCategorySimpleName.hide()
        tagLine.hide()
    }

    private fun initImageUi() {
        tvSchemeJump.hide()
        tvJump.hide()
        tvHideBg.show()
        tvUpdate.show()
        tvCopy.hide()
        ivBrowse.show()
        tvDelete.show()
        entryBg.show()
        entryBgMask.show()
        categoryTag.hide()
        tvMap.hide()
        vEntryTag.setBackgroundColor(resources.getColor(R.color.md_blue_400))
    }

    fun setWordEntry(bean: WordEntryBean, categoryTagShow: Boolean = true) {
        initWordUi()
        this.bean = bean
        title.text = bean.content
        if (categoryTagShow) {
            setCategoryTag(bean.belongTo)
        } else {
            hideCategoryTag()
        }
        tvCreateTime.text = TimeUtils.friendlyTime(bean.date)
        updateStarIcon()
        icon.setActualImageResource(R.drawable.app_icon_entry_view_tag_word)
    }

    private fun initWordUi() {
        tvSchemeJump.hide()
        tvJump.hide()
        tvHideBg.hide()
        tvUpdate.show()
        tvCopy.show()
        ivBrowse.show()
        tvDelete.show()
        entryBg.hide()
        entryBgMask.hide()
        categoryTag.hide()
        tvMap.hide()
        vEntryTag.setBackgroundColor(resources.getColor(R.color.md_green_400))
    }

    fun setVideoEntry(bean: VideoEntryBean, categoryTagShow: Boolean = true) {
        initVideoUi()
        this.bean = bean
        title.text = bean.title
        if (categoryTagShow) {
            setCategoryTag(bean.belongTo)
        } else {
            hideCategoryTag()
        }
        tvCreateTime.text = TimeUtils.friendlyTime(bean.date)
        updateStarIcon()
        icon.setActualImageResource(R.drawable.app_icon_entry_view_tag_video) // TODO VIDEO 更换图标
    }

    private fun initVideoUi() {
        tvSchemeJump.hide()
        tvJump.hide()
        tvHideBg.hide()
        tvUpdate.show()
        tvCopy.hide()
        ivBrowse.show()
        tvDelete.show()
        entryBg.hide()
        entryBgMask.hide()
        categoryTag.hide()
        tvMap.hide()
        vEntryTag.setBackgroundColor(resources.getColor(R.color.md_purple_400))
    }

    fun setGalleryEntry(bean: GalleryEntryBean, categoryTagShow: Boolean = true) {
        initGalleryUi()
        this.bean = bean
        title.text = bean.title
        if (categoryTagShow) {
            setCategoryTag(bean.belongTo)
        } else {
            hideCategoryTag()
        }
        tvCreateTime.text = TimeUtils.friendlyTime(bean.date)
        updateStarIcon()
        icon.setActualImageResource(R.drawable.app_icon_entry_view_tag_gallery)
    }

    private fun initGalleryUi() {
        tvSchemeJump.hide()
        tvJump.hide()
        tvHideBg.hide()
        tvUpdate.show()
        tvCopy.hide()
        ivBrowse.show()
        tvDelete.show()
        entryBg.hide()
        entryBgMask.hide()
        categoryTag.hide()
        tvMap.hide()
        vEntryTag.setBackgroundColor(resources.getColor(R.color.md_orange_400))
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
            subCard.invisible()
            val heightAnim = AnimationUtil.height(subCard, 0f, 60.dpF, 300, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    subCard.show()
                }
            })
            val tranXAnim = AnimationUtil.tranX(subCard, (-300).dpF, 0f, 300, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    animMonitor.setOpen()
                    runOnUiComplete?.invoke()
                }
            })
            animChain = AnimChain().addAnimator(heightAnim).addAnimator(tranXAnim).apply { start() }
        } else if (animMonitor.canClose()) {
            animMonitor.setClosing()
            val tranXAnim = AnimationUtil.tranX(subCard, 0f, (-300).dpF, 300, DecelerateInterpolator(), null)
            val heightAnim = AnimationUtil.height(subCard, 60.dpF, 0f, 300, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    animMonitor.setClose()
                    subCard.hide()
                    ivBrowse.show()
                    runOnUiComplete?.invoke()
                }
            })
            animChain = AnimChain().addAnimator(tranXAnim).addAnimator(heightAnim).apply { start() }
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
            val tranXAnim = AnimationUtil.tranX(subCard, 0f, (-300).dpF, 300, DecelerateInterpolator(), null)
            val heightAnim = AnimationUtil.height(subCard, subCard.height.toFloat(), 0f, 300, DecelerateInterpolator(), object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    animMonitor.setClose()
                    subCard.hide()
                    ivBrowse.show()
                    runOnUiComplete?.invoke()
                }
            })
            animChain = AnimChain().addAnimator(tranXAnim).addAnimator(heightAnim).apply { start() }
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
        layoutParams.height = 60.dp
        subCard.layoutParams = layoutParams
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
        subCard.hide()
        ivBrowse.show()
        animMonitor.setClose()
    }
}
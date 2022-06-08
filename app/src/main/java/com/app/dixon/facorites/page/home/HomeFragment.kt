package com.app.dixon.facorites.page.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.forEachIndexed
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.common.LAST_ENTRY_NUM
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.hide
import com.app.dixon.facorites.core.ex.process
import com.app.dixon.facorites.core.ex.show
import com.app.dixon.facorites.core.util.CollectionUtil
import com.app.dixon.facorites.core.view.EntryView
import com.app.dixon.facorites.page.edit.event.LastEntryNumUpdateEvent
import com.dixon.dlibrary.util.SharedUtil
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.app_fragment_home_content.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 全路径：com.app.dixon.facorites.page.home
 * 类描述：首页的Fragment
 * 创建人：xuzheng
 * 创建时间：3/22/22 2:49 PM
 *
 * 展示最近的三个收藏
 */
class HomeFragment : VisibleExtensionFragment(), DataService.IGlobalEntryChanged {

    // 仅包含最近的前N个元素
    private val entries = mutableListOf<BaseEntryBean>()
    private var maxEntryNum = 5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.app_fragment_home_content, container, false).apply {
        // 注册监听
        EventBus.getDefault().register(this@HomeFragment)
        maxEntryNum = SharedUtil.getInt(LAST_ENTRY_NUM, 5)
    }

    // 添加条目卡片View
    private fun LinearLayout.addCardView(): EntryView {
        val entryView = LayoutInflater.from(context).inflate(R.layout.app_home_fragment_entry_card, this, false) as EntryView
        addView(entryView)
        val entryIndex = indexOfChild(entryView)
        if (entryIndex == 0) {
            val params = entryView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = 0
            entryView.layoutParams = params
        }
        entryView.setOnClickListener {
            this.forEachIndexed { index, view ->
                if (entryIndex != index) {
                    (view as? EntryView)?.hideSubCard()
                }
            }
        }
        return entryView
    }

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        DataService.register(this)
        initBanner()
        initEntryData()
        initEntryView()
        initOtherView()
    }

    private fun initOtherView() {
        viewAll.setOnClickListener {
            context?.let {
                PageJumper.openAllEntryPage(it)
            }
        }
    }

    private fun initBanner() {
//        val images = listOf("https://imgs.699pic.com/01/500/340/209/500340209.jpg!list2x.v1", "https://pic.5tu.cn/uploads/allimg/1605/251507157490.jpg")
        val images = listOf(R.drawable.app_guide_cover_1)
        banner.setParams(images, { inflate, container, bean ->
            val item = inflate.inflate(R.layout.app_item_banner_home, container, false)
            val imageView = item.findViewById<SimpleDraweeView>(R.id.ivImage)
//            imageView.setImageURI(bean)
            imageView.setActualImageResource(bean)
            item
        })
    }

    private fun initEntryData() {
        obtainLastEntry()
    }

    private fun initEntryView() {
        cardLayout.removeAllViews()
        for (index in 0 until maxEntryNum) {
            (entries.getOrNull(index))?.let {
                val cardView = cardLayout.addCardView()
                it.process({ linkEntry ->
                    // link 类型
                    cardView.setLinkEntry(linkEntry)
                }, { imageEntry ->
                    // image 类型
                    cardView.setImageEntry(imageEntry)
                })
            }
        }
        updateEntryTip()
    }

    private fun updateEntryTip() {
        when {
            // 数量达到或超过展示限制
            entries.size >= maxEntryNum -> {
                entryTip.text = "-- 仅展示最近${maxEntryNum}条 --"
                entryTip.show()
                emptyTip.hide()
            }
            // 数量为空
            entries.isEmpty() -> {
                entryTip.hide()
                emptyTip.show()
            }
            // 数量不为空且未达展示上限
            else -> {
                entryTip.hide()
                emptyTip.hide()
            }
        }
    }

    // 获取最近的前三个Entry
    private fun obtainLastEntry() {
        entries.clear()
        val allEntry = mutableListOf<BaseEntryBean>()
        DataService.getCategoryList().forEach { category ->
            DataService.getEntryList(category.id)?.let { entryList ->
                allEntry.addAll(entryList)
            }
        }
        allEntry.sortByDescending { it.date }
        val size = minOf(allEntry.size, maxEntryNum)
        for (index in 0 until size) {
            entries.add(allEntry[index])
        }
    }

    override fun onDataCreated(bean: BaseEntryBean) {
        // 更新数据
        CollectionUtil.insertDataToHead(entries, bean, maxEntryNum)
        initEntryView()
    }

    override fun onDataDeleted(bean: BaseEntryBean) {
        if (entries.contains(bean)) {
            obtainLastEntry()
            initEntryView()
        }
    }

    override fun onDataUpdated(bean: BaseEntryBean) {
        val index = entries.indexOf(bean)
        if (index != -1) {
            val cardView = cardLayout.getChildAt(index) as? EntryView
            bean.process({
                cardView?.setLinkEntry(it)
            }, {
                cardView?.setImageEntry(it)
            })
            // indexOf只是使用ID判断，并不代表内容一致，需要更新内容
            entries[index] = bean
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLastEntryNumUpdate(event: LastEntryNumUpdateEvent) {
        maxEntryNum = event.num
        initEntryData()
        initEntryView()
    }
}
package com.app.dixon.facorites.page.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.hide
import com.app.dixon.facorites.core.ex.show
import com.app.dixon.facorites.core.util.CollectionUtil
import com.app.dixon.facorites.core.view.LinkCardView
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.app_fragment_home_content.*

/**
 * 全路径：com.app.dixon.facorites.page.home
 * 类描述：首页的Fragment
 * 创建人：xuzheng
 * 创建时间：3/22/22 2:49 PM
 *
 * 展示最近的三个收藏
 */

private const val MAX_ENTRY_NUM = 5

class HomeFragment : VisibleExtensionFragment(), DataService.IGlobalEntryChanged {

    // 仅包含最近的前N个元素
    private val entries = mutableListOf<BaseEntryBean>()
    private lateinit var cards: List<LinkCardView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.app_fragment_home_content, container, false).apply {
        val cards = mutableListOf<LinkCardView>()
        cards.add(findViewById(R.id.cardFirst))
        cards.add(findViewById(R.id.cardSecond))
        cards.add(findViewById(R.id.cardThird))
        cards.add(findViewById(R.id.cardFourth))
        cards.add(findViewById(R.id.cardFifth))
        this@HomeFragment.cards = cards

        // TODO 测试删除
        cards.forEachIndexed { index, linkCardView ->
            linkCardView.setOnClickListener {
                Log.i("testkkk","$index")
                hideSubCard(index)
            }
        }
    }

    private fun hideSubCard(target: Int) {
        cards.forEachIndexed { index, linkCardView ->
            if (target != index) {
                linkCardView.hideSubCard()
            }
        }
    }

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        DataService.register(this)
        initData()
        initBanner()
        initEntries()
    }

    private fun initBanner() {
        val images = listOf("https://imgs.699pic.com/01/500/340/209/500340209.jpg!list2x.v1", "https://pic.5tu.cn/uploads/allimg/1605/251507157490.jpg")
        banner.setParams(images, { inflate, container, bean ->
            val item = inflate.inflate(R.layout.app_item_banner_home, container, false)
            val imageView = item.findViewById<SimpleDraweeView>(R.id.ivImage)
            imageView.setImageURI(bean)
            item
        })
    }

    private fun initData() {
        obtainLastEntry()
    }

    private fun initEntries() {
        for (index in 0 until MAX_ENTRY_NUM) {
            // TODO 根据类型判断
            (entries.getOrNull(index) as? LinkEntryBean)?.let {
                cards[index].show()
                cards[index].setLinkEntry(it)
            } ?: let {
                cards[index].hide()
                cards[index].clear()
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
        val size = minOf(allEntry.size, MAX_ENTRY_NUM)
        for (index in 0 until size) {
            entries.add(allEntry[index])
        }
    }

    override fun onDataCreated(bean: BaseEntryBean) {
        // 更新数据
        CollectionUtil.insertDataToHead(entries, bean, MAX_ENTRY_NUM)
        initEntries()
    }

    override fun onDataDeleted(bean: BaseEntryBean) {
        if (entries.contains(bean)) {
            obtainLastEntry()
            initEntries()
        }
    }

    override fun onDataUpdated(bean: BaseEntryBean) {

    }
}
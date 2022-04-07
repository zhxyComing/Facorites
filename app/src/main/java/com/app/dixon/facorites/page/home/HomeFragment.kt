package com.app.dixon.facorites.page.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.util.CollectionUtil
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.view.LinkCardView
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
//            linkCardView.setOnLongClickListener {
//                DataService.deleteEntry(DataService.getCategoryList()[0].id, entries[index])
//                true
//            }
        }
    }

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        DataService.register(this)
        initData()
        initView()
    }

    private fun initData() {
        obtainLastEntry()
    }

    private fun initView() {
        val size = minOf(entries.size, MAX_ENTRY_NUM)
        for (index in 0 until MAX_ENTRY_NUM) {
            // TODO 根据类型判断
            (entries.getOrNull(index) as? LinkEntryBean)?.let {
                cards[index].setLinkEntry(it)
            } ?: cards[index].clear()
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

    override fun onDataCreated(data: BaseEntryBean) {
        // 更新数据
        CollectionUtil.insertDataToHead(entries, data, MAX_ENTRY_NUM)
        initView()
    }

    override fun onDataDeleted(t: BaseEntryBean) {
        if (entries.contains(t)) {
            obtainLastEntry()
            initView()
        }
    }

    override fun onDataUpdated(t: BaseEntryBean) {

    }
}
package com.app.dixon.facorites.page.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.view.forEachIndexed
import androidx.core.widget.PopupWindowCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.common.LAST_ENTRY_NUM
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.CollectionUtil
import com.app.dixon.facorites.core.util.KeyboardUtil
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.view.EntryView
import com.app.dixon.facorites.page.edit.event.LastEntryNumUpdateEvent
import com.app.dixon.facorites.page.entry.EntryAdapter
import com.app.dixon.facorites.page.entry.Openable
import com.dixon.dlibrary.util.SharedUtil
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.app_custom_spinner_expand.view.*
import kotlinx.android.synthetic.main.app_fragment_home_content.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.abs


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

    // 所有元素 用于搜索用
    private val allEntries = mutableListOf<Openable<BaseEntryBean>>()
    private var searchEntryAdapter: EntryAdapter? = null
    private lateinit var searchExpandList: PopupWindow
    private var searchExpandShow = false

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
        initSearchView()
    }

    private fun initSearchView() {
        context?.let {
            // 搜索数据源
            DataService.getCategoryList().forEach { category ->
                DataService.getEntryList(category.id)?.forEach { entry ->
                    allEntries.add(Openable(false, entry))
                }
            }
            // 搜索下拉框
            initSearchExpand()

            // 搜索监听
            etSearch.addTextChangedListener({ _, _, _, _ -> },
                { charSequence, _, _, _ ->
                    val searchString = charSequence.toString()
                    if (searchString.isEmpty()) {
                        searchExpandList.dismiss()
                    } else {
                        Ln.i("Filter", "过滤 $searchString")
                        searchEntryAdapter?.filter?.filter(charSequence.toString())

                        if (!searchExpandShow) {
                            searchExpandShow = !searchExpandShow
                            val offsetX = abs(searchExpandList.contentView.measuredWidth - etSearch.width) / 2
                            val offsetY = 0
                            PopupWindowCompat.showAsDropDown(searchExpandList, etSearch, offsetX, offsetY, Gravity.START)
                        }
                    }
                }, {})

            // 搜索结果为空 隐藏搜索结果列表
            searchEntryAdapter?.onFilterEmptyListener = {
                searchExpandList.dismiss()
            }

            ivSearchClear.setOnClickListener {
                etSearch.setText("")
                activity?.let {
                    KeyboardUtil.closeKeyboard(it)
                }
                etSearch.clearFocus()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        searchExpandList.dismiss()
    }

    override fun onStop() {
        super.onStop()
        // 监听软键盘关闭，然后清除焦点也是一种实现，但是软键盘的关闭监听并不准确
        // 这里退出页面时清除焦点，以防再次进入弹出软键盘
        etSearch.clearFocus()
    }

    private fun initSearchExpand() {
        val contentView: View = LayoutInflater.from(context).inflate(R.layout.app_home_search_expand, null)
        with(contentView.rvExpand) {
            this.layoutManager = LinearLayoutManager(context)
            searchEntryAdapter = EntryAdapter(context, allEntries)
            adapter = searchEntryAdapter
        }
        searchExpandList = PopupWindow(context)
        searchExpandList.contentView = contentView
        searchExpandList.width = ViewGroup.LayoutParams.WRAP_CONTENT
        searchExpandList.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // popWindow 显示时，点击外部优先关闭 window
        searchExpandList.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        searchExpandList.isOutsideTouchable = true
//        searchExpandList.isTouchable = true

        // 在展示之前先执行一次测量 避免后续获取宽高为0
        searchExpandList.contentView.measure(
            makeDropDownMeasureSpec(searchExpandList.width),
            makeDropDownMeasureSpec(searchExpandList.height)
        )

        searchExpandList.setOnDismissListener {
            searchExpandShow = false
        }
    }

    private fun makeDropDownMeasureSpec(measureSpec: Int): Int {
        val mode = if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
            View.MeasureSpec.UNSPECIFIED
        } else {
            View.MeasureSpec.EXACTLY
        }
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode)
    }

    private fun initOtherView() {
        viewAll.setOnClickListener {
            context?.let {
                PageJumper.openAllEntryPage(it)
            }
        }
    }

    private fun initBanner() {
        // TODO 目前是强制跳转关于页
        val images = listOf(R.drawable.app_guide_cover_1)
        banner.setParams(images, { inflate, container, bean ->
            val item = inflate.inflate(R.layout.app_item_banner_home, container, false)
            val imageView = item.findViewById<SimpleDraweeView>(R.id.ivImage)
            imageView.setActualImageResource(bean)
            imageView.setOnClickListener {
                context?.let {
                    PageJumper.openAboutPage(it)
                }
            }
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
        // 更新所有数据 这里直接加到首位
        allEntries.add(0, Openable(false, bean))
        searchEntryAdapter?.filterData?.add(0, Openable(false, bean))
        searchEntryAdapter?.notifyItemChanged(0)
    }

    override fun onDataDeleted(bean: BaseEntryBean) {
        if (entries.contains(bean)) {
            obtainLastEntry()
            initEntryView()
            // 更新所有数据
            allEntries.findByCondition { it.data == bean }?.let {
                allEntries.remove(it)
                searchEntryAdapter?.filterData?.remove(it)
                searchEntryAdapter?.notifyDataSetChanged()
            }
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
        // 更新搜索用的所有数据
        // 因为过滤数据和原始数据使用的同一个Openable，所以Openable的成员变量有一个发生变更即可
        allEntries.findByCondition { it.data == bean }?.let {
            it.data = bean
            searchEntryAdapter?.notifyDataSetChanged()
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
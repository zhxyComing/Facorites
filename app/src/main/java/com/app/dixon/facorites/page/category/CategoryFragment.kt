package com.app.dixon.facorites.page.category

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.common.SORT_TYPE_NAME
import com.app.dixon.facorites.core.common.SORT_TYPE_TIME
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.findByCondition
import com.app.dixon.facorites.core.ex.findIndexByCondition
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.core.view.CreateCategoryDialog
import kotlinx.android.synthetic.main.app_fragment_category_content.*

/**
 * 分类 Fragment
 */

class CategoryFragment : VisibleExtensionFragment(), DataService.ICategoryChanged {

    private val dataList: MutableList<CategoryInfoBean> = mutableListOf()

    private var sortType: String = SORT_TYPE_TIME

    // 有条目变化时 刷新列表的内容 比如收藏数等等
    private val itemContentUpdateListener = object : DataService.IGlobalEntryChanged {

        override fun onDataRefresh() {
            rvCategory.adapter?.notifyDataSetChanged()
        }

        override fun onDataCreated(bean: BaseEntryBean) {
            rvCategory.adapter?.notifyDataSetChanged()
        }

        override fun onDataDeleted(bean: BaseEntryBean) {
            rvCategory.adapter?.notifyDataSetChanged()
        }

        override fun onDataUpdated(bean: BaseEntryBean) {
            rvCategory.adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.app_fragment_category_content, container, false).apply {
        normalFont()
        findViewById<View>(R.id.sort).mediumFont()
    }

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        DataService.register(this)
        DataService.register(itemContentUpdateListener)
        initView()
    }

    private fun initView() {
        context?.let {
            dataList.addAll(DataService.getCategoryList())
            sortByLastTime() // 新创建的收藏夹排前边
            val adapter = CategoryAdapter(it, dataList)
            val controller: LayoutAnimationController = AnimationUtils.loadLayoutAnimation(it, R.anim.app_rv_in_anim)
            rvCategory.layoutAnimation = controller
            rvCategory.layoutManager = LinearLayoutManager(it)
            rvCategory.adapter = adapter
        }
        ivCreateCategory.setOnClickListener {
            context?.let {
                CreateCategoryDialog(it).show()
            }
        }
        sort.setOnClickListener {
            changeSort()
        }
    }

    private fun changeSort() {
        when (sortType) {
            SORT_TYPE_TIME -> {
                sortByName()
                rvCategory.adapter?.notifyDataSetChanged()
                sortType = SORT_TYPE_NAME
                sort.text = "名称排序"
            }
            SORT_TYPE_NAME -> {
                sortByLastTime()
                rvCategory.adapter?.notifyDataSetChanged()
                sortType = SORT_TYPE_TIME
                sort.text = "最近创建排序"
            }
        }
    }

    // 最近创建排序 顺序如下：
    // 1.最新置顶 2.置顶 3.最新创建 4.创建
    private fun sortByLastTime() {
        val topList = dataList.filter { it.topTimeMs != 0L }.sortedByDescending { it.topTimeMs }
        val normalList = dataList.filter { it.topTimeMs == 0L }.sortedByDescending { it.id }
        dataList.clear()
        dataList.addAll(topList)
        dataList.addAll(normalList)
    }

    private fun sortByName(){
        val topList = dataList.filter { it.topTimeMs != 0L }.sortedBy { it.name }
        val normalList = dataList.filter { it.topTimeMs == 0L }.sortedBy { it.name }
        dataList.clear()
        dataList.addAll(topList)
        dataList.addAll(normalList)
    }

    override fun onDataCreated(bean: CategoryInfoBean) {
        dataList.add(0, bean)
        if (sortType == SORT_TYPE_NAME) {
            sortByName()
        } else {
            sortByLastTime()
        }
        rvCategory.adapter?.notifyDataSetChanged()
    }

    override fun onDataDeleted(bean: CategoryInfoBean) {
        val index = dataList.indexOf(bean)
        if (index != -1) {
            dataList.removeAt(index)
            rvCategory.adapter?.notifyItemRemoved(index)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDataUpdated(bean: CategoryInfoBean) {
        val updateByTopBehavior = 0x0
        val updateByTopCancelBehavior = 0x1
        val updateBehavior = 0x2
        var behavior = updateBehavior
        dataList.findByCondition { it.id == bean.id }?.let {
            if (it.topTimeMs != bean.topTimeMs) {
                behavior = if (bean.topTimeMs != 0L) {
                    // 说明置顶了，放在第一位
                    updateByTopBehavior
                } else {
                    // 说明取消置顶 重新排序
                    updateByTopCancelBehavior
                }
            }
        }
        dataList.findIndexByCondition { it.id == bean.id }?.let {
            when (behavior) {
                updateByTopBehavior -> {
                    // 置顶，更新列表
                    dataList.removeAt(it)
                    dataList.add(0, bean)
                    rvCategory.adapter?.notifyDataSetChanged()
                }
                updateByTopCancelBehavior -> {
                    // 取消置顶，重新排序
                    dataList[it] = bean
                    if (sortType == SORT_TYPE_NAME) {
                        sortByName()
                    } else {
                        sortByLastTime()
                    }
                    rvCategory.adapter?.notifyDataSetChanged()
                }
                else -> {
                    // 正常更新，刷新Item即可
                    dataList[it] = bean
                    rvCategory.adapter?.notifyItemChanged(it)
                }
            }
        }
    }
}
package com.app.dixon.facorites.page.category

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
import com.app.dixon.facorites.core.ex.findIndexByCondition
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
    ): View = inflater.inflate(R.layout.app_fragment_category_content, container, false)

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        DataService.register(this)
        DataService.register(itemContentUpdateListener)
        initView()
    }

    private fun initView() {
        context?.let {
            dataList.addAll(DataService.getCategoryList())
            dataList.sortByDescending { data -> data.id } // 新创建的收藏夹排前边
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
                dataList.sortBy { data -> data.name }
                rvCategory.adapter?.notifyDataSetChanged()
                sortType = SORT_TYPE_NAME
                sort.text = "名称排序"
            }
            SORT_TYPE_NAME -> {
                dataList.sortByDescending { data -> data.id }
                rvCategory.adapter?.notifyDataSetChanged()
                sortType = SORT_TYPE_TIME
                sort.text = "最近创建排序"
            }
        }
    }

    override fun onDataCreated(bean: CategoryInfoBean) {
        dataList.add(0, bean)
        if (sortType == SORT_TYPE_NAME) {
            dataList.sortBy { data -> data.name }
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

    override fun onDataUpdated(bean: CategoryInfoBean) {
        dataList.findIndexByCondition { it.id == bean.id }?.let {
            dataList[it] = bean
            rvCategory.adapter?.notifyItemChanged(it)
        }
    }
}
package com.app.dixon.facorites.page.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.view.CreateCategoryDialog
import kotlinx.android.synthetic.main.app_fragment_category_content.*

/**
 * 分类 Fragment
 */

private const val SORT_TYPE_TIME = "sort_time"
private const val SORT_TYPE_NAME = "sort_name"

class CategoryFragment : VisibleExtensionFragment(), DataService.ICategoryChanged {

    private val dataList: MutableList<CategoryInfoBean> = mutableListOf()

    private var sortType: String = SORT_TYPE_TIME

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.app_fragment_category_content, container, false)

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        DataService.register(this)
        initView()
    }

    private fun initView() {
        context?.let {
            dataList.addAll(DataService.getCategoryList())
            dataList.sortByDescending { data -> data.id } // 新创建的收藏夹排前边
            val adapter = CategoryAdapter(it, dataList)
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
        val index = dataList.indexOf(bean)
        if (index != -1) {
            dataList[index] = bean
            rvCategory.adapter?.notifyItemChanged(index)
        }
    }
}
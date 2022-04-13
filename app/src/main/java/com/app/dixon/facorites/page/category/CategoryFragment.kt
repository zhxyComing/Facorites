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
class CategoryFragment : VisibleExtensionFragment(), DataService.ICategoryChanged {

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
            val list = DataService.getCategoryList()
            val adapter = CategoryAdapter(it, list)
            rvCategory.layoutManager = LinearLayoutManager(it)
            rvCategory.adapter = adapter
        }
        ivCreateCategory.setOnClickListener {
            context?.let {
                CreateCategoryDialog(it).show()
            }
        }
    }

    override fun onDataCreated(bean: CategoryInfoBean) {
        rvCategory.adapter?.notifyDataSetChanged()
    }

    override fun onDataDeleted(bean: CategoryInfoBean) {
        rvCategory.adapter?.notifyDataSetChanged()
    }

    override fun onDataUpdated(bean: CategoryInfoBean) {
        rvCategory.adapter?.notifyDataSetChanged()
    }
}
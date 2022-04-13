package com.app.dixon.facorites.page.entry

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.CATEGORY_ID
import com.app.dixon.facorites.core.common.CATEGORY_INFO
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.service.DataService
import kotlinx.android.synthetic.main.activity_entry.*
import kotlin.properties.Delegates

class EntryActivity : BaseActivity() {

    private var categoryInfo by Delegates.notNull<CategoryInfoBean>()

    private val data = mutableListOf<BaseEntryBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        intent.getParcelableExtra<CategoryInfoBean>(CATEGORY_INFO)?.let {
            categoryInfo = it
        } ?: finish()

        DataService.register(DataChangedCallback(categoryInfo.id))
        initView()
    }

    private fun initView() {
        DataService.getEntryList(categoryInfo.id)?.let {
            data.addAll(it)
        }
        // 最近时间排序
        data.sortByDescending { it.date }
        rvCategory.adapter = EntryAdapter(this, data)
        rvCategory.layoutManager = LinearLayoutManager(this)

        // 设置标题
        tvCategoryName.text = categoryInfo.name
    }

    private inner class DataChangedCallback(categoryId: Long) : DataService.IEntryChanged(categoryId) {

        override fun onDataCreated(bean: BaseEntryBean) {
            data.add(0, bean)
            rvCategory.adapter?.notifyDataSetChanged()
        }

        override fun onDataDeleted(bean: BaseEntryBean) {
            data.remove(bean)
            rvCategory.adapter?.notifyDataSetChanged()
        }

        override fun onDataUpdated(bean: BaseEntryBean) {
            rvCategory.adapter?.notifyDataSetChanged()
        }
    }
}
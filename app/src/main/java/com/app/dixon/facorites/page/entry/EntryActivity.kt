package com.app.dixon.facorites.page.entry

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.CATEGORY_INFO
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.util.Ln
import kotlinx.android.synthetic.main.activity_entry.*
import kotlin.properties.Delegates

class EntryActivity : BaseActivity() {

    private var categoryInfo by Delegates.notNull<CategoryInfoBean>()

    private val data = mutableListOf<Openable<BaseEntryBean>>()

    private lateinit var callback: DataService.IEntryChanged

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        intent.getParcelableExtra<CategoryInfoBean>(CATEGORY_INFO)?.let {
            categoryInfo = it
        } ?: finish()

        // 注意：Callback不能使用局部变量，否则有页面还在、Callback被回收的风险
        callback = DataChangedCallback(categoryInfo.id)
        DataService.register(callback)
        initView()
    }

    private fun initView() {
        DataService.getEntryList(categoryInfo.id)?.forEach {
            data.add(Openable(data = it))
        }
        // 最近时间排序
        data.sortByDescending { it.data.date }
        rvCategory.adapter = EntryAdapter(this, data)
        rvCategory.layoutManager = LinearLayoutManager(this)

        // 设置标题
        tvCategoryName.text = categoryInfo.name
    }

    private inner class DataChangedCallback(categoryId: Long) : DataService.IEntryChanged(categoryId) {

        override fun onDataCreated(bean: BaseEntryBean) {
            data.add(0, Openable(data = bean))
            rvCategory.adapter?.notifyDataSetChanged()
        }

        override fun onDataDeleted(bean: BaseEntryBean) {
            // 移除数据
            Ln.i("EntryActivity", "onDataDeleted $bean")
            val iterator = data.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.data == bean) {
                    Ln.i("EntryActivity", "remove success")
                    iterator.remove()
                }
            }
            rvCategory.adapter?.notifyDataSetChanged()
        }

        override fun onDataUpdated(bean: BaseEntryBean) {
            rvCategory.adapter?.notifyDataSetChanged()
        }
    }
}
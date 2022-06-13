package com.app.dixon.facorites.page.entry

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.CATEGORY_INFO
import com.app.dixon.facorites.core.common.SORT_TYPE_TIME
import com.app.dixon.facorites.core.common.SORT_TYPE_TIME_ORDER
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.hide
import com.app.dixon.facorites.core.ex.setImageByPath
import com.app.dixon.facorites.core.ex.setImageByUri
import com.app.dixon.facorites.core.ex.show
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.view.ENTRY_IMAGE_REQUEST
import com.app.dixon.facorites.page.category.event.CategoryImageCompleteEvent
import com.dixon.dlibrary.util.FontUtil
import kotlinx.android.synthetic.main.activity_entry.*
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates

class EntryActivity : BaseActivity() {

    private var categoryInfo by Delegates.notNull<CategoryInfoBean>()

    private val data = mutableListOf<Openable<BaseEntryBean>>()

    private var sortType: String = SORT_TYPE_TIME

    private lateinit var callback: DataService.IEntryChanged

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)
        FontUtil.font(window.decorView)

        intent.getParcelableExtra<CategoryInfoBean>(CATEGORY_INFO)?.let {
            categoryInfo = it
        } ?: finish()

        // 注意：Callback不能使用局部变量，否则有页面还在、Callback被回收的风险
        callback = DataChangedCallback(categoryInfo.id)
        DataService.register(callback)
        initView()
    }

    override fun useStatusTransparent(): Boolean = true

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
        // 设置数量
        updateEntryNum()

        // 排序方式
        sort.setOnClickListener {
            changeSort()
        }

        // 设置背景图
        categoryInfo.bgPath?.let {
            bgView.setImageByPath(it)
        } ?: bgView.setImageByUri(Uri.parse("res://com.app.dixon.facorites/" + R.drawable.app_icon_category_bg))

        // 显示空页面
        updateEmptyTip()
    }

    private fun updateEmptyTip() {
        if (data.isEmpty()) {
            emptyTip.show()
            sort.hide()
            rvCategory.hide()
        } else {
            emptyTip.hide()
            sort.show()
            rvCategory.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateEntryNum() {
        tvEntryNum.text = "共 ${data.size} 条收藏"
    }

    private fun changeSort() {
        when (sortType) {
            SORT_TYPE_TIME -> {
                data.sortBy { it.data.date }
                rvCategory.adapter?.notifyDataSetChanged()
                sortType = SORT_TYPE_TIME_ORDER
                sort.text = "创建时间排序"
            }
            SORT_TYPE_TIME_ORDER -> {
                data.sortByDescending { it.data.date }
                rvCategory.adapter?.notifyDataSetChanged()
                sortType = SORT_TYPE_TIME
                sort.text = "最近创建排序"
            }
        }
    }

    private inner class DataChangedCallback(categoryId: Long) : DataService.IEntryChanged(categoryId) {

        override fun onDataCreated(bean: BaseEntryBean) {
            if (sortType == SORT_TYPE_TIME) {
                data.add(0, Openable(data = bean))
            } else if (sortType == SORT_TYPE_TIME_ORDER) {
                data.add(Openable(data = bean))
            }
            rvCategory.adapter?.notifyDataSetChanged()
            updateEntryNum()
            updateEmptyTip()
        }

        override fun onDataDeleted(bean: BaseEntryBean) {
            // 移除数据
            Ln.i("EntryActivity", "onDataDeleted $bean")
            find(bean)?.let { index ->
                data.removeAt(index)
                rvCategory.adapter?.notifyItemRemoved(index)
                updateEntryNum()
                updateEmptyTip()
            }
        }

        override fun onDataUpdated(bean: BaseEntryBean) {
            // 找到ID一样的数据 然后替换
            find(bean)?.let { index ->
                val originOpenStatus = data[index].isOpen
                val originCategory = data[index].data.belongTo
                // 同一文件夹 更新
                if (originCategory == bean.belongTo) {
                    data[index] = Openable(originOpenStatus, bean)
                    rvCategory.adapter?.notifyItemChanged(index)
                } else {
                    // 不同文件夹 移除
                    data.removeAt(index)
                    rvCategory.adapter?.notifyItemRemoved(index)
                }
            } ?: let {
                // 说明是更新过来的数据
                // 重新请求数据
                data.clear()
                DataService.getEntryList(categoryInfo.id)?.forEach {
                    data.add(Openable(data = it))
                }
                // 重设 排序
                if (sortType == SORT_TYPE_TIME) {
                    data.sortByDescending { it.data.date }
                    rvCategory.adapter?.notifyDataSetChanged()
                    sortType = SORT_TYPE_TIME
                    sort.text = "最近创建排序"
                } else if (sortType == SORT_TYPE_TIME_ORDER) {
                    data.sortBy { it.data.date }
                    rvCategory.adapter?.notifyDataSetChanged()
                    sortType = SORT_TYPE_TIME_ORDER
                    sort.text = "创建时间排序"
                }
            }
            updateEntryNum()
            updateEmptyTip()
        }

        private fun find(bean: BaseEntryBean): Int? {
            data.forEachIndexed { index, openable ->
                if (openable.data == bean) {
                    return index
                }
            }
            return null
        }
    }

    // 修改图片Entry选图回调
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            if (requestCode == ENTRY_IMAGE_REQUEST) {
                // 图片收藏选图成功
                it.data?.let { uri ->
                    Ln.i("ImageResult", "$uri")
                    EventBus.getDefault().post(CategoryImageCompleteEvent(uri))
                }
            } else {
                // do nothing
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
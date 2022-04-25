package com.app.dixon.facorites.core.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.ex.shakeTipIfEmpty
import com.app.dixon.facorites.core.ex.try2URL
import com.dixon.dlibrary.util.ScreenUtil
import kotlinx.android.synthetic.main.app_dialog_create_entry_content.*
import kotlinx.android.synthetic.main.app_item_category_spinner.view.*
import java.util.*

// TODO 优化代码
class CreateEntryDialog(context: Context, val link: String? = null, private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！"), val editType: Int = EDIT_TYPE_CREATE) :
    BaseDialog(context) {

    companion object {
        const val EDIT_TYPE_CREATE = 0
        const val EDIT_TYPE_UPDATE = 1
    }

    // 带入的Entry
    private var tapeEntry: LinkEntryBean? = null

    constructor(
        context: Context, entry: LinkEntryBean,
        callback: Callback<BaseEntryBean> = CommonCallback("更新成功！"),
        editType: Int = EDIT_TYPE_UPDATE
    ) : this(
        context,
        callback = callback,
        editType = editType
    ) {
        this.tapeEntry = entry
    }

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_create_entry_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        tvCreate.setOnClickListener {
            val text = etEntryInput.text.toString()
            val title = etEntryTitle.text.toString()
            val remark = etEntryRemark.text.toString()
            if (text.isNotEmpty() && title.isNotEmpty()) {
                if (editType == EDIT_TYPE_CREATE) {
                    DataService.createEntry(
                        LinkEntryBean(
                            link = text,
                            title = title,
                            remark = remark,
                            date = Date().time,
                            belongTo = spinner.selectedItemId
                        ),
                        callback
                    )
                } else if (editType == EDIT_TYPE_UPDATE) {
                    tapeEntry?.let {
                        DataService.updateEntry(
                            it,
                            LinkEntryBean(
                                link = text,
                                title = title,
                                remark = remark,
                                date = it.date,
                                belongTo = spinner.selectedItemId
                            ),
                            callback
                        )
                    }
                }
                dismiss()
            } else {
                // 未填数据提示
                etEntryInput.shakeTipIfEmpty()
                etEntryTitle.shakeTipIfEmpty()
            }
        }
        // 粘贴时解析
        etEntryInput.setOnPasteListener {
            parseTitle(it)
        }
        // 带入弹窗的链接进行解析
        // 比如外部跳转
        link?.let {
            etEntryInput.setText(it)
            parseTitle(it)
        }
        initSpinner()
        // 带入完整数据 比如更新
        tapeEntry?.let {
            etEntryInput.setText(it.link)
            etEntryTitle.setText(it.title)
            etEntryRemark.setText(it.remark)
            var spinnerIndex: Int? = null
            DataService.getCategoryList().forEachIndexed { index, categoryInfoBean ->
                if (categoryInfoBean.id == it.belongTo) {
                    spinnerIndex = index
                }
            }
            spinnerIndex?.let { index -> spinner.setSelection(index) }
        }
    }

    private fun parseTitle(link: String) {
        if (link.isNotEmpty()) {
            etEntryTitle.hint = "标题解析中.."
            JSoupService.askTitle(link.try2URL(), { data ->
                etEntryTitle.hint = "标题"
                etEntryTitle.setText(data)
            }, {
                etEntryTitle.hint = "标题解析失败，请自行输入"
            })
        }
    }

    // 下拉选择框
    private fun initSpinner() {
        val adapter = SpinnerAdapter(context, DataService.getCategoryList())
        spinner.adapter = adapter
        spinner.setSelection(0)
    }

    private class SpinnerAdapter(val context: Context, val data: List<CategoryInfoBean>) : BaseAdapter() {

        override fun getCount(): Int = data.size

        override fun getItem(position: Int): Any = data[position]

        override fun getItemId(position: Int): Long = data[position].id

        override fun getView(position: Int, originView: View?, parent: ViewGroup?): View {
            val contentView = originView ?: LayoutInflater.from(context).inflate(R.layout.app_item_category_spinner, parent, false)
            val holder = originView?.let {
                (it.tag as ViewHolder)
            } ?: let {
                ViewHolder(contentView).apply {
                    contentView.tag = this
                }
            }
            holder.itemView.tvName.text = data[position].name
            return contentView
        }

        class ViewHolder(val itemView: View)
    }
}
package com.app.dixon.facorites.core.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.ex.shakeTip
import com.app.dixon.facorites.core.ex.shakeTipIfEmpty
import com.app.dixon.facorites.core.ex.try2URL
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_create_entry_content.*
import kotlinx.android.synthetic.main.app_item_category_spinner.view.*
import java.util.*

class CreateEntryDialog(context: Context, val link: String? = null, private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！")) :
    BaseDialog(context) {

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
                DataService.createEntry(
                    spinner.selectedItemId,
                    LinkEntryBean(
                        link = text,
                        title = title,
                        remark = remark,
                        date = Date().time,
                        belongTo = spinner.selectedItemId
                    ),
                    callback
                )
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
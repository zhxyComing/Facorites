package com.app.dixon.facorites.page.entry

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import kotlinx.android.synthetic.main.app_item_entry.view.*

/**
 * 全路径：com.app.dixon.facorites.page.entry
 * 类描述：entry 列表 item
 * 创建人：xuzheng
 * 创建时间：4/7/22 5:03 PM
 */
class EntryAdapter(val context: Context, val data: List<Openable<BaseEntryBean>>) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    class EntryViewHolder(item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val item = LayoutInflater.from(context).inflate(R.layout.app_item_entry, parent, false)
        return EntryViewHolder(item)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val openable = data[position]
        val entry = openable.data
        (entry as? LinkEntryBean)?.let {
            holder.itemView.linkCard.apply {
                setLinkEntry(entry)
                setOnClickListener {
                    // 0. 查找当前列表的打开状态
                    val openIndex = findOpenIndex()
                    // 1. 改变当前卡片的打开状态
                    openable.isOpen = !openable.isOpen
                    // 2. 如果是打开卡片，则把旧的已开卡片关闭，并记录新的
                    if (openable.isOpen) {
                        if (openIndex != -1) {
                            data[openIndex].isOpen = false
                            notifyItemChanged(openIndex)
                        }
                    }
                }
                if (openable.isOpen) {
                    openSubCardAtOnce()
                } else {
                    closeSubCardAtOnce()
                }
            }
        }
    }

    private fun findOpenIndex(): Int {
        data.forEachIndexed { index, openable ->
            if (openable.isOpen) {
                return index
            }
        }
        return -1
    }

    override fun getItemCount(): Int = data.size
}
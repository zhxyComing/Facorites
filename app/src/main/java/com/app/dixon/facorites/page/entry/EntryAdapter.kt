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
class EntryAdapter(val context: Context, val data: List<BaseEntryBean>) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    class EntryViewHolder(item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val item = LayoutInflater.from(context).inflate(R.layout.app_item_entry, parent, false)
        return EntryViewHolder(item)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = data[position]
        (entry as? LinkEntryBean)?.let {
            holder.itemView.linkCard.setLinkEntry(entry)
        }
    }

    override fun getItemCount(): Int = data.size
}
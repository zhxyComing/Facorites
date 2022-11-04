package com.app.dixon.facorites.page.entry

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.ex.process
import com.app.dixon.facorites.core.util.normalFont
import kotlinx.android.synthetic.main.app_item_entry.view.*

/**
 * 全路径：com.app.dixon.facorites.page.entry
 * 类描述：entry 列表 item
 * 创建人：xuzheng
 * 创建时间：4/7/22 5:03 PM
 */
class EntryAdapter(val context: Context, val data: List<Openable<BaseEntryBean>>, private val showCategoryTag: Boolean = true) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>(), Filterable {

    class EntryViewHolder(item: View) : RecyclerView.ViewHolder(item)

    // 过滤的子数据
    var filterData: MutableList<Openable<BaseEntryBean>>? = null

    var onFilterEmptyListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val item = LayoutInflater.from(context).inflate(R.layout.app_item_entry, parent, false)
        item.normalFont()
        return EntryViewHolder(item)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val openable = obtainData()[position]
        val entry = openable.data
        entry.process({ linkEntry ->
            holder.itemView.linkCard.setLinkEntry(linkEntry, showCategoryTag)
        }, { imageEntry ->
            holder.itemView.linkCard.setImageEntry(imageEntry, showCategoryTag)
        }, { categoryEntry ->
            holder.itemView.linkCard.setCategoryEntry(categoryEntry, showCategoryTag)
        }, { wordEntry ->
            holder.itemView.linkCard.setWordEntry(wordEntry, showCategoryTag)
        }, { galleryEntry ->
            holder.itemView.linkCard.setGalleryEntry(galleryEntry, showCategoryTag)
        }, { videoEntry ->
            holder.itemView.linkCard.setVideoEntry(videoEntry, showCategoryTag)
        }, { fileEntry ->
            holder.itemView.linkCard.setFileEntry(fileEntry, showCategoryTag)
        })
        holder.itemView.linkCard.apply {
            setOnClickListener {
                // 0. 查找当前列表的打开状态
                val openIndex = findOpenIndex()
                // 1. 改变当前卡片的打开状态
                openable.isOpen = !openable.isOpen
                // 2. 如果是打开卡片，则把旧的已开卡片关闭，并记录新的
                if (openable.isOpen) {
                    if (openIndex != -1) {
                        obtainData()[openIndex].isOpen = false
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

    private fun findOpenIndex(): Int {
        obtainData().forEachIndexed { index, openable ->
            if (openable.isOpen) {
                return index
            }
        }
        return -1
    }

    override fun getItemCount(): Int = obtainData().size

    private fun obtainData() = filterData ?: let { data }

    // 支持筛选
    override fun getFilter(): Filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString: String = constraint.toString()
            filterData ?: let {
                filterData = mutableListOf()
            }
            filterData?.clear()
            if (charString.isEmpty()) {
                //没有过滤的内容，则使用源数据
                filterData?.addAll(data)
            } else {
                data.forEach {
                    it.data.process({ linkEntry ->
                        if (linkEntry.title.toLowerCase().contains(charString.toLowerCase()) ||
                            linkEntry.link.toLowerCase().contains(charString.toLowerCase())
                        ) {
                            filterData?.add(it)
                        }
                    }, { imageEntry ->
                        if (imageEntry.title.toLowerCase().contains(charString.toLowerCase())) {
                            filterData?.add(it)
                        }
                    }, { categoryEntry ->
                        if (categoryEntry.categoryInfoBean.name.toLowerCase().contains(charString.toLowerCase())) {
                            filterData?.add(it)
                        }
                    }, { wordEntry ->
                        if (wordEntry.content.toLowerCase().contains(charString.toLowerCase())) {
                            filterData?.add(it)
                        }
                    }, { galleryEntry ->
                        if (galleryEntry.title.toLowerCase().contains(charString.toLowerCase())) {
                            filterData?.add(it)
                        }
                    }, { videoEntry ->
                        if (videoEntry.title.toLowerCase().contains(charString.toLowerCase())) {
                            filterData?.add(it)
                        }
                    }, { fileEntry ->
                        if (fileEntry.title.toLowerCase().contains(charString.toLowerCase())) {
                            filterData?.add(it)
                        }
                    })
                }
            }

            val filterResults = FilterResults()
            filterResults.values = filterData
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            (results?.values as? MutableList<Openable<BaseEntryBean>>)?.let {
                filterData = it
                notifyDataSetChanged()
                if (filterData?.isEmpty() == true) {
                    onFilterEmptyListener?.invoke()
                }
            }
        }
    }
}
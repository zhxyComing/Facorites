package com.app.dixon.facorites.page.gallery.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.ex.setImageByPath
import kotlinx.android.synthetic.main.app_item_gallery_import.view.*
import kotlinx.android.synthetic.main.app_item_gallery_import_footer.view.*

private const val ITEM_TYPE_CONTENT = 0
private const val ITEM_TYPE_FOOTER = 1

// 有1个添加图片的Footer
class GalleryImportAdapter(val context: Context, val data: List<String>, private val addClickAction: () -> Unit, private val removeClickAction: (pos: Int) -> Unit, val hideFooter: Boolean = false) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class GalleryViewHolder(item: View) : RecyclerView.ViewHolder(item)

    class FooterViewHolder(item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_FOOTER) {
            val item = LayoutInflater.from(context).inflate(R.layout.app_item_gallery_import_footer, parent, false)
            FooterViewHolder(item)
        } else {
            val item = LayoutInflater.from(context).inflate(R.layout.app_item_gallery_import, parent, false)
            GalleryViewHolder(item)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GalleryViewHolder) {
            val path: String = data[position]
            holder.itemView.ivPic.setImageByPath(path, 100, 100)
            holder.itemView.ivRemove.setOnClickListener {
                // 删除了元素后pos可能已经越界了，所以不能直接传pos
                removeClickAction.invoke(data.indexOf(path))
            }
            holder.itemView.cardContent.setOnClickListener {
                PageJumper.openImagePage(context, path)
            }
        } else if (holder is FooterViewHolder) {
            holder.itemView.cardAdd.setOnClickListener {
                addClickAction.invoke()
            }
        }
    }

    override fun getItemCount(): Int = if (hideFooter) data.size else data.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position >= data.size) ITEM_TYPE_FOOTER
        else ITEM_TYPE_CONTENT
    }

}

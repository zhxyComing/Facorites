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

class GalleryListAdapter(val context: Context, val data: List<String>, private val onClickAction: (index: Int, path: String) -> Unit) : RecyclerView.Adapter<GalleryListAdapter.GalleryViewHolder>() {

    class GalleryViewHolder(item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val item = LayoutInflater.from(context).inflate(R.layout.app_item_gallery_list, parent, false)
        return GalleryViewHolder(item)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val path: String = data[position]
        holder.itemView.ivPic.setImageByPath(path, 100, 100)
        holder.itemView.ivPic.setOnClickListener {
//            PageJumper.openImagePage(context, path)
            onClickAction.invoke(position, path)
        }
    }

    override fun getItemCount(): Int = data.size

}

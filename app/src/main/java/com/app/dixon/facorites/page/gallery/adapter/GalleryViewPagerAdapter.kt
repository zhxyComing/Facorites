package com.app.dixon.facorites.page.gallery.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.dixon.facorites.R
import kotlinx.android.synthetic.main.item_gallery_view_pager.view.*

class GalleryViewPagerAdapter(val context: Context, val data: List<String>) : RecyclerView.Adapter<GalleryViewPagerAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_view_pager, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.photoView.setImageURI(Uri.parse(data[position]))
    }

    override fun getItemCount(): Int = data.size
}
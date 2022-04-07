package com.app.dixon.facorites.page.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import kotlinx.android.synthetic.main.app_item_category.view.*

/**
 * 全路径：com.app.dixon.facorites.page.category
 * 类描述：分类Adapter
 * 创建人：xuzheng
 * 创建时间：4/7/22 11:23 AM
 */
class CategoryAdapter(val context: Context, val data: List<CategoryInfoBean>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val item = LayoutInflater.from(context).inflate(R.layout.app_item_category, parent, false)
        return CategoryViewHolder(item)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.itemView.tvTitle.text = data[position].name
        holder.itemView.tvDesc.text = "${data[position].id}"
        holder.itemView.cardContent.setOnClickListener {
            PageJumper.openEntryPage(context, data[position])
        }
    }

    override fun getItemCount(): Int = data.size

    class CategoryViewHolder(item: View) : RecyclerView.ViewHolder(item)
}
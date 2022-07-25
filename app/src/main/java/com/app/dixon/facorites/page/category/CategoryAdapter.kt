package com.app.dixon.facorites.page.category

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.setImageByPath
import com.app.dixon.facorites.core.ex.setImageByUri
import com.app.dixon.facorites.core.ex.showIf
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.util.TimeUtils
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.core.view.EditCategoryDialog
import com.dixon.dlibrary.util.FontUtil
import kotlinx.android.synthetic.main.app_item_category.view.*

/**
 * 全路径：com.app.dixon.facorites.page.category
 * 类描述：分类Adapter
 * 创建人：xuzheng
 * 创建时间：4/7/22 11:23 AM
 *
 */
class CategoryAdapter(val context: Context, val data: List<CategoryInfoBean>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val item = LayoutInflater.from(context).inflate(R.layout.app_item_category, parent, false)
        item.normalFont()
        item.tvTitle.mediumFont()
        item.topTag.mediumFont()
        return CategoryViewHolder(item)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val info = data[position]
        holder.itemView.tvTitle.text = info.name
        holder.itemView.tvDesc.text = "创建于${TimeUtils.friendlyTime(info.id)} · 包含${DataService.getEntryList(info.id)?.size}条收藏"
        holder.itemView.topTag.showIf(info.topTimeMs != 0L)
        holder.itemView.cardContent.setOnClickListener {
            PageJumper.openEntryPage(context, data[position])
        }
        holder.itemView.setOnLongClickListener {
            EditCategoryDialog(context, info).show()
            true
        }
        info.bgPath?.let {
            Ln.i("CategoryAdapter", it)
            holder.itemView.bgView.setImageByPath(it)
        } ?: holder.itemView.bgView.setImageByUri(Uri.parse("res://com.app.dixon.facorites/" + R.drawable.app_icon_category_bg))
    }

    override fun getItemCount(): Int = data.size

    class CategoryViewHolder(item: View) : RecyclerView.ViewHolder(item)
}
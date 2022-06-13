package com.app.dixon.facorites.page.note

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.NoteBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.NoteService
import com.app.dixon.facorites.core.ex.process
import com.app.dixon.facorites.core.util.ClipUtil
import com.app.dixon.facorites.core.util.TimeUtils
import com.dixon.dlibrary.util.FontUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_item_note.view.*

/**
 * 全路径：com.app.dixon.facorites.page.category
 * 类描述：分类Adapter
 * 创建人：xuzheng
 * 创建时间：4/7/22 11:23 AM
 *
 */
class NoteAdapter(val context: Context, val data: List<NoteBean>) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val item = LayoutInflater.from(context).inflate(R.layout.app_item_note, parent, false)
        FontUtil.font(item.tvTip)
        return NoteViewHolder(item)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = data[position]
        holder.itemView.tvContent.text = note.content
        holder.itemView.tvTip.text = "编辑于${TimeUtils.friendlyTime(note.id)}"
        holder.itemView.tvJump.setOnClickListener {
            // 找到对应的条目
            findEntry(note.belongTo)?.let { entry ->
                entry.process({ linkEntry ->
                    PageJumper.openBrowsePage(context, linkEntry.belongTo, linkEntry.date, linkEntry.link)
                }, { imageEntry ->
                    // 暂时不会走到
                    PageJumper.openImagePage(context, imageEntry.path)
                })
            } ?: let {
                // 未找到 说明条目已删除
                ToastUtil.toast("笔记所属收藏找不到啦～")
            }
        }
        holder.itemView.tvDelete.setOnClickListener {
            NoteService.delete(note)
        }
        holder.itemView.tvContent.setOnLongClickListener {
            ClipUtil.copyToClip(context, holder.itemView.tvContent.text.toString())
            ToastUtil.toast("已复制")
            true
        }
    }

    private fun findEntry(id: Long): BaseEntryBean? {
        DataService.getCategoryList().forEach { category ->
            DataService.getEntryList(category.id)?.let { entryList ->
                entryList.forEach {
                    if (it.date == id) {
                        return it
                    }
                }
            }
        }
        return null
    }

    override fun getItemCount(): Int = data.size

    class NoteViewHolder(item: View) : RecyclerView.ViewHolder(item)
}
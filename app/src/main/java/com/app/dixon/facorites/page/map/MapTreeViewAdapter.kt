package com.app.dixon.facorites.page.map

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import kotlinx.android.synthetic.main.app_content_tree_map.view.*
import treeview.adapter.DrawInfo
import treeview.adapter.TreeViewAdapter
import treeview.adapter.TreeViewHolder
import treeview.line.AngledLine
import treeview.line.BaseLine
import treeview.model.NodeModel


class MapTreeViewAdapter(val itemClick: ((BaseNodeData) -> Unit)? = null) : TreeViewAdapter<BaseNodeData>() {

    private val dashLine = AngledLine(Color.parseColor("#FF2442"), 4)

    override fun onCreateViewHolder(viewGroup: ViewGroup, node: NodeModel<BaseNodeData>): TreeViewHolder<BaseNodeData> {
        val contentView = LayoutInflater.from(viewGroup.context).inflate(R.layout.app_content_tree_map, viewGroup, false)
        return TreeViewHolder(contentView, node)
    }

    override fun onBindViewHolder(holder: TreeViewHolder<BaseNodeData>) {
        val itemView: View = holder.view
        val node: NodeModel<BaseNodeData> = holder.node
        val nodeData = node.value
        if (nodeData is CategoryNodeData) {
            itemView.tvItemTitle.setBackgroundResource(R.drawable.app_select_map_category)
            itemView.tvItemTitle.mediumFont()
        } else {
            itemView.tvItemTitle.setBackgroundResource(R.drawable.app_select_map_entry)
            itemView.tvItemTitle.normalFont()
        }
        itemView.tvItemTitle.text = nodeData.name
        itemView.tvItemTitle.setOnClickListener {
            itemClick?.invoke(nodeData)
        }
    }

    override fun onDrawLine(drawInfo: DrawInfo?): BaseLine? = dashLine
}
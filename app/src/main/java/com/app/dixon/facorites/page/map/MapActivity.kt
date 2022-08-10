package com.app.dixon.facorites.page.map

import android.graphics.Color
import android.os.Bundle
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.MAP_CATEGORY
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.data.bean.CategoryEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.ImageEntryBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.isValidUrl
import com.app.dixon.facorites.core.ex.process
import com.app.dixon.facorites.core.util.ClipUtil
import com.dixon.dlibrary.util.ToastUtil
import com.gyso.treeview.adapter.TreeViewAdapter
import com.gyso.treeview.layout.RightTreeLayoutManager
import com.gyso.treeview.layout.TreeLayoutManager
import com.gyso.treeview.line.BaseLine
import com.gyso.treeview.line.StraightLine
import com.gyso.treeview.model.NodeModel
import com.gyso.treeview.model.TreeModel
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity : BaseActivity() {

    private lateinit var categoryInfoBean: CategoryInfoBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        intent.getParcelableExtra<CategoryInfoBean>(MAP_CATEGORY)?.let {
            categoryInfoBean = it
        } ?: let {
            finish()
            return
        }

        val adapter: TreeViewAdapter<*> = MapTreeViewAdapter { baseNodeData ->
            if (baseNodeData is CategoryNodeData) {
                PageJumper.openEntryPage(this, baseNodeData.categoryInfoBean)
            } else if (baseNodeData is EntryNodeData) {
                val entry = baseNodeData.entry
                entry.process({
                    if (it.link.isValidUrl()) {
                        PageJumper.openBrowsePage(this, it.belongTo, it.date, it.link, it.title)
                    } else {
                        ClipUtil.copyToClip(this, it.link)
                        ToastUtil.toast("非网页链接，已复制到剪贴板，请自行选择合适程序")
                    }
                }, {
                    PageJumper.openImagePage(this, it.path)
                }, {})
            }
        }
        val line: BaseLine = StraightLine(Color.parseColor("#4DB6AC"), 2) // ? 有什么用
        val treeLayoutManager: TreeLayoutManager = RightTreeLayoutManager(this, 50, 20, line)

        // adapter & layout
        treeView.adapter = adapter
        treeView.setTreeLayoutManager(treeLayoutManager)

        // 数据
        // rootNode
        val rootNode: NodeModel<BaseNodeData> = NodeModel(CategoryNodeData(categoryInfoBean, categoryInfoBean.name))
        val treeModel: TreeModel<BaseNodeData> = TreeModel(rootNode)
        addNode(treeModel, categoryInfoBean.id, rootNode)

        adapter.treeModel = treeModel
    }

    private fun addNode(treeModel: TreeModel<BaseNodeData>, categoryId: Long, rootNode: NodeModel<BaseNodeData>) {
        DataService.getEntryList(categoryId)?.sortedByDescending { it.date }?.forEach {
            if (it is CategoryEntryBean) {
                val nodeTemp: NodeModel<BaseNodeData> = NodeModel<BaseNodeData>(CategoryNodeData(it.categoryInfoBean, it.categoryInfoBean.name))
                treeModel.addNode(rootNode, nodeTemp)
                addNode(treeModel, it.categoryInfoBean.id, nodeTemp)
            } else {
                var name = ""
                if (it is LinkEntryBean) {
                    name = it.title
                } else if (it is ImageEntryBean) {
                    name = it.title
                }
                val nodeTemp: NodeModel<BaseNodeData> = NodeModel<BaseNodeData>(EntryNodeData(it, name))
                treeModel.addNode(rootNode, nodeTemp)
            }
        }
    }
}
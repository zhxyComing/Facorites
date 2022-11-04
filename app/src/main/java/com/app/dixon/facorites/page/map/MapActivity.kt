package com.app.dixon.facorites.page.map

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.MAP_CATEGORY
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.data.bean.*
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.isValidUrl
import com.app.dixon.facorites.core.ex.process
import com.app.dixon.facorites.core.util.ClipUtil
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.util.OpenFileUtil
import com.app.dixon.facorites.core.util.mediumFont
import com.dixon.dlibrary.util.HandlerUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.activity_map.*
import treeview.adapter.TreeViewAdapter
import treeview.layout.RightTreeLayoutManager
import treeview.layout.TreeLayoutManager
import treeview.line.BaseLine
import treeview.line.StraightLine
import treeview.listener.TreeViewControlListener
import treeview.model.NodeModel
import treeview.model.TreeModel

// TODO 优化代码
class MapActivity : BaseActivity() {

    private lateinit var categoryInfoBean: CategoryInfoBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        editMode.mediumFont()

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
                }, {

                }, {
                    PageJumper.openWordPage(this, it.content)
                }, {
                    PageJumper.openGalleryPage(this, it.path, it.title)
                }, {
                    PageJumper.openVideoPlayerPage(this, it.path)
                }, {
                    OpenFileUtil.openFile(it.path)
                })
            }
        }
        val line: BaseLine = StraightLine(Color.parseColor("#2E7D32"), 2) // ? 有什么用
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

        // editor
        with(treeView.editor) {
            focusMidLocation()
            editMode.setOnCheckedChangeListener { _, isChecked ->
                requestMoveNodeByDragging(isChecked)
            }

            treeView.setTreeViewControlListener(object : TreeViewControlListener {
                // 缩放
                override fun onScaling(state: Int, percent: Int) {

                }

                // 拖动
                override fun onDragMoveNodesHit(draggingNode: NodeModel<*>?, hittingNode: NodeModel<*>?, draggingView: View?, hittingView: View?) {
                    // 拖动过程中每次节点的碰撞都会触发，由于检测不到最终链接，所以无法使用
                }

                // 拖动结束
                override fun onDragOver(draggingNode: NodeModel<*>?, newParentNode: NodeModel<*>?, pastParentNode: NodeModel<*>?) {
                    Ln.i("拖动结束", "拖动节点_$draggingNode 新父级节点_$newParentNode 老父级节点_$pastParentNode")
                    if (draggingNode != null && newParentNode != null && pastParentNode != null) {
                        // 场景一 文件夹没变
                        if (newParentNode == pastParentNode) return
                        // 场景二 拖动文件到文件下 无效
                        if (newParentNode.value is EntryNodeData) {
                            ToastUtil.toast("不能将收藏归属到另一收藏下哦～")
                            removeNode(draggingNode)
                            addChildNodes(pastParentNode, draggingNode)
                            adapter.treeModel = treeModel
                            return
                        }
                        // 场景三 拖动文件到文件夹下
                        if (newParentNode.value is CategoryNodeData && draggingNode.value is EntryNodeData) {
                            val updater = (draggingNode.value as EntryNodeData).entry
                            val newBelongTo = (newParentNode.value as CategoryNodeData).categoryInfoBean.id
                            var newEntry: BaseEntryBean? = null
                            updater.process({
                                newEntry = LinkEntryBean(it.link, it.title, it.remark, it.schemeJump, it.date, newBelongTo, it.star)
                            }, {
                                newEntry = ImageEntryBean(it.path, it.title, it.hideBg, it.date, newBelongTo, it.star)
                            }, {
                                //
                            }, {
                                newEntry = WordEntryBean(it.content, it.date, newBelongTo, it.star)
                            }, {
                                newEntry = GalleryEntryBean(it.path, it.title, it.date, newBelongTo, it.star)
                            }, {
                                newEntry = VideoEntryBean(it.path, it.title, it.date, newBelongTo, it.star)
                            }, {
                                newEntry = FileEntryBean(it.path, it.title, it.date, newBelongTo, it.star)
                            })
                            newEntry?.let {
                                DataService.updateEntry(updater, it)
                            }
                            return
                        }
                        // 场景四 拖动文件夹到文件夹下
                        if (newParentNode.value is CategoryNodeData && draggingNode.value is CategoryNodeData) {
                            val updater = (draggingNode.value as CategoryNodeData).categoryInfoBean
                            val newBelongTo = (newParentNode.value as CategoryNodeData).categoryInfoBean.id
                            // 找到文件夹对应的条目，根文件夹没有所属条目，所以无法走下面逻辑，也就无法移到子文件夹下
                            DataService.getCategoryList().forEach { categoryInfo ->
                                DataService.getEntryList(categoryInfo.id)?.forEach { entry ->
                                    if (entry.date == updater.id && entry is CategoryEntryBean) {
                                        val newCategoryInfoBean = CategoryInfoBean(
                                            entry.categoryInfoBean.id,
                                            entry.categoryInfoBean.name,
                                            entry.categoryInfoBean.bgPath,
                                            entry.categoryInfoBean.topTimeMs,
                                            belongTo = newBelongTo
                                        )
                                        val newEntry = CategoryEntryBean(
                                            newCategoryInfoBean,
                                            entry.date,
                                            belongTo = newBelongTo,
                                            entry.star
                                        )
                                        DataService.updateEntry(entry, newEntry)
                                        return
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun addNode(treeModel: TreeModel<BaseNodeData>, categoryId: Long, rootNode: NodeModel<BaseNodeData>) {
        DataService.getEntryList(categoryId)?.sortedByDescending { it.date }?.forEach {
            if (it is CategoryEntryBean) {
                val nodeTemp: NodeModel<BaseNodeData> = NodeModel<BaseNodeData>(CategoryNodeData(it.categoryInfoBean, it.categoryInfoBean.name))
                treeModel.addNode(rootNode, nodeTemp)
                addNode(treeModel, it.categoryInfoBean.id, nodeTemp)
            } else {
                var name = ""
                it.process({ link ->
                    name = link.title
                }, { image ->
                    name = image.title
                }, {}, { word ->
                    name = word.content
                }, { gallery ->
                    name = gallery.title
                }, { video ->
                    name = video.title
                }, { file ->
                    name = file.title
                })
                val nodeTemp: NodeModel<BaseNodeData> = NodeModel<BaseNodeData>(EntryNodeData(it, name))
                treeModel.addNode(rootNode, nodeTemp)
            }
        }
    }

    override fun statusBarColor(): Int = R.color.md_grey_300
}
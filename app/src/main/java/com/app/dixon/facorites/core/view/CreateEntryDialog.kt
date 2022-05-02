package com.app.dixon.facorites.core.view

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.data.bean.ImageEntryBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.BitmapIOService
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.enum.EntryType
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.ImageSelectHelper
import com.app.dixon.facorites.page.category.event.CategoryImageCompleteEvent
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_create_entry_content.*
import kotlinx.android.synthetic.main.app_item_category_spinner.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

const val ENTRY_IMAGE_REQUEST = 101

// 创建用构造函数
class CreateEntryDialog(
    context: Context,
    private val linkFromShare: String? = null,
    private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！")
) :
    BaseDialog(context) {

    companion object {
        const val EDIT_TYPE_CREATE = 0
        const val EDIT_TYPE_UPDATE = 1
    }

    // 编辑类型 创建｜更新
    private var editType = EDIT_TYPE_CREATE

    // 数据类型 链接｜图片
    private var dataType: Int = EntryType.LINK

    // 用于更新带入的Entry
    private var tapeEntry: BaseEntryBean? = null

    // 图片类型参数
    // 选图
    private var imagePath: String? = null

    // 更新用构造函数
    constructor(
        context: Context, entry: BaseEntryBean,
        callback: Callback<BaseEntryBean> = CommonCallback("更新成功！")
    ) : this(
        context,
        callback = callback,
    ) {
        this.editType = EDIT_TYPE_UPDATE
        this.tapeEntry = entry
    }

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_create_entry_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        initCommonLogic()
        initLinkLogic()
        initImageLayout()
    }

    // 通用逻辑的初始化
    // 1.EventBus注册；
    // 2.点击保存或取消；
    // 3.选择分类；
    private fun initCommonLogic() {
        EventBus.getDefault().register(this)
        tvCreate.setOnClickListener {
            if (dataType == EntryType.LINK) {
                saveOrUpdateLink()
            } else {
                saveOrUpdateImage()
            }
        }
        // 分类的下拉列表
        initSpinner()
        // 更新时默认选中已选的分类
        tapeEntry?.let {
            var spinnerIndex: Int? = null
            DataService.getCategoryList().forEachIndexed { index, categoryInfoBean ->
                if (categoryInfoBean.id == it.belongTo) {
                    spinnerIndex = index
                }
            }
            spinnerIndex?.let { index -> spinner.setSelection(index) }
        }
        // 切换数据类型
        layoutChange.setOnClickListener {
            changeType()
            if (dataType == EntryType.LINK) {
                showLinkUi()
                layoutChange.setImageResource(R.drawable.app_image_red)
            } else {
                showImageUi()
                layoutChange.setImageResource(R.drawable.app_link_red)
            }
        }
        // 更新 其它类型不显示 并往UI填充已有数据
        tapeEntry?.process({ linkEntry ->
            dataType = EntryType.LINK
            showLinkUi()
            etEntryInput.setText(linkEntry.link)
            etEntryTitle.setText(linkEntry.title)
            etEntryRemark.setText(linkEntry.remark)
        }, { imageEntry ->
            dataType = EntryType.IMAGE
            showImageUi()
            etImageTitle.setText(imageEntry.title)
            bgView.setImageByPath(imageEntry.path)
            imagePath = imageEntry.path
        })
        tapeEntry?.let { layoutChange.hide() }
    }

    // 链接数据初始化逻辑
    private fun initLinkLogic() {
        // 粘贴时解析 抽取Http链接
        etEntryInput.setOnPasteListener {
            ToastUtil.toast(it)
            val realUrl = it.tryExtractHttpByMatcher()
            if (it != realUrl) {
                // 原始字符串不是纯链接，且解析出了链接，重新设置Title为链接
                etEntryInput.post {
                    etEntryInput.setText(realUrl)
                }
            }
            parseTitle(realUrl)
        }
        // 带入弹窗的链接进行解析 比如外部分享跳转
        linkFromShare?.let {
            etEntryInput.setText(it)
            parseTitle(it)
        }
    }

    private fun showLinkUi() {
        linkLayout.show()
        imageLayout.hide()
    }

    private fun showImageUi() {
        linkLayout.hide()
        imageLayout.show()
    }

    // 保存链接
    private fun saveOrUpdateLink() {
        val text = etEntryInput.text.toString()
        val title = etEntryTitle.text.toString()
        val remark = etEntryRemark.text.toString()
        if (text.isNotEmpty() && title.isNotEmpty()) {
            if (editType == EDIT_TYPE_CREATE) {
                DataService.createEntry(
                    LinkEntryBean(
                        link = text,
                        title = title,
                        remark = remark,
                        date = Date().time,
                        belongTo = spinner.selectedItemId
                    ),
                    callback
                )
            } else if (editType == EDIT_TYPE_UPDATE) {
                (tapeEntry as? LinkEntryBean)?.let {
                    DataService.updateEntry(
                        it,
                        LinkEntryBean(
                            link = text,
                            title = title,
                            remark = remark,
                            schemeJump = it.schemeJump,
                            date = it.date,
                            belongTo = spinner.selectedItemId
                        ),
                        callback
                    )
                }
            }
            dismiss()
        } else {
            // 未填数据提示
            etEntryInput.shakeTipIfEmpty()
            etEntryTitle.shakeTipIfEmpty()
        }
    }

    // 保存图片
    private fun saveOrUpdateImage() {
        val title = etImageTitle.text.toString()
        val path = imagePath
        if (title.isNotEmpty() && !path.isNullOrEmpty()) {
            if (editType == EDIT_TYPE_CREATE) {
                DataService.createEntry(
                    ImageEntryBean(
                        path = path,
                        title = title,
                        date = Date().time,
                        belongTo = spinner.selectedItemId
                    ),
                    callback
                )
            } else if (editType == EDIT_TYPE_UPDATE) {
                (tapeEntry as? ImageEntryBean)?.let {
                    DataService.updateEntry(
                        it,
                        ImageEntryBean(
                            path = path,
                            title = title,
                            date = it.date,
                            belongTo = spinner.selectedItemId
                        ),
                        callback
                    )
                }
            }
            dismiss()
        } else {
            etImageTitle.shakeTipIfEmpty()
            if (path.isNullOrEmpty()) {
                selectImage.shakeTip()
            }
        }
    }

    // 图片类型
    private fun initImageLayout() {
        selectImage.setOnClickListener {
            // 打开图片选择
            ImageSelectHelper.openImageSelectPage(ENTRY_IMAGE_REQUEST)
        }
    }

    private fun changeType() {
        dataType = if (dataType == EntryType.LINK) {
            EntryType.IMAGE
        } else {
            EntryType.LINK
        }
    }

    private fun parseTitle(link: String) {
        if (link.isNotEmpty()) {
            etEntryTitle.hint = "标题解析中.."
            JSoupService.askTitle(link.try2URL(), { data ->
                etEntryTitle.hint = "标题"
                etEntryTitle.setText(data)
            }, {
                etEntryTitle.hint = "标题解析失败，请自行输入"
            })
        }
    }

    // 下拉选择框
    private fun initSpinner() {
        val adapter = SpinnerAdapter(context, DataService.getCategoryList())
        spinner.adapter = adapter
        spinner.setSelection(0)
    }

    private class SpinnerAdapter(val context: Context, val data: List<CategoryInfoBean>) : BaseAdapter() {

        override fun getCount(): Int = data.size

        override fun getItem(position: Int): Any = data[position]

        override fun getItemId(position: Int): Long = data[position].id

        override fun getView(position: Int, originView: View?, parent: ViewGroup?): View {
            val contentView = originView ?: LayoutInflater.from(context).inflate(R.layout.app_item_category_spinner, parent, false)
            val holder = originView?.let {
                (it.tag as ViewHolder)
            } ?: let {
                ViewHolder(contentView).apply {
                    contentView.tag = this
                }
            }
            holder.itemView.tvName.text = data[position].name
            return contentView
        }

        class ViewHolder(val itemView: View)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onImageSelectComplete(event: CategoryImageCompleteEvent) {
        // 转存图片到本地
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(event.uri))
        val absolutePath = BitmapIOService.createBitmapSavePath()
        tvTip.text = "导入中，请耐心等待"
        selectImage.isEnabled = false
        BitmapIOService.saveBitmap(absolutePath, bitmap, object : Callback<String> {
            override fun onSuccess(data: String) {
                ToastUtil.toast("图片转存成功")
                imagePath = data
                bgView.setImageByUri(event.uri)
                tvTip.text = "从相册选取图片"
                selectImage.isEnabled = true
            }

            override fun onFail(msg: String) {
                ToastUtil.toast("图片转存失败，请更换图片后重新尝试")
                tvTip.text = "从相册选取图片"
                selectImage.isEnabled = true
            }
        })
    }
}
package com.app.dixon.facorites.core.view

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
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

// TODO 优化代码
class CreateEntryDialog(context: Context, val link: String? = null, private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！"), val editType: Int = EDIT_TYPE_CREATE) :
    BaseDialog(context) {

    companion object {
        const val EDIT_TYPE_CREATE = 0
        const val EDIT_TYPE_UPDATE = 1
    }

    // 当前的编辑类型
    private var type: Int = EntryType.LINK

    // link类型参数
    // 带入的Entry
    private var tapeEntry: LinkEntryBean? = null

    // 图片类型参数
    // 选图
    private var imagePath: String? = null

    constructor(
        context: Context, entry: LinkEntryBean,
        callback: Callback<BaseEntryBean> = CommonCallback("更新成功！"),
        editType: Int = EDIT_TYPE_UPDATE
    ) : this(
        context,
        callback = callback,
        editType = editType
    ) {
        this.tapeEntry = entry
    }

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context)

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_create_entry_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        EventBus.getDefault().register(this)
        tvCreate.setOnClickListener {
            if (type == EntryType.LINK) {
                saveLink()
            } else {
                saveImage()
            }
        }
        // 粘贴时解析
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
        // 带入弹窗的链接进行解析
        // 比如外部跳转
        link?.let {
            etEntryInput.setText(it)
            parseTitle(it)
        }
        initSpinner()
        // 带入完整数据 比如更新
        tapeEntry?.let {
            etEntryInput.setText(it.link)
            etEntryTitle.setText(it.title)
            etEntryRemark.setText(it.remark)
            var spinnerIndex: Int? = null
            DataService.getCategoryList().forEachIndexed { index, categoryInfoBean ->
                if (categoryInfoBean.id == it.belongTo) {
                    spinnerIndex = index
                }
            }
            spinnerIndex?.let { index -> spinner.setSelection(index) }
        }

        initImageLayout()
    }

    // 保存链接
    private fun saveLink() {
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
                tapeEntry?.let {
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
    private fun saveImage() {
        val title = etImageTitle.text.toString()
        val path = imagePath
        if (title.isNotEmpty() && !path.isNullOrEmpty()) {
            DataService.createEntry(
                ImageEntryBean(
                    path = path,
                    title = title,
                    date = Date().time,
                    belongTo = spinner.selectedItemId
                ),
                callback
            )
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
        if (tapeEntry != null) {
            layoutChange.hide()
            return
        }
        layoutChange.setOnClickListener {
            changeType()
            if (type == EntryType.LINK) {
                linkLayout.show()
                imageLayout.hide()
                layoutChange.setImageResource(R.drawable.app_image_red)
            } else {
                linkLayout.hide()
                imageLayout.show()
                layoutChange.setImageResource(R.drawable.app_link_red)
            }
        }
        initImageLayoutUi()
    }

    private fun initImageLayoutUi() {
        selectImage.setOnClickListener {
            // 打开图片选择
            ImageSelectHelper.openImageSelectPage(ENTRY_IMAGE_REQUEST)
        }
    }

    private fun changeType() {
        type = if (type == EntryType.LINK) {
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
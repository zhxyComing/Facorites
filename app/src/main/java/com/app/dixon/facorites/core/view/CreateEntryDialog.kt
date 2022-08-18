package com.app.dixon.facorites.core.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.data.bean.*
import com.app.dixon.facorites.core.data.service.BitmapIOService
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.enum.EntryType
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.ImageSelectHelper
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.page.category.event.CategoryImageCompleteEvent
import com.app.dixon.facorites.page.gallery.adapter.GalleryImportAdapter
import com.app.dixon.facorites.page.gallery.event.GalleryCompleteEvent
import com.dixon.dlibrary.util.Ln
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.app_dialog_create_entry_content.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


const val ENTRY_IMAGE_REQUEST = 101
const val ENTRY_GALLERY_REQUEST = 102

// 创建用构造函数
class CreateEntryDialog(
    context: Context,
    private val linkFromShare: String? = null,
    private val callback: Callback<BaseEntryBean> = CommonCallback("创建成功！"),
    private val defaultCategory: Long? = null
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

    // 图片转存状态
    private var imageImporting: Boolean = false

    // 是否点击了确认
    private var hasSave = false

    // 图片集类型
    private var galleryPath = mutableListOf<String>()
    private var tempGalleryPath = mutableListOf<String>() // 新导入的临时图片 如果后续选择取消 则要删除掉这些新导入的图

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
        initGalleryLayout()
    }

    // 通用逻辑的初始化
    // 1.EventBus注册；
    // 2.点击保存或取消；
    // 3.选择分类；
    private fun initCommonLogic() {
        EventBus.getDefault().register(this)
        llContainer.normalFont()
        tvCreate.setOnClickListener {
            when (dataType) {
                EntryType.LINK -> saveOrUpdateLink()
                EntryType.IMAGE -> saveOrUpdateImage()
                EntryType.WORD -> saveOrUpdateWord()
                EntryType.GALLERY -> saveOrUpdateGallery()
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
            spinnerIndex?.let { index -> categoryChoose.setSelection(index) }
        }
        // 切换数据类型
        layoutChange.setOnClickListener {
            changeType()
            when (dataType) {
                EntryType.LINK -> {
                    showLinkUi()
                    layoutChange.setImageResource(R.drawable.app_image_red)
                }
                EntryType.IMAGE -> {
                    showImageUi()
                    layoutChange.setImageResource(R.drawable.app_icon_word_create_entry)
                }
                EntryType.WORD -> {
                    showWordUi()
                    layoutChange.setImageResource(R.drawable.app_icon_gallery_create_entry)
                }
                EntryType.GALLERY -> {
                    showGalleryUi()
                    layoutChange.setImageResource(R.drawable.app_link_red)
                }
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
        }, {
            // 文件夹以另外的方式创建
        }, { wordEntry ->
            dataType = EntryType.WORD
            showWordUi()
            etWordContent.setText(wordEntry.content)
        }, { galleryEntry ->
            dataType = EntryType.GALLERY
            galleryPath.addAll(galleryEntry.path)
            etGalleryTitle.setText(galleryEntry.title)
            showGalleryUi()
        })
        tapeEntry?.let { layoutChange.hide() }

        // 部分粗体
        etWordContent.mediumFont()
        tvLinkLayoutTitle.mediumFont()
        tvImageLayoutTitle.mediumFont()
        tvWordLayoutTitle.mediumFont()
        tvGalleryLayoutTitle.mediumFont()
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
            etWordContent.setText(it)
            parseTitle(it)
        }
    }

    private fun showLinkUi() {
        linkLayout.show()
        imageLayout.hide()
        wordLayout.hide()
        galleryLayout.hide()
    }

    private fun showImageUi() {
        linkLayout.hide()
        imageLayout.show()
        wordLayout.hide()
        galleryLayout.hide()
    }

    private fun showWordUi() {
        linkLayout.hide()
        imageLayout.hide()
        wordLayout.show()
        galleryLayout.hide()
    }

    private fun showGalleryUi() {
        linkLayout.hide()
        imageLayout.hide()
        wordLayout.hide()
        galleryLayout.show()
    }

    // 保存链接
    private fun saveOrUpdateLink() {
        val text = etEntryInput.text.toString()
        val title = etEntryTitle.text.toString()
        val remark = etEntryRemark.text.toString()
        val categoryId = categoryChoose.getSelectionData<CategoryInfoBean>()?.id
        if (text.isNotEmpty() && title.isNotEmpty() && categoryId != null) {
            if (editType == EDIT_TYPE_CREATE) {
                DataService.createEntry(
                    LinkEntryBean(
                        link = text,
                        title = title,
                        remark = remark,
                        date = Date().time,
                        belongTo = categoryId,
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
                            belongTo = categoryId,
                            star = it.star
                        ),
                        callback
                    )
                }
            }
            hasSave = true
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
        val categoryId = categoryChoose.getSelectionData<CategoryInfoBean>()?.id
        if (title.isNotEmpty() && !path.isNullOrEmpty() && !imageImporting && categoryId != null) {
            if (editType == EDIT_TYPE_CREATE) {
                DataService.createEntry(
                    ImageEntryBean(
                        path = path,
                        title = title,
                        date = Date().time,
                        belongTo = categoryId
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
                            belongTo = categoryId,
                            star = it.star
                        ),
                        callback
                    )
                }
            }
            hasSave = true
            dismiss()
        } else {
            etImageTitle.shakeTipIfEmpty()
            // 没选图或者导入过程中均不允许创建或更新
            if (path.isNullOrEmpty() || imageImporting) {
                selectImage.shakeTip()
            }
        }
    }

    // 保存语录
    private fun saveOrUpdateWord() {
        val content = etWordContent.text.toString()
        val categoryId = categoryChoose.getSelectionData<CategoryInfoBean>()?.id
        if (content.isNotEmpty() && categoryId != null) {
            if (editType == EDIT_TYPE_CREATE) {
                DataService.createEntry(
                    WordEntryBean(
                        content = content,
                        date = Date().time,
                        belongTo = categoryId,
                    ),
                    callback
                )
            } else if (editType == EDIT_TYPE_UPDATE) {
                (tapeEntry as? WordEntryBean)?.let {
                    DataService.updateEntry(
                        it,
                        WordEntryBean(
                            content = content,
                            date = it.date,
                            belongTo = categoryId,
                            star = it.star
                        ),
                        callback
                    )
                }
            }
            hasSave = true
            dismiss()
        } else {
            // 未填数据提示
            etWordContent.shakeTipIfEmpty()
        }
    }

    // 保存相册集
    private fun saveOrUpdateGallery() {
        val title = etGalleryTitle.text.toString()
        val categoryId = categoryChoose.getSelectionData<CategoryInfoBean>()?.id
        if (title.isNotEmpty() && categoryId != null && galleryPath.isNotEmpty()) {
            if (editType == EDIT_TYPE_CREATE) {
                DataService.createEntry(
                    GalleryEntryBean(
                        path = galleryPath,
                        title = title,
                        date = Date().time,
                        belongTo = categoryId,
                    ),
                    callback
                )
            } else if (editType == EDIT_TYPE_UPDATE) {
                (tapeEntry as? GalleryEntryBean)?.let {
                    DataService.updateEntry(
                        it,
                        GalleryEntryBean(
                            path = galleryPath,
                            title = title,
                            date = it.date,
                            belongTo = categoryId,
                            star = it.star
                        ),
                        callback
                    )
                }
            }
            hasSave = true
            dismiss()
        } else {
            // 未填数据提示
            etGalleryTitle.shakeTipIfEmpty()
            // 没选图或者导入过程中均不允许创建或更新
            if (galleryPath.isEmpty()) {
                rvGalleryList.shakeTip()
            }
        }
    }

    // 图片类型
    private fun initImageLayout() {
        selectImage.setOnClickListener {
            // 打开图片选择
            ImageSelectHelper.openImageSelectPage(ENTRY_IMAGE_REQUEST)
        }
        tvImageLayoutTip.setOnClickListener {
            TipDialog(
                context,
                content = "导入收藏夹子的图片有以下特性：\n\n1.应用外（如相册）不可见；\n" +
                        "\n2.删除手机（相册）的原图不会影响到收藏图。",
                title = "图片收藏提示"
            ).show()
        }
    }

    // 图片集类型
    private fun initGalleryLayout() {
        tvGalleryLayoutTip.setOnClickListener {
            TipDialog(
                context,
                content = "导入收藏夹子的图片有以下特性：\n\n1.应用外（如相册）不可见；\n" +
                        "\n2.删除手机（相册）的原图不会影响到收藏图。",
                title = "图片集收藏提示"
            ).show()
        }
        rvGalleryList.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        rvGalleryList.adapter = GalleryImportAdapter(context,
            galleryPath,
            addClickAction = {
                // 打开图片选择
                ImageSelectHelper.openGallerySelectPage(ENTRY_GALLERY_REQUEST)
            },
            removeClickAction = {
                // 移除Item
                val removePath = galleryPath.removeAt(it) // 删除galleryPath的数据，Adapter.data也会同步变化
                rvGalleryList.adapter?.notifyItemRemoved(it)
                // 临时导入的文件才删除
                // 如果是更新，则会带入图片，这些图片只有在确认更新时由DataService负责删除
                // 原则是：已保存的图片由数据管理器（DataService）负责删除，临时图片是业务产生的，由业务方（CreateEntryDialog）删除
                if (tempGalleryPath.contains(removePath)) {
                    BitmapIOService.deleteBitmap(removePath)
                    tempGalleryPath.remove(removePath)
                }
            })
    }

    private fun changeType() {
        dataType = when (dataType) {
            EntryType.LINK -> EntryType.IMAGE
            EntryType.IMAGE -> EntryType.WORD
            EntryType.WORD -> EntryType.GALLERY
            EntryType.GALLERY -> EntryType.LINK
            else -> EntryType.LINK
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
        val expendInfoList = mutableListOf<CustomSpinner.ExpandInfo<CategoryInfoBean>>()
        DataService.getCategoryList().forEach {
            expendInfoList.add(CustomSpinner.ExpandInfo(it.name, it.bgPath, it))
        }
        categoryChoose.setData(expendInfoList)
        categoryChoose.setShowPos(CustomSpinner.ShowPos.TOP)
        defaultCategory?.let { id ->
            expendInfoList.findIndexByCondition {
                it.data.id == id
            }?.let { index ->
                categoryChoose.setSelection(index)
            }
        }
    }

    override fun onDetachedFromWindow() {
        if (!hasSave) {
            deleteExpiredImportImage()
            deleteExpiredGalleryImage()
        } else {
            // 保存了，但是保存的不是对应类型
            if (dataType != EntryType.IMAGE) {
                deleteExpiredImportImage()
            }
            if (dataType != EntryType.GALLERY) {
                deleteExpiredGalleryImage()
            }
        }
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    // 删除过期的导入图片
    private fun deleteExpiredImportImage() {
        imagePath?.let {
            var deleteExpiredImage = true
            (tapeEntry as? ImageEntryBean)?.let { tapeImageBean ->
                // 带入更新的图片在确认修改之前不删除
                if (tapeImageBean.path == it) {
                    deleteExpiredImage = false
                }
            }
            if (deleteExpiredImage) {
                BitmapIOService.deleteBitmap(it)
            }
        }
    }

    // 删除过期的导入图片集
    private fun deleteExpiredGalleryImage() {
        tempGalleryPath.forEach {
            BitmapIOService.deleteBitmap(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onImageSelectComplete(event: CategoryImageCompleteEvent) {
        // 转存之前先把之前导入的图片删掉
        deleteExpiredImportImage()
        // 图片信息
        var rotate = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.contentResolver.openInputStream(event.uri)?.let {
                val imageRotation = ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                if (imageRotation == ExifInterface.ORIENTATION_ROTATE_90 || imageRotation == ExifInterface.ORIENTATION_ROTATE_270) {
                    rotate = true
                }
            }
        }
        // 转存图片到本地
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(event.uri))
        // iOS 拍出的图片带旋转角，要在导入时转为旋转后的图片
        if (rotate) {
            val m = Matrix()
            m.setRotate(90f, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
            try {
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
            } catch (ex: OutOfMemoryError) {
                Ln.e("OutOfMemoryError", "转存图片OOM")
            }
        }
        val absolutePath = BitmapIOService.createBitmapSavePath()
        tvTip.text = "转存图片中，请耐心等待"
        imageImporting = true
        selectImage.isEnabled = false
        BitmapIOService.saveBitmap(absolutePath, bitmap, object : Callback<String> {
            override fun onSuccess(data: String) {
                ToastUtil.toast("图片转存成功")
                imageImporting = false
                imagePath = data
                bgView.setImageByUri(event.uri)
                tvTip.text = "从相册选取图片"
                selectImage.isEnabled = true
            }

            override fun onFail(msg: String) {
                ToastUtil.toast("图片转存失败，请更换图片后重新尝试")
                imageImporting = false
                tvTip.text = "从相册选取图片"
                selectImage.isEnabled = true
            }
        })
    }

    // 导入多张图片
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGallerySelectComplete(event: GalleryCompleteEvent) {
        val dialog = ProgressDialog(context, "图片导入中..").apply { show() }
        val max = event.list.size
        var progress = 0f
        event.list.forEach { uri ->
            // 图片信息
            var rotate = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.contentResolver.openInputStream(uri)?.let {
                    val imageRotation = ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                    if (imageRotation == ExifInterface.ORIENTATION_ROTATE_90 || imageRotation == ExifInterface.ORIENTATION_ROTATE_270) {
                        rotate = true
                    }
                }
            }
            // 转存图片到本地
            var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            // iOS 拍出的图片带旋转角，要在导入时转为旋转后的图片
            if (rotate) {
                val m = Matrix()
                m.setRotate(90f, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
                try {
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                } catch (ex: OutOfMemoryError) {
                    Ln.e("OutOfMemoryError", "转存图片OOM")
                }
            }
            val absolutePath = BitmapIOService.createBitmapSavePath()
            BitmapIOService.saveBitmap(absolutePath, bitmap, object : Callback<String> {
                override fun onSuccess(data: String) {
                    galleryPath.add(data)
                    tempGalleryPath.add(data)
                    progress++
                    dialog.setProgress((progress / max * 100).toInt())
                    if (progress.toInt() == max) {
                        dialog.dismiss()
                        ToastUtil.toast("图片导入完成")
                        rvGalleryList.adapter?.notifyDataSetChanged()
                    }
                }

                override fun onFail(msg: String) {
                    // 导入失败，删除存图的文件
                    BitmapIOService.deleteBitmap(absolutePath)
                    progress++
                    dialog.setProgress((progress / max * 100).toInt())
                    if (progress.toInt() == max) {
                        dialog.dismiss()
                        ToastUtil.toast("图片导入完成")
                        rvGalleryList.adapter?.notifyDataSetChanged()
                    }
                }
            })
        }
    }
}
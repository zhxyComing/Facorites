package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.bean.FileBox
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.CommonCallback
import com.app.dixon.facorites.core.common.ProgressCallback
import com.app.dixon.facorites.core.data.bean.*
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.FileIOService
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.enum.EntryType
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.ImageSelectHelper
import com.app.dixon.facorites.core.util.VideoSelectHelper
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.page.category.event.CategoryImageCompleteEvent
import com.app.dixon.facorites.page.gallery.adapter.GalleryImportAdapter
import com.app.dixon.facorites.page.gallery.event.GalleryCompleteEvent
import com.app.dixon.facorites.page.video.event.VideoSelectCompleteEvent
import com.dixon.dlibrary.util.Ln
import com.dixon.dlibrary.util.ScreenUtil
import com.dixon.dlibrary.util.ToastUtil
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.jarvanmo.exoplayerview.media.SimpleMediaSource
import kotlinx.android.synthetic.main.app_dialog_create_entry_content.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*


const val ENTRY_IMAGE_REQUEST = 101
const val ENTRY_GALLERY_REQUEST = 102
const val ENTRY_VIDEO_REQUEST = 103

/**
 * TODO 重构 拆解 优化
 */

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

    // 视频
    private var tempVideoPath: String? = null // 新导入的临时视频 如果后续选择取消 则要删除掉这些新导入的视频

    // 文件
    private var tempFilePath: String? = null // 新导入的临时文件 如果后续选择取消 则要删除掉这些新导入的文件

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
        initVideoLayout()
        initFileLayout()
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
                EntryType.VIDEO -> saveOrUpdateVideo()
                EntryType.FILE -> saveOrUpdateFile()
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
        // TODO 抽离组件
        ivGoLinkLayout.setOnClickListener {
            dataType = EntryType.LINK
            showLinkUi()
            // TODO 图标高亮 & 替换图标
        }
        ivGoWordLayout.setOnClickListener {
            dataType = EntryType.WORD
            showWordUi()
        }
        ivGoImageLayout.setOnClickListener {
            dataType = EntryType.IMAGE
            showImageUi()
        }
        ivGoGalleryLayout.setOnClickListener {
            dataType = EntryType.GALLERY
            showGalleryUi()
        }
        ivGoVideoLayout.setOnClickListener {
            dataType = EntryType.VIDEO
            showVideoUi()
        }
        ivGoFileLayout.setOnClickListener {
            dataType = EntryType.FILE
            showFileUi()
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
        }, { videoEntryBean ->
            dataType = EntryType.VIDEO
            tempVideoPath = videoEntryBean.path
            etVideoTitle.setText(videoEntryBean.title)
            showVideoUi()
            videoView.show()
            tvVideoImportTip.hide()
            // 不允许修改视频：视频都改了，为什么不去创建新的？
            ivVideoRemove.hide()
            playVideo(videoEntryBean.path)
        }, { fileEntryBean ->
            dataType = EntryType.FILE
            tempFilePath = fileEntryBean.path
            showFileUi()
            showFile(File(fileEntryBean.path), fileEntryBean.title)
            // 不允许修改文件
            ivFileRemove.hide()
        })
        tapeEntry?.let { layoutChange.hide() }

        // 部分粗体
        etWordContent.mediumFont()
        tvLinkLayoutTitle.mediumFont()
        tvImageLayoutTitle.mediumFont()
        tvWordLayoutTitle.mediumFont()
        tvGalleryLayoutTitle.mediumFont()
        tvVideoLayoutTitle.mediumFont()
        tvFileLayoutTitle.mediumFont()
        tvFileTitle.mediumFont()

        changeFunctionSelectStatus()
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
            if (!linkFromShare.contains("http")) {
                // 切换至语录面板
                dataType = EntryType.WORD
                showWordUi()
                // TODO 图标高亮
            }
        }
    }

    private fun changeFunctionSelectStatus() {
        val items = listOf(ivGoLinkLayout, ivGoWordLayout, ivGoImageLayout, ivGoGalleryLayout, ivGoVideoLayout, ivGoFileLayout)
        fun resetSelectStatus() {
            items.forEach { it.setBackgroundColor(Color.TRANSPARENT) }
        }
        when (dataType) {
            EntryType.LINK -> {
                resetSelectStatus()
                ivGoLinkLayout.setBackgroundResource(R.drawable.app_shape_create_entry_function_item_bg)
            }
            EntryType.WORD -> {
                resetSelectStatus()
                ivGoWordLayout.setBackgroundResource(R.drawable.app_shape_create_entry_function_item_bg)
            }
            EntryType.IMAGE -> {
                resetSelectStatus()
                ivGoImageLayout.setBackgroundResource(R.drawable.app_shape_create_entry_function_item_bg)
            }
            EntryType.GALLERY -> {
                resetSelectStatus()
                ivGoGalleryLayout.setBackgroundResource(R.drawable.app_shape_create_entry_function_item_bg)
            }
            EntryType.VIDEO -> {
                resetSelectStatus()
                ivGoVideoLayout.setBackgroundResource(R.drawable.app_shape_create_entry_function_item_bg)
            }
            EntryType.FILE -> {
                resetSelectStatus()
                ivGoFileLayout.setBackgroundResource(R.drawable.app_shape_create_entry_function_item_bg)
            }
        }
    }

    private fun showLinkUi() {
        linkLayout.show()
        imageLayout.hide()
        wordLayout.hide()
        galleryLayout.hide()
        videoLayout.hide()
        fileLayout.hide()
        changeFunctionSelectStatus()
    }

    private fun showImageUi() {
        linkLayout.hide()
        imageLayout.show()
        wordLayout.hide()
        galleryLayout.hide()
        videoLayout.hide()
        fileLayout.hide()
        changeFunctionSelectStatus()
    }

    private fun showWordUi() {
        linkLayout.hide()
        imageLayout.hide()
        wordLayout.show()
        galleryLayout.hide()
        videoLayout.hide()
        fileLayout.hide()
        changeFunctionSelectStatus()
    }

    private fun showGalleryUi() {
        linkLayout.hide()
        imageLayout.hide()
        wordLayout.hide()
        galleryLayout.show()
        videoLayout.hide()
        fileLayout.hide()
        changeFunctionSelectStatus()
    }

    private fun showVideoUi() {
        videoLayout.show()
        linkLayout.hide()
        imageLayout.hide()
        wordLayout.hide()
        galleryLayout.hide()
        fileLayout.hide()
        changeFunctionSelectStatus()
    }

    private fun showFileUi() {
        videoLayout.hide()
        linkLayout.hide()
        imageLayout.hide()
        wordLayout.hide()
        galleryLayout.hide()
        fileLayout.show()
        changeFunctionSelectStatus()
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

    // 保存视频
    private fun saveOrUpdateVideo() {
        val title = etVideoTitle.text.toString()
        val categoryId = categoryChoose.getSelectionData<CategoryInfoBean>()?.id
        if (title.isNotEmpty() && categoryId != null && !tempVideoPath.isNullOrEmpty()) {
            if (editType == EDIT_TYPE_CREATE) {
                DataService.createEntry(
                    VideoEntryBean(
                        path = tempVideoPath!!,
                        title = title,
                        date = Date().time,
                        belongTo = categoryId,
                    ),
                    callback
                )
            } else if (editType == EDIT_TYPE_UPDATE) {
                (tapeEntry as? VideoEntryBean)?.let {
                    DataService.updateEntry(
                        it,
                        VideoEntryBean(
                            path = tempVideoPath!!,
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
            etVideoTitle.shakeTipIfEmpty()
            // 没选图或者导入过程中均不允许创建或更新
            if (tempVideoPath.isNullOrEmpty()) {
                tvVideoSubLayout.shakeTip()
            }
        }
    }

    // 保存文件
    private fun saveOrUpdateFile() {
        val title = etFileTitle.text.toString()
        val categoryId = categoryChoose.getSelectionData<CategoryInfoBean>()?.id
        if (title.isNotEmpty() && categoryId != null && !tempFilePath.isNullOrEmpty()) {
            if (editType == EDIT_TYPE_CREATE) {
                DataService.createEntry(
                    FileEntryBean(
                        path = tempFilePath!!,
                        title = title,
                        date = Date().time,
                        belongTo = categoryId,
                    ),
                    callback
                )
            } else if (editType == EDIT_TYPE_UPDATE) {
                (tapeEntry as? FileEntryBean)?.let {
                    DataService.updateEntry(
                        it,
                        FileEntryBean(
                            path = tempFilePath!!,
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
            etFileTitle.shakeTipIfEmpty()
            // 没选图或者导入过程中均不允许创建或更新
            if (tempFilePath.isNullOrEmpty()) {
                tvFileSubLayout.shakeTip()
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
                    FileIOService.deleteFile(removePath)
                    tempGalleryPath.remove(removePath)
                }
            })
    }

    // 视频类型
    private fun initVideoLayout() {
        tvVideoSubLayout.setOnClickListener {
            VideoSelectHelper.openVideoSelectPage(ENTRY_VIDEO_REQUEST)
        }
        ivVideoRemove.setOnClickListener {
            tvVideoImportTip.show()
            ivVideoRemove.hide()
            releaseVideo()
            videoView.hide()
        }
        tvVideoLayoutTip.setOnClickListener {
            TipDialog(
                context,
                content = "导入收藏夹子的视频有以下特性：\n\n1.应用外（如相册）不可见；\n" +
                        "\n2.删除手机（相册）的原视频不会影响到收藏视频。",
                title = "视频收藏提示"
            ).show()
        }
    }

    // 文件类型
    private fun initFileLayout() {
        tvFileSubLayout.setOnClickListener {
            importFile()
        }
        ivFileRemove.setOnClickListener {
            tvFileImportTip.show()
            ivFileRemove.hide()
            flFileShowView.hide()
            deleteExpiredFile()
        }
        tvFileLayoutTip.setOnClickListener {
            TipDialog(
                context,
                content = "导入收藏夹子的文件有以下特性：\n\n1.应用外不可见；\n" +
                        "\n2.删除手机的原文件不会影响到收藏文件。",
                title = "文件收藏提示"
            ).show()
        }
        flFileShowView.setOnClickListener { }
    }

    // 导入文件
    private fun importFile() {
        // 申请权限
        // TODO 暂时关闭apk的导入，有俩种可能的原因导致导入的应用无法安装：1.没有执行权限，对比正常apk -rw-rw，它是 -rw 2.应用 md5 校验通不过. 后续修吧，这个影响不大.
        requestStoragePermission {
            FileExploreDialog(context) { file, documentFile ->
                FileBox(file, documentFile).process({
                    if (it.path.endsWith(".apk")) {
                        ToastUtil.toast("导入应用程序功能将在后续版本支持，敬请谅解..")
                        return@process
                    }
                    val dialog = ProgressDialog(context, "文件导入中..").apply { show() }
                    Ln.i("ImportFile", "file: ${it.name}")
                    FileIOService.saveFile(FileIOService.FileType.FILE, it.toUri(), object : ProgressCallback<String> {
                        override fun onSuccess(data: String) {
                            dialog.dismiss()
                            ToastUtil.toast("文件导入完成")
                            tempFilePath = data
                            showFile(File(data), it.name)
                        }

                        override fun onFail(msg: String) {
                            dialog.dismiss()
                            ToastUtil.toast("文件导入失败")
                        }

                        override fun onProgress(progress: Int) {
                            dialog.setProgress(progress)
                        }
                    })
                }, {
                    if (it.name?.endsWith(".apk") == true) {
                        ToastUtil.toast("导入apk功能将在后续版本支持，敬请谅解.")
                        return@process
                    }
                    val dialog = ProgressDialog(context, "文件导入中..").apply { show() }
                    Ln.i("ImportFile", "file: ${it.name}")
                    FileIOService.saveFile(FileIOService.FileType.FILE, it.uri, object : ProgressCallback<String> {
                        override fun onSuccess(data: String) {
                            dialog.dismiss()
                            ToastUtil.toast("文件导入完成")
                            tempFilePath = data
                            showFile(File(data), it.name ?: data)
                        }

                        override fun onFail(msg: String) {
                            dialog.dismiss()
                            ToastUtil.toast("文件导入失败")
                        }

                        override fun onProgress(progress: Int) {
                            dialog.setProgress(progress)
                        }
                    })
                })
            }.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showFile(file: File, appointName: String) {
        ivFileRemove.show()
        flFileShowView.show()
        tvFileImportTip.hide()
        val simpleName = if (appointName.lastIndexOf(".") != -1) {
            appointName.substring(0, appointName.lastIndexOf("."))
        } else {
            appointName
        }
        tvFileTitle.text = simpleName
        etFileTitle.setText(simpleName)
        tvFileSize.text = "文件大小：${byteToString(file.length())}"
        tvFileType.text = "文件类型：${
            if (file.path.lastIndexOf(".") != -1) {
                file.path.substring(file.path.lastIndexOf(".") + 1)
            } else {
                "未知"
            }
        }"
        tvFileType.setOnLongClickListener {
            ToastUtil.toast(file.name)
            true
        }
    }

    // 请求SD卡读写权限
    private fun requestStoragePermission(block: () -> Unit) {
        XXPermissions.with(ContextAssistant.activity())
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {
                // 获得权限
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    if (all) {
                        block.invoke()
                    }
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    if (never) {
                        ToastUtil.toast("收藏夹子被拒绝授权，请手动授予读写权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(ContextAssistant.activity(), permissions)
                    } else {
                        ToastUtil.toast("获取读写权限失败，无法浏览文件")
                    }
                }
            })
    }

    private fun changeType() {
        dataType = when (dataType) {
            EntryType.LINK -> EntryType.IMAGE
            EntryType.IMAGE -> EntryType.WORD
            EntryType.WORD -> EntryType.GALLERY
            EntryType.GALLERY -> EntryType.VIDEO
            EntryType.VIDEO -> EntryType.LINK
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
        val expendInfoList = obtainExpendInfoListByCategoryData()
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

    private fun obtainExpendInfoListByCategoryData(): List<CustomSpinner.ExpandInfo<CategoryInfoBean>> {
        val unorderedCategoryList = DataService.getCategoryList().toMutableList()
        val resList = mutableListOf<CustomSpinner.ExpandInfo<CategoryInfoBean>>()

        fun appendChild(target: CategoryInfoBean, pool: MutableList<CategoryInfoBean>, resList: MutableList<CustomSpinner.ExpandInfo<CategoryInfoBean>>, level: Int) {
            pool.forEach {
                if (it.belongTo == target.id) {
                    resList.add(CustomSpinner.ExpandInfo("◇ ".repeat(level) + it.name, it.bgPath, it))
                    appendChild(it, pool, resList, level + 1)
                }
            }
        }

        val rootCategoryList = unorderedCategoryList.filter { it.belongTo == null }
        val pool = unorderedCategoryList.filter { it.belongTo != null }.toMutableList()
        rootCategoryList.forEach {
            resList.add(CustomSpinner.ExpandInfo(it.name, it.bgPath, it))
            appendChild(it, pool, resList, 1)
        }
        return resList
    }

    override fun onDetachedFromWindow() {
        Ln.i("CreateEntryDialog", "onDetachedFromWindow")
        // 如果视频在播放 则停止播放并释放资源
        videoView.stop()
        videoView.releasePlayer()
        if (!hasSave) {
            deleteExpiredImportImage()
            deleteExpiredGalleryImage()
            deleteExpiredVideo()
            deleteExpiredFile()
        } else {
            // 保存了，但是保存的不是对应类型
            if (dataType != EntryType.IMAGE) {
                deleteExpiredImportImage()
            }
            if (dataType != EntryType.GALLERY) {
                deleteExpiredGalleryImage()
            }
            if (dataType != EntryType.VIDEO) {
                deleteExpiredVideo()
            }
            if (dataType != EntryType.FILE) {
                deleteExpiredFile()
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
                FileIOService.deleteFile(it)
            }
        }
    }

    // 删除过期的导入图片集
    private fun deleteExpiredGalleryImage() {
        tempGalleryPath.forEach {
            FileIOService.deleteFile(it)
        }
    }

    // 删除过期的导入视频
    private fun deleteExpiredVideo() {
        tempVideoPath?.let {
            var deleteExpiredImage = true
            (tapeEntry as? VideoEntryBean)?.let { tapeVideoBean ->
                // 带入更新的视频在确认修改之前不删除
                if (tapeVideoBean.path == it) {
                    deleteExpiredImage = false
                }
            }
            if (deleteExpiredImage) {
                FileIOService.deleteFile(it)
                tempVideoPath = null
            }
        }
    }

    // 删除过期的导入文件
    private fun deleteExpiredFile() {
        tempFilePath?.let {
            var deleteExpiredFile = true
            (tapeEntry as? FileEntryBean)?.let { tapeFileBean ->
                // 带入更新的文件在确认修改之前不删除
                if (tapeFileBean.path == it) {
                    deleteExpiredFile = false
                }
            }
            if (deleteExpiredFile) {
                FileIOService.deleteFile(it)
                tempFilePath = null
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onImageSelectComplete(event: CategoryImageCompleteEvent) {
        // 转存之前先把之前导入的图片删掉
        deleteExpiredImportImage()
        tvTip.text = "转存图片中，请耐心等待"
        imageImporting = true
        selectImage.isEnabled = false
        FileIOService.saveFile(FileIOService.FileType.IMAGE, event.uri, object : ProgressCallback<String> {
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

            override fun onProgress(progress: Int) {

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
            FileIOService.saveFile(FileIOService.FileType.IMAGE, uri, object : ProgressCallback<String> {
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
                    progress++
                    dialog.setProgress((progress / max * 100).toInt())
                    if (progress.toInt() == max) {
                        dialog.dismiss()
                        ToastUtil.toast("图片导入完成")
                        rvGalleryList.adapter?.notifyDataSetChanged()
                    }
                }

                override fun onProgress(progress: Int) {

                }
            })
        }
    }

    // 导入视频
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVideoSelectComplete(event: VideoSelectCompleteEvent) {
        val dialog = ProgressDialog(context, "视频导入中..").apply { show() }
        FileIOService.saveFile(FileIOService.FileType.VIDEO, event.uri, object : ProgressCallback<String> {
            override fun onSuccess(data: String) {
                dialog.dismiss()
                ToastUtil.toast("导入视频成功")
                tempVideoPath = data

                // 打开播放器进行播放
                videoView.show()
                tvVideoImportTip.hide()
                ivVideoRemove.show()
                playVideo(data)
            }

            override fun onFail(msg: String) {
                dialog.dismiss()
                ToastUtil.toast("导入视频失败 $msg")
            }

            override fun onProgress(progress: Int) {
                dialog.setProgress(progress)
            }
        })
    }

    private fun playVideo(path: String) {
        val mediaSource = SimpleMediaSource(Uri.parse(path)) //uri also supported
        videoView.play(mediaSource, 0) //play from a particular position
        videoView.player.repeatMode = REPEAT_MODE_ALL
        videoView.changeWidgetVisibility(R.id.exo_player_enter_fullscreen, View.GONE)
    }

    // 停止播放并移除视频
    private fun releaseVideo() {
        videoView.stop()
        videoView.releasePlayer()
        deleteExpiredVideo()
    }
}
package com.app.dixon.facorites.page.image

import ShareUtil
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.IMAGE_PATH
import com.app.dixon.facorites.core.data.service.BitmapIOService
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.ex.hide
import com.app.dixon.facorites.core.ex.show
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.core.view.CreateEntryDialog
import com.app.dixon.facorites.core.view.CreateImageEntryDialog
import com.app.dixon.facorites.core.view.OptionDialog
import com.app.dixon.facorites.core.view.TipDialog
import com.dixon.dlibrary.util.ToastUtil
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.android.synthetic.main.activity_image.*
import java.io.File

class ImageActivity : BaseActivity() {

    private lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        normalFont()

        intent.getStringExtra(IMAGE_PATH)?.let {
            path = it
        } ?: let {
            finish()
            return
        }

        initView()
    }

    private fun initView() {
        // 从网络读取图片
        if (path.startsWith("http")) {
            tvLoading.show()
            tvShare.hide()
            BitmapIOService.readBitmapFromUrl(path, object : Callback<Bitmap> {
                override fun onSuccess(data: Bitmap) {
                    tvLoading.hide()
                    photoView.setImageBitmap(data)
                    // 初始化功能条
                    initUrlBanner(data)
                }

                override fun onFail(msg: String) {
                    tvLoading.hide()
                    ToastUtil.toast("图片加载失败")
                    finish()
                }
            })
            return
        }
        // 从本地读取图片
        photoView.setImageURI(Uri.parse(path))
        tvShare.setOnClickListener {
            // 思路
            // 1.弹窗提醒；
            // 2.申请权限；
            // 3.转存文件；
            // 4.执行分享。

            // 如果已经有缓存图了，则直接使用
            val fileName = path.substring(path.lastIndexOf("/") + 1, path.length)
            if (FileUtils.isExTempDirContainsFile(fileName)) {
                ContextAssistant.activity()?.let { aty ->
                    ShareUtil.shareImage(aty, FileUtils.getExTempFileAbsolutePath(fileName))
                }
                return@setOnClickListener
            }
            // 每次分享图片前，如果缓存过多，则把旧的缓存图删掉
            FileUtils.clearExTempDirIfNecessary()
            if (!XXPermissions.isGranted(this, Permission.MANAGE_EXTERNAL_STORAGE)) {
                OptionDialog(
                    context = this,
                    title = "手动授权提醒",
                    desc = it.resources.getString(R.string.app_image_share_tip),
                    rightString = "授权",
                    leftString = "关闭",
                    rightClick = {
                        requestStoragePermission {
                            runShare()
                        }
                    }).show()
            } else {
                runShare()
            }
        }
    }

    private fun initUrlBanner(bitmap: Bitmap) {
        llUrlBanner.show()
        tvAdd.setOnClickListener {
            CreateImageEntryDialog(this, bitmap).show()
        }
        tvSave.setOnClickListener {
            BitmapIOService.saveBitmapToAlbum(this, bitmap)
        }
    }

    // TODO IO线程类
    private fun runShare() {
        val tipDialog = TipDialog(this, "转码中，请耐心等待...", "提醒", false)
        tipDialog.show()
        Thread {
            val fileName = path.substring(path.lastIndexOf("/"), path.length)
            val file = FileUtils.createExTempFile(fileName)
            val bitmap = FileUtils.readBitmap(path)
            if (file != null && bitmap != null) {
                FileUtils.saveBitmap(file, bitmap, object : Callback<String> {
                    override fun onSuccess(data: String) {
                        backUi {
                            tipDialog.dismiss()
                            ContextAssistant.activity()?.let { aty ->
                                ShareUtil.shareImage(aty, data)
                            }
                        }
                    }

                    override fun onFail(msg: String) {
                        backUi {
                            tipDialog.dismiss()
                            ToastUtil.toast("分享失败：转存文件错误")
                        }
                        File(file).delete()
                    }
                })
            }
        }.start()
    }

    override fun statusBarColor(): Int = R.color.black


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
                        ToastUtil.toast("收藏夹子被拒绝授权，请手动授予存储权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(ContextAssistant.activity(), permissions)
                    } else {
                        ToastUtil.toast("获取存储权限失败，无法分享图片")
                    }
                }
            })
    }
}
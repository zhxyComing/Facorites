package com.app.dixon.facorites.core.util

import android.content.Context
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.view.OptionDialog
import com.dixon.dlibrary.util.ToastUtil
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.io.File

/**
 * path 分享图片
 *
 * 思路
 * 1.弹窗提醒；
 * 2.申请权限；
 * 3.转存文件；
 * 4.执行分享。
 */
fun String.shareAsImage(context: Context) {
    // 如果已经有缓存图了，则直接使用
    val fileName = this.substring(this.lastIndexOf("/") + 1, this.length)
    if (FileUtils.isExTempDirContainsFile(fileName)) {
        ContextAssistant.activity()?.let { aty ->
            ShareUtil.shareImage(aty, FileUtils.getExTempFileAbsolutePath(fileName))
        }
        return
    }
    // 每次分享图片前，如果缓存过多，则把旧的缓存图删掉
    FileUtils.clearExTempDirIfNecessary()
    if (!XXPermissions.isGranted(context, Permission.MANAGE_EXTERNAL_STORAGE)) {
        OptionDialog(
            context = context,
            title = "手动授权提醒",
            desc = context.resources.getString(R.string.app_image_share_tip),
            rightString = "授权",
            leftString = "关闭",
            rightClick = {
                requestStoragePermission {
                    runShare(this)
                }
            }).show()
    } else {
        runShare(this)
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
                    ToastUtil.toast("收藏夹子被拒绝授权，请手动授予存储权限")
                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                    XXPermissions.startPermissionActivity(ContextAssistant.activity(), permissions)
                } else {
                    ToastUtil.toast("获取存储权限失败，无法分享图片")
                }
            }
        })
}

// 执行分享
private fun runShare(path: String) {
    ThreadExecutor.execute {
        val fileName = path.substring(path.lastIndexOf("/"), path.length)
        val file = FileUtils.createExTempFile(fileName)
        file?.let {
            FileUtils.saveFile(path, file, object : Callback<String> {
                override fun onSuccess(data: String) {
                    backUi {
                        ContextAssistant.activity()?.let { aty ->
                            ShareUtil.shareImage(aty, data)
                        }
                    }
                }

                override fun onFail(msg: String) {
                    backUi {
                        ToastUtil.toast("分享失败：资源错误")
                    }
                    File(file).delete()
                }
            })
        }
    }
}

fun String.clearShareTempFile() {
    val fileName = this.substring(this.lastIndexOf("/") + 1, this.length)
    if (FileUtils.isExTempDirContainsFile(fileName)) {
        val path = FileUtils.getExTempFileAbsolutePath(fileName)
        File(path).delete()
    }
}
package com.app.dixon.facorites.page.mine

import ShareUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.bean.FileBox
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.ie.IEService
import com.app.dixon.facorites.core.util.ClipUtil
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.core.view.FileExploreDialog
import com.app.dixon.facorites.core.view.OptionDialog
import com.app.dixon.facorites.core.view.ProgressDialog
import com.app.dixon.facorites.core.view.TipDialog
import com.dixon.dlibrary.util.ToastUtil
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.android.synthetic.main.app_fragment_mine_content.*
import java.io.File


/**
 * 全路径：com.app.dixon.facorites.page.home
 * 类描述：个人页面
 * 创建人：xuzheng
 * 创建时间：3/22/22 2:49 PM
 */

class MineFragment : VisibleExtensionFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.app_fragment_mine_content, container, false).apply {
        normalFont()
        findViewById<View>(R.id.tvPageTitle).mediumFont()
    }

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        ivBg.setActualImageResource(R.drawable.app_mine_bg_cover)

        initCommon()
        initIE()
    }

    // 导入导出
    private fun initIE() {
        // 导出书签的提示
        ivExportBookmarkTip.setOnClickListener {
            context?.let {
                TipDialog(it, it.getString(R.string.app_export_bookmark_tip)).show()
            }
        }

//        ivExportBackupTip.setOnClickListener {
//            context?.let {
//                TipDialog(it, it.getString(R.string.app_export_backup_tip)).show()
//            }
//        }

        appExportBookmark.setOnClickListener {
            // 申请权限
            requestStoragePermission {
                // 导出书签
                runExportBookmark()
            }
        }

        appImportBookmark.setOnClickListener {
            // 申请权限
            requestStoragePermission {
                context?.let {
                    FileExploreDialog(it) { file, documentFile ->
                        runImport(FileBox(file, documentFile))
                    }.show()
                }
            }
        }

//        appExportBackup.setOnClickListener {
//            ToastUtil.toast("施工中...")
//        }
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
                        ToastUtil.toast("获取存储权限失败，无法导出备份")
                    }
                }
            })
    }

    private fun runExportBookmark() {
        activity?.let { context ->
            val progressDialog = ProgressDialog(context, "书签导出")
            progressDialog.setOnShowListener {
                IEService.exportBookmark(onProgress = { progress ->
                    progressDialog.setProgress(progress)
                }, onFail = { msg ->
                    ToastUtil.toast("书签导出失败：$msg")
                    progressDialog.dismiss()
                }, onSuccess = { path ->
                    backUi(500) {
                        progressDialog.dismiss()
                        OptionDialog(context, title = "书签导出成功！",
                            desc = "导出目录：存储根目录/收藏夹子/${File(path).name}",
                            descClick = {
                                ClipUtil.copyToClip(context, path)
                                ToastUtil.toast("完整路径已复制～")
                            },
                            rightString = "发送",
                            leftString = "关闭",
                            rightClick = {
                                // Android 原生分享
                                ShareUtil.shareFile(context, path)
                            }).show()
                    }
                })
            }
            progressDialog.show()
        }
    }

    private fun runImport(fileBox: FileBox) {
        fileBox.process({ file ->
            if (file.name.endsWith(".html")) {
                runImportBookmark(file)
            } else {
                ToastUtil.toast("无法导入，请确保文件正确")
            }
        }, { documentFile ->
            if (documentFile.name?.endsWith(".html") == true) {
                runImportBookmark(documentFile)
            } else {
                ToastUtil.toast("无法导入，请确保文件正确")
            }
        })
    }

    // 导入书签
    private fun runImportBookmark(file: File) {
        activity?.let { context ->
            val progressDialog = ProgressDialog(context, "书签导入")
            progressDialog.setOnShowListener {
                IEService.importBookmark(file, onProgress = { progress ->
                    progressDialog.setProgress(progress)
                }, onFail = { msg ->
                    ToastUtil.toast("书签导入失败：$msg")
                    progressDialog.dismiss()
                }, onSuccess = {
                    backUi(500) {
                        progressDialog.dismiss()
                        OptionDialog(
                            context, title = "书签导入成功！",
                            desc = "请查看收藏夹～",
                            rightString = "确认",
                            leftString = "关闭"
                        ).show()
                    }
                })
            }
            progressDialog.show()
        }
    }

    private fun runImportBookmark(documentFile: DocumentFile) {
        activity?.let { context ->
            val progressDialog = ProgressDialog(context, "书签导入")
            progressDialog.setOnShowListener {
                IEService.importBookmark(documentFile, onProgress = { progress ->
                    progressDialog.setProgress(progress)
                }, onFail = { msg ->
                    ToastUtil.toast("书签导入失败：$msg")
                    progressDialog.dismiss()
                }, onSuccess = {
                    backUi(500) {
                        progressDialog.dismiss()
                        OptionDialog(
                            context, title = "书签导入成功！",
                            desc = "请查看收藏夹～",
                            rightString = "确认",
                            leftString = "关闭"
                        ).show()
                    }
                })
            }
            progressDialog.show()
        }
    }

    // 通用设置
    private fun initCommon() {
        appEdit.setOnClickListener {
            PageJumper.openEditPage(this)
        }

        appAbout.setOnClickListener {
            PageJumper.openAboutPage(this)
        }
    }
}
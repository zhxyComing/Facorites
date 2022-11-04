package com.app.dixon.facorites.core.util

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.umeng.commonsdk.stateless.UMSLEnvelopeBuild
import java.io.File
import java.util.*

object OpenFileHelper {

    fun openFile(filePath: String): Intent? {
        val file = File(filePath)
        if (!file.exists()) return null
        /* 依扩展名的类型决定MimeType */
        return when (file.name.substring(file.name.lastIndexOf(".") + 1, file.name.length).lowercase(Locale.getDefault())) {
            "m4a", "mp3", "mid", "xmf", "ogg", "wav" -> {
                getAudioFileIntent(filePath)
            }
            "3gp", "mp4" -> {
                getVideoFileIntent(filePath)
            }
            "jpg", "gif", "png", "jpeg", "bmp" -> {
                getImageFileIntent(filePath)
            }
            "apk" -> {
                getApkFileIntent(filePath)
            }
            "ppt" -> {
                getPPTFileIntent(filePath)
            }
            "xls" -> {
                getExcelFileIntent(filePath)
            }
            "doc" -> {
                getWordFileIntent(filePath)
            }
            "pdf" -> {
                getPdfFileIntent(filePath)
            }
            "chm" -> {
                getChmFileIntent(filePath)
            }
            "txt" -> {
                getTextFileIntent(filePath)
            }
            else -> {
                getAllIntent(filePath)
            }
        }
    }

    // Android获取一个用于打开APK文件的intent
    private fun getAllIntent(param: String): Intent {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        setDataAndType(intent, param, "*/*")
        return intent
    }

    // Android获取一个用于打开APK文件的intent
    private fun getApkFileIntent(path: String): Intent {
        // TODO 先保存到外部文件夹 再安装 否则内部文件没有权限无法安装
        // TODO https://www.likecs.com/show-250245.html 没用，但是阐明了安装失败的原因
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        setDataAndType(intent, path, "application/vnd.android.package-archive")
        return intent
    }

    // Android获取一个用于打开VIDEO文件的intent
    private fun getVideoFileIntent(param: String): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("oneshot", 0)
        intent.putExtra("configchange", 0)
        setDataAndType(intent, param, "video/*")
        return intent
    }

    // Android获取一个用于打开AUDIO文件的intent
    private fun getAudioFileIntent(param: String): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("oneshot", 0)
        intent.putExtra("configchange", 0)
        setDataAndType(intent, param, "audio/*")
        return intent
    }

    // Android获取一个用于打开图片文件的intent
    private fun getImageFileIntent(param: String): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setDataAndType(intent, param, "image/*")
        return intent
    }

    // Android获取一个用于打开PPT文件的intent
    private fun getPPTFileIntent(param: String): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setDataAndType(intent, param, "application/vnd.ms-powerpoint")
        return intent
    }

    // Android获取一个用于打开Excel文件的intent
    private fun getExcelFileIntent(param: String): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setDataAndType(intent, param, "application/vnd.ms-excel")
        return intent
    }

    // Android获取一个用于打开Word文件的intent
    private fun getWordFileIntent(param: String): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setDataAndType(intent, param, "application/msword")
        return intent
    }

    // Android获取一个用于打开CHM文件的intent
    private fun getChmFileIntent(param: String): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setDataAndType(intent, param, "application/x-chm")
        return intent
    }

    // Android获取一个用于打开文本文件的intent
    private fun getTextFileIntent(param: String): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setDataAndType(intent, param, "text/plain")
        return intent
    }

    // Android获取一个用于打开PDF文件的intent
    private fun getPdfFileIntent(param: String): Intent {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setDataAndType(intent, param, "application/pdf")
        return intent
    }

    private fun setDataAndType(intent: Intent, param: String, type: String) {
        val file = File(param)
        val uri = Uri.fromFile(file)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val contentUri: Uri = FileProvider.getUriForFile(UMSLEnvelopeBuild.mContext, "com.app.dixon.facorites.provider", file)
            // 表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(contentUri, type)
        } else {
            intent.setDataAndType(uri, type)
        }
    }
}
package com.app.dixon.facorites.core.util

import com.app.dixon.facorites.base.ContextAssistant
import java.io.File
import java.util.*

// 通过外部程序打开文件的工具类
// 懒得写了，参考自https://blog.51cto.com/u_14397532/4930925
object OpenFileUtil {

    fun openFile(filePath: String) {
//        val intent = Intent()
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        intent.action = Intent.ACTION_VIEW
//        /* 调用getMIMEType()来取得MimeType */
//        val file = File(filePath)
//        val type: String = getMIMEType(file)
//        Ln.i("OpenFileUtil", "type $type")
//        /* 设定intent的file与MimeType */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            val contentUri: Uri = FileProvider.getUriForFile(mContext, "com.app.dixon.facorites.provider", file)
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            intent.setDataAndType(contentUri, type)
//        } else {
//            intent.setDataAndType(Uri.fromFile(file), type)
//        }
        val intent = OpenFileHelper.openFile(filePath)
        ContextAssistant.activity()?.startActivity(intent)
    }

    /* 判断文件MimeType的method */
    private fun getMIMEType(f: File): String {
        var type = ""
        val fName: String = f.name
        /* 按扩展名的类型决定MimeType */
        when (fName.substring(fName.lastIndexOf(".") + 1, fName.length).lowercase(Locale.getDefault())) {
            "m4a", "mp3", "mid", "xmf", "ogg", "wav" -> {
                type = "audio"
            }
            "3gp", "mp4" -> {
                type = "video"
            }
            "jpg", "gif", "png", "jpeg", "bmp" -> {
                type = "image"
            }
            "apk" -> {
                /* android.permission.INSTALL_PACKAGES */
                //用下载并自动安装apk包
                type = "application/vnd.android.package-archive"
                return type
            }
            else -> {
                /* 如果无法直接打开，就弹出软件列表给用户选择 */
                type = "*"
            }
        }
        type += "/*"
        return type
    }
}

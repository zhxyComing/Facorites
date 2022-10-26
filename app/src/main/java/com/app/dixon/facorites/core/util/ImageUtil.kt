package com.app.dixon.facorites.core.util

import android.graphics.BitmapFactory
import com.app.dixon.facorites.core.ex.byteToString
import java.io.File

object ImageUtil {

    fun obtainImageInfo(path: String): String {
        val fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."))
        val fileTime = TimeUtils.friendlyTime(fileName.toLong())
        val fileSize = byteToString(File(path).length())
        val fileMeasure = obtainImageSize(path)
        return "导入时间：$fileTime\n图片大小：$fileSize\n图片分辨率：${fileMeasure.first} X ${fileMeasure.second}"
    }

    fun obtainImageSize(path: String): Pair<Int, Int> {
        val options = BitmapFactory.Options()
        options.inSampleSize = 1
        options.inJustDecodeBounds = false
        BitmapFactory.decodeFile(path, options)
        return Pair(options.outWidth, options.outHeight)
    }
}
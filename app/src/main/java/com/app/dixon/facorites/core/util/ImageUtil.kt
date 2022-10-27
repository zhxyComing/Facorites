package com.app.dixon.facorites.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.data.service.FileIOService
import com.app.dixon.facorites.core.ex.byteToString
import java.io.File
import kotlin.math.sqrt

object ImageUtil {

    fun obtainImageInfo(path: String): String {
        val fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."))
        val fileTime = TimeUtils.friendlyTime(fileName.toLong())
        val fileSize = byteToString(File(path).length())
        val fileMeasure = obtainImageSize(path)
        return "导入时间：$fileTime\n图片大小：$fileSize\n图片分辨率：${fileMeasure.first} X ${fileMeasure.second}"
    }

    fun obtainImageSize(path: String): Pair<Int, Int> {
        val options = Options()
        options.inSampleSize = 1
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        return Pair(options.outWidth, options.outHeight)
    }

    fun obtainSafeBitmap(path: String, callback: (bitmap: Bitmap) -> Unit) {
        Ln.i("obtainSafeBitmap", "路径：$path")
        val size = obtainImageSize(path)
        if (!checkBitmapLimit(size.first, size.second)) {
            Ln.i("obtainSafeBitmap", "图片不压缩")
            FileIOService.readBitmap(path, callback = object : Callback<Bitmap> {
                override fun onSuccess(data: Bitmap) {
                    callback.invoke(data)
                }

                override fun onFail(msg: String) {
                    Ln.i("obtainSafeBitmap", "Fail $msg")
                }
            })
        } else {
            val resize = resetBitmapSize(size.first, size.second)
            val options = Options()
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            options.inSampleSize = calculateInSampleSize(size.first, size.second, resize.first, resize.second)
            Ln.i("obtainSafeBitmap", "图片压缩：$size $resize ${options.inSampleSize}")
            FileIOService.readBitmap(path, options, callback = object : Callback<Bitmap> {
                override fun onSuccess(data: Bitmap) {
                    Ln.i("obtainSafeBitmap", "图片压缩结果：${data.width} ${data.height}")
                    callback.invoke(data)
                }

                override fun onFail(msg: String) {
                    Ln.i("obtainSafeBitmap", "Fail $msg")
                }
            })
        }
    }

    private fun checkBitmapLimit(bitmapWidth: Int, bitmapHeight: Int) = bitmapWidth.toLong() * bitmapHeight.toLong() * 4L > 50000000L // 50M

    private fun resetBitmapSize(bitmapWidth: Int, bitmapHeight: Int): Pair<Int, Int> {
        val ratio: Double = bitmapWidth.toDouble() / bitmapHeight.toDouble()
        val resetHeight = sqrt(50000000L / 4 / ratio).toInt()
        val resetWidth = (ratio * resetHeight).toInt()
        return resetWidth to resetHeight
    }

    private fun calculateInSampleSize(currentWidth: Int, currentHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (currentWidth > reqWidth || currentHeight > reqHeight) {
            var tarWidth = currentWidth
            var tarHeight = currentHeight
            //计算缩放比，是2的指数
            while (tarWidth >= reqWidth && tarHeight >= reqHeight) {
                inSampleSize *= 2
                tarWidth = currentWidth / inSampleSize
                tarHeight = currentHeight / inSampleSize
            }
        }
        return inSampleSize
    }
}
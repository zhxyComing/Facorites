package com.app.dixon.facorites.core.data.service.base

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.app.dixon.facorites.base.BaseApplication
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.util.Ln
import java.io.*

/**
 * 全路径：com.app.dixon.facorites.core.data.service
 * 类描述：用于在指定根目录下读写文件(data/data/xxx/file)
 * 创建人：xuzheng
 * 创建时间：3/18/22 11:53 AM
 *
 * 字符串使用相对路径 方便规范数据存储
 * 图片使用绝对路径 方便外部通过Uri去调用
 */
object FileUtils {

    private val ROOT_CATEGORY = BaseApplication.application.filesDir

    /**
     * 在指定目录下创建文件夹
     */
    fun createDir(path: String): Boolean {
        val file = File(ROOT_CATEGORY, path)
        if (!file.exists()) {
            val result: Boolean // 文件是否创建成功
            try {
                result = file.mkdirs()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            if (!result) {
                return false
            }
        }
        return true
    }

    /**
     * 在绝对目录下创建文件夹
     */
    fun createDirAbs(absolutePath: String): Boolean {
        val file = File(absolutePath)
        if (!file.exists()) {
            val result: Boolean // 文件是否创建成功
            try {
                result = file.mkdirs()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            if (!result) {
                return false
            }
        }
        return true
    }

    /**
     * 保存字符串
     */
    fun saveString(path: String, str: String): Boolean {
        val file = File(ROOT_CATEGORY, path)
        if (!file.exists()) {
            val result: Boolean // 文件是否创建成功
            try {
                result = file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            if (!result) {
                return false
            }
        }
        file.writeText(str)
        return true
    }

    /**
     * 保存字符串
     */
    fun saveStringAbs(absolutePath: String, str: String): Boolean {
        val file = File(absolutePath)
        if (!file.exists()) {
            val result: Boolean // 文件是否创建成功
            try {
                result = file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            if (!result) {
                return false
            }
        }
        file.writeText(str)
        return true
    }


    /**
     * 追加字符串
     */
    fun appendString(path: String, str: String): Boolean {
        val file = File(ROOT_CATEGORY, path)
        if (!file.exists()) {
            val result: Boolean // 文件是否创建成功
            try {
                result = file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            if (!result) {
                return false
            }
        }
        file.appendText(str)
        return true
    }

    /**
     * 追加字符串
     */
    fun appendStringAbs(absolutePath: String, str: String): Boolean {
        val file = File(absolutePath)
        if (!file.exists()) {
            val result: Boolean // 文件是否创建成功
            try {
                result = file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            if (!result) {
                return false
            }
        }
        file.appendText(str)
        return true
    }

    /**
     * 读取字符串
     */
    fun readString(path: String): String {
        val file = File(ROOT_CATEGORY, path)
        if (!file.exists()) {
            return ""
        }
        return file.readText()
    }

    /**
     * 读取字符串
     */
    fun readStringAbs(absolutePath: String): String {
        val file = File(absolutePath)
        if (!file.exists()) {
            return ""
        }
        return file.readText()
    }

    /**
     * 按行读取字符串
     */
    fun readStringByLine(path: String): List<String> {
        val file = File(ROOT_CATEGORY, path)
        if (!file.exists()) {
            return listOf()
        }
        val lineList = mutableListOf<String>()
        val inputStream: InputStream = file.inputStream()
        inputStream.bufferedReader().forEachLine { lineList.add(it) }
        return lineList
    }

    /**
     * 按行读取字符串
     */
    fun readStringByLineAbs(absolutePath: String): List<String> {
        val file = File(absolutePath)
        if (!file.exists()) {
            return listOf()
        }
        val lineList = mutableListOf<String>()
        val inputStream: InputStream = file.inputStream()
        inputStream.bufferedReader().forEachLine { lineList.add(it) }
        return lineList
    }

    /**
     * 判断文件是否存在
     */
    fun exists(path: String): Boolean {
        val file = File(ROOT_CATEGORY, path)
        return file.exists()
    }

    /**
     * 判断文件是否存在
     */
    fun existsAbs(absolutePath: String): Boolean {
        val file = File(absolutePath)
        return file.exists()
    }

    /**
     * 创建空文件
     */
    fun createNewFile(path: String): Boolean {
        val file = File(ROOT_CATEGORY, path)
        return file.createNewFile()
    }

    /**
     * 创建空文件
     */
    fun createNewFileAbs(absolutePath: String): Boolean {
        val file = File(absolutePath)
        return file.createNewFile()
    }

    /**
     * 删除文件
     */
    fun deleteFile(path: String): Boolean {
        val file = File(ROOT_CATEGORY, path)
        return file.delete()
    }

    /**
     * 删除文件
     */
    fun deleteFileAbs(absolutePath: String): Boolean {
        val file = File(absolutePath)
        return file.delete()
    }

    /**
     * 保存图片
     *
     * 图片的路径一律使用绝对值 方便外部通过Uri去调用
     */
    fun saveBitmap(absolutePath: String, bitmap: Bitmap, asyncCallback: Callback<String>) {
        val file = File(absolutePath)
        if (!file.exists()) {
            val success = file.createNewFile()
            if (!success) {
                return
            }
        }
        try {
            val fos = FileOutputStream(file.absolutePath)
            // 压缩
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            asyncCallback.onSuccess(file.absolutePath)
        } catch (e: IOException) {
            Ln.e("BitmapUtils", e.toString())
            asyncCallback.onFail(e.toString())
        }
    }

    /**
     * 读取图片
     */
    fun readBitmap(absolutePath: String): Bitmap? {
        val file = File(absolutePath)
        if (!file.exists()) {
            return null
        }
        // 最大读取10M图片
        val buf = ByteArray(1024 * 1024 * 10)
        val bitmap: Bitmap?
        try {
            val fis = FileInputStream(file.absolutePath)
            val len: Int = fis.read(buf, 0, buf.size)
            bitmap = BitmapFactory.decodeByteArray(buf, 0, len)
            if (bitmap == null) {
                return null
            }
            fis.close()
        } catch (e: Exception) {
            return null
        }
        return bitmap
    }

    /**
     * 创建一个空文件用于保存图片
     */
    fun createBitmapSavePath(path: String): String {
        val file = File(ROOT_CATEGORY, path)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file.absolutePath
    }

    /**
     * 创建一个空文件用于保存图片
     */
    fun createBitmapSavePathAbs(absolutePath: String): String {
        val file = File(absolutePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file.absolutePath
    }


    /**
     * 删除图片
     */
    fun deleteBitmap(absolutePath: String): Boolean {
        val file = File(absolutePath)
        if (!file.exists()) {
            return false
        }
        return file.delete()
    }

    /**
     * 获取文件夹下所有文件的内容
     */
    fun readDir(path: String): List<String> {
        val file = File(ROOT_CATEGORY, path)
        val files = file.listFiles() ?: return listOf()
        val result: MutableList<String> = ArrayList()
        for (i in files.indices) {
            val content = File(files[i].absolutePath).readText()
            result.add(content)
        }
        return result
    }

    /**
     * 获取文件夹下所有文件的内容
     */
    fun readDirAbs(absolutePath: String): List<String> {
        val file = File(absolutePath)
        val files = file.listFiles() ?: return listOf()
        val result: MutableList<String> = ArrayList()
        for (i in files.indices) {
            val content = File(files[i].absolutePath).readText()
            result.add(content)
        }
        return result
    }

    /**
     * 读取Assets下的文件内容
     */
    fun readAssets(assetName: String): String? {
        var result: String? = ""
        try {
            //获取输入流
            val mAssets: InputStream = ContextAssistant.application().assets.open(assetName)
            //获取文件的字节数
            val length = mAssets.available()
            //创建byte数组
            val buffer = ByteArray(length)
            //将文件中的数据写入到字节数组中
            mAssets.read(buffer)
            mAssets.close()
            result = String(buffer)
            return result
        } catch (e: IOException) {
            e.printStackTrace()
            return result
        }
    }
}
package com.app.dixon.facorites.core.data.service.base

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import com.app.dixon.facorites.base.BaseApplication
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.EXPORT_ROOT_CATEGORY
import com.app.dixon.facorites.core.common.ProgressCallback
import com.app.dixon.facorites.core.util.Ln
import java.io.*
import java.util.*

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
    fun deleteFileAbs(absolutePath: String): Boolean {
        val file = File(absolutePath)
        return file.delete()
    }

    fun saveFile(originAbsolutePath: String, saveAbsolutePath: String, asyncCallback: Callback<String>) {
        // File.getTotalSpace()
        // 创建String对象保存文件名路径
        try {
            // 创建指定路径的文件
            val file = File(saveAbsolutePath)
            if (!file.exists()) {
                file.createNewFile()
            }
            val inputStream: InputStream = FileInputStream(originAbsolutePath)
            val fos = FileOutputStream(saveAbsolutePath)
            val b = ByteArray(1024)
            while (inputStream.read(b) != -1) {
                fos.write(b) // 写入数据
            }
            inputStream.close()
            fos.close() // 保存数据
            asyncCallback.onSuccess(saveAbsolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            asyncCallback.onFail(e.toString())
        }
    }

    fun saveFile(originUri: Uri, saveAbsolutePath: String, asyncCallback: ProgressCallback<String>) {
        // File.getTotalSpace()
        // 创建String对象保存文件名路径
        try {
            // 创建指定路径的文件
            val file = File(saveAbsolutePath)
            if (!file.exists()) {
                file.createNewFile()
            }
            val inputStream: InputStream? = ContextAssistant.application().contentResolver.openInputStream(originUri)
            inputStream?.let {
                val uriLength: Long = it.available().toLong()
                Ln.i("SaveFile", "uriLength: $uriLength")
                var currentLength = 0L
                val fos = FileOutputStream(saveAbsolutePath)
                val b = ByteArray(1024)
                while (it.read(b) != -1) {
                    fos.write(b) // 写入数据
                    currentLength += 1024
                    val progress = (currentLength * 100L / uriLength).toInt() // 一定要用Long，否则超限
                    asyncCallback.onProgress(progress)
                    Ln.i("SaveFile", "progress: $progress currentLength: $currentLength uriLength: $uriLength")
                }
                it.close()
                fos.close() // 保存数据
                asyncCallback.onSuccess(saveAbsolutePath)
                Ln.i("SaveFile", "Success")
            } ?: let {
                asyncCallback.onFail("inputStream 获取失败")
                Ln.i("SaveFile", "Fail 获取失败")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            asyncCallback.onFail(e.toString())
            Ln.i("SaveFile", "Fail ${e.printStackTrace()}")
        }
    }

    /**
     * 删除图片
     */
    fun deleteFile(absolutePath: String): Boolean {
        val file = File(absolutePath)
        if (!file.exists()) {
            return false
        }
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
        // 最大读取30M图片
        val buf = ByteArray(1024 * 1024 * 30)
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
     * 创建一个空文件用于保存文件
     */
    fun createFileSavePath(path: String): String {
        val file = File(ROOT_CATEGORY, path)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file.absolutePath
    }

    /**
     * 创建一个空文件用于保存临时数据
     */
    fun createTempFile(): String? {
        val file = File(ROOT_CATEGORY, "temp/${Date().time}.temp")
        if (createDir("temp")) {
            if (!file.exists()) {
                file.createNewFile()
            }
            return file.absolutePath
        }
        return null
    }

    /**
     * 创建一个空文件用于保存临时数据 创建在外部SD卡上
     */
    fun createExTempFile(name: String): String? {
        val file = File("${getSDPath()}/$EXPORT_ROOT_CATEGORY", "cache/${name}")
        if (createDirAbs("${getSDPath()}/$EXPORT_ROOT_CATEGORY/cache")) {
            if (!file.exists()) {
                file.createNewFile()
            }
            return file.absolutePath
        }
        return null
    }

    fun clearExTempDirIfNecessary() {
        val file = File("${getSDPath()}/$EXPORT_ROOT_CATEGORY", "cache")
        val fileNumbers = file.listFiles()?.size ?: 0
        val fileSize = obtainDirSize(file).toDouble() / 1024 / 1024 // M
        // 缓存大于100M或者数量超过20个，就清空所有旧缓存（没必要学LRU，因为这些缓存大概率是不会再用到的，是分享时转存的数据）
        if (fileSize > 100 || fileNumbers > 20) {
            deleteDir(file)
        }
    }

    fun isExTempDirContainsFile(name: String) =
        containsFile(File("${getSDPath()}/$EXPORT_ROOT_CATEGORY", "cache"), name)

    fun getExTempFileAbsolutePath(name: String): String =
        "${getSDPath()}/$EXPORT_ROOT_CATEGORY/cache/$name"

    private fun obtainDirSize(file: File): Long {
        var size = 0L
        if (file.exists()) {
            if (file.isDirectory) {
                val fileList = file.listFiles()
                fileList?.forEach {
                    size += obtainDirSize(it)
                }
            } else {
                size = file.length()
            }
        }
        return size
    }

    private fun deleteDir(file: File) {
        if (file.exists()) {
            if (file.isDirectory) {
                val fileList = file.listFiles()
                fileList?.forEach {
                    deleteDir(it)
                }
            } else {
                file.delete()
            }
        }
    }

    private fun containsFile(dir: File, name: String): Boolean {
        if (dir.exists()) {
            if (dir.isDirectory) {
                val fileList = dir.listFiles()
                fileList?.forEach {
                    if (containsFile(it, name)) {
                        return true
                    }
                }
            } else {
                return dir.name == name
            }
        }
        return false
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

    /**
     * 获取SD卡路径
     */
    fun getSDPath(): String {
        var sdDir: File? = null
        val sdCardExist = (Environment.getExternalStorageState()
                == Environment.MEDIA_MOUNTED) //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory() //获取根目录
        }
        return sdDir.toString()
    }

    /**
     * 获取路径下的所有文件名
     */
    fun getFileNameArrayByPathAbs(absolutePath: String): Array<String>? {
        val file = File(absolutePath)
        if (file.exists()) {
            return file.list()
        }
        return null
    }

    /**
     * 获取路径下的所有文件
     */
    fun getFileArrayByPathAbs(absolutePath: String): Array<File>? {
        val file = File(absolutePath)
        if (file.exists()) {
            return file.listFiles()
        }
        return null
    }
}
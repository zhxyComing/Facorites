package com.app.dixon.facorites.core.data.service

import com.app.dixon.facorites.base.BaseApplication
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * 全路径：com.app.dixon.facorites.core.data.service
 * 类描述：用于在指定根目录下读写文件
 * 创建人：xuzheng
 * 创建时间：3/18/22 11:53 AM
 */
object FileUtils {

    /**
     * 在指定目录下创建文件夹
     */
    fun createDir(path: String): Boolean {
        val file = File(BaseApplication.application.filesDir, path)
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
        val file = File(BaseApplication.application.filesDir, path)
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
        val file = File(BaseApplication.application.filesDir, path)
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
        val file = File(BaseApplication.application.filesDir, path)
        if (!file.exists()) {
            return ""
        }
        return file.readText()
    }

    /**
     * 按行读取字符串
     */
    fun readStringByLine(path: String): List<String> {
        val file = File(BaseApplication.application.filesDir, path)
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
        val file = File(BaseApplication.application.filesDir, path)
        return file.exists()
    }

    /**
     * 创建空文件
     */
    fun createNewFile(path: String): Boolean {
        val file = File(BaseApplication.application.filesDir, path)
        return file.createNewFile()
    }

    /**
     * 删除文件
     */
    fun deleteFile(path: String): Boolean {
        val file = File(BaseApplication.application.filesDir, path)
        return file.delete()
    }
}
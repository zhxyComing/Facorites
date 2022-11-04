package com.app.dixon.facorites.core.data.bean

/**
 * 全路径：com.app.dixon.facorites.core.data.bean
 * 类描述：文件类型（app 无法直接打开，需要通过外部程序打开的文件）
 * 创建人：xuzheng
 * 创建时间：2022/11/3 11:09
 */
class FileEntryBean(val path: String, val title: String, date: Long, belongTo: Long, star: Boolean = false) : BaseEntryBean(date, belongTo, star) {

    override fun toString(): String {
        return "FileEntryBean(path='$path', title='$title') ${super.toString()}"
    }
}
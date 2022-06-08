package com.app.dixon.facorites.core.data.bean

/**
 * 全路径：com.app.dixon.facorites.core.data.bean
 * 类描述：图片类型
 * 创建人：xuzheng
 * 创建时间：2022/4/26 15:01
 */
class ImageEntryBean(val path: String, val title: String, date: Long, belongTo: Long, star: Boolean = false) : BaseEntryBean(date, belongTo, star) {

    override fun toString(): String {
        return "ImageEntryBean(path='$path', title='$title') ${super.toString()}"
    }
}
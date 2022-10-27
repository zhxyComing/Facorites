package com.app.dixon.facorites.core.data.bean

/**
 * 全路径：com.app.dixon.facorites.core.data.bean
 * 类描述：视频类型
 * 创建人：xuzheng
 * 创建时间：2022/10/26 15:32
 */
class VideoEntryBean(val path: String, val title: String, date: Long, belongTo: Long, star: Boolean = false) : BaseEntryBean(date, belongTo, star) {

    override fun toString(): String {
        return "VideoEntryBean(path=$path, title='$title') ${super.toString()}"
    }
}
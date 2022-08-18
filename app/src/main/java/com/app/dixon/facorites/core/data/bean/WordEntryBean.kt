package com.app.dixon.facorites.core.data.bean

/**
 * 一句话类型
 */
class WordEntryBean(val content: String, date: Long, belongTo: Long, star: Boolean = false) : BaseEntryBean(date, belongTo, star) {

    override fun toString(): String {
        return "WordEntryBean(content='$content') ${super.toString()}"
    }
}
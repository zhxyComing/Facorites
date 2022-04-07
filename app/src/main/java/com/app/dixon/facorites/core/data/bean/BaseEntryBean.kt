package com.app.dixon.facorites.core.data.bean

/**
 * 全路径：com.app.dixon.facorites.core.data.bean
 * 类描述：基础条目
 * 创建人：xuzheng
 * 创建时间：3/17/22 7:53 PM
 */
open class BaseEntryBean(val date: Long) {

    override fun toString(): String {
        return "BaseEntryBean(date=$date)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as BaseEntryBean
        if (date != other.date) return false
        return true
    }

    override fun hashCode(): Int {
        return date.hashCode()
    }
}
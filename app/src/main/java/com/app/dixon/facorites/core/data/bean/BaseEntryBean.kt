package com.app.dixon.facorites.core.data.bean

/**
 * 全路径：com.app.dixon.facorites.core.data.bean
 * 类描述：基础条目
 * 创建人：xuzheng
 * 创建时间：3/17/22 7:53 PM
 *
 * @param date 既是创建日期，也是ID
 * @param belongTo 所属分类ID
 */
open class BaseEntryBean(val date: Long, val belongTo: Long, val star : Boolean) {

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

    override fun toString(): String {
        return "BaseEntryBean(date=$date, belongTo=$belongTo, star=$star)"
    }
}
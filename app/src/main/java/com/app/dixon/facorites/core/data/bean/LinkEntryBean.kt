package com.app.dixon.facorites.core.data.bean

/**
 * 全路径：com.app.dixon.facorites.core.data.bean
 * 类描述：链接类型
 * 创建人：xuzheng
 * 创建时间：3/17/22 7:52 PM
 *
 * @param link 链接
 * @param title 标题
 * @param remark 备注
 */
class LinkEntryBean(val link: String, val title: String, val remark: String, date: Long, belongTo: Long) : BaseEntryBean(date, belongTo) {

    override fun toString(): String {
        return "LinkEntryBean(link='$link', title='$title', remark='$remark') ${super.toString()}"
    }
}
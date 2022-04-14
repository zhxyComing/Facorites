package com.app.dixon.facorites.core.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：时间工具类
 * 创建人：xuzheng
 * 创建时间：4/7/22 9:03 PM
 */
object TimeUtils {

    fun friendlyTime(timestamp: Long): String {
        if (timestamp == 0L) {
            return ""
        }
        val time = Date(timestamp)
        val timeString: String
        val calendar = Calendar.getInstance()

        // 是否同天
        val currentDate: String = createDateFormat().format(calendar.time)
        val paramDate: String = createDateFormat().format(time)
        if (currentDate == paramDate) {
            val hour = ((calendar.timeInMillis - time.time) / 3600000).toInt()
            timeString = if (hour == 0) Math.max(
                (calendar.timeInMillis - time.time) / 60000L, 1
            ).toString() + "分钟前" else hour.toString() + "小时前"
            return timeString
        }
        val lt = time.time / 86400000
        val ct = calendar.timeInMillis / 86400000
        val days = (ct - lt).toInt()
        return when {
            days == 0 -> {
                val hour = ((calendar.timeInMillis - time.time) / 3600000).toInt()
                if (hour == 0) Math.max(
                    (calendar.timeInMillis - time.time) / 60000, 1
                ).toString() + "分钟前" else hour.toString() + "小时前"
            }
            days == 1 -> "昨天"
            days == 2 -> "前天"
            days in 3..10 -> days.toString() + "天前"
            days < 31 -> (days / 7).toString() + "周前"
            days < 366 -> (days / 30).toString() + "个月前"
            else -> createDateFormat().format(time)
        }
    }

    private fun createDateFormat(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
}
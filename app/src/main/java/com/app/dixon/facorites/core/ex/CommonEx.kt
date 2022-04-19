package com.app.dixon.facorites.core.ex

import android.util.Patterns
import android.webkit.URLUtil
import java.net.URL

/**
 * 全路径：com.app.dixon.facorites.core.ex
 * 类描述：通用扩展方法
 * 创建人：xuzheng
 * 创建时间：3/29/22 5:30 PM
 */
// String -> String_Url
fun String?.try2URL(): String = if (!isNullOrEmpty()) {
    when {
        startsWith("http") -> this
        // 尝试拼接成一个URL
        else -> "https://$this" // 尝试拼接成一个URL
    }
} else ""

// String -> String_Host
// Host正常不带Http，这里返回的带
fun String?.try2Host(): String = if (!isNullOrEmpty()) {
    when {
        startsWith("http") -> {
            try {
                val host = URL(this).host
                var res = ""
                if (host.isNotEmpty()) {
                    res = this.substring(0, this.indexOf(host) + host.length)
                }
                res
            } catch (e: Exception) {
                ""
            }
        }
        // 尝试拼接成一个URL
        else -> {
            try {
                val host = URL("https://$this").host
                var res = ""
                if (host.isNotEmpty()) {
                    res = this.substring(0, this.indexOf(host) + host.length)
                }
                res
            } catch (e: Exception) {
                ""
            }
        }
    }
} else ""

// 尝试转为图标链接
fun String?.try2IconLink(): String {
    val host = this.try2Host()
    if (host.isNotEmpty()) {
        return "$host/favicon.ico"
    }
    return ""
}

// 判断是否是URL
fun String?.isValidUrl(): Boolean {
    if (this == null) return false
    if (Patterns.WEB_URL.matcher(this).matches() || URLUtil.isValidUrl(this)) {
        return true
    }
    return false
}

fun <T> MutableList<T>.removeByCondition(condition: (T) -> Boolean): Boolean {
    val iterator = iterator()
    var hasRemove = false
    while (iterator.hasNext()) {
        if (condition.invoke(iterator.next())) {
            hasRemove = true
            iterator.remove()
        }
    }
    return hasRemove
}

fun <T> MutableList<T>.findByCondition(condition: (T) -> Boolean): T? {
    forEach {
        if (condition.invoke(it)) {
            return it
        }
    }
    return null
}
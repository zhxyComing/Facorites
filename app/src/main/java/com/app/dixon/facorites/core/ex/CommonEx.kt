package com.app.dixon.facorites.core.ex

import android.util.Patterns
import android.webkit.URLUtil
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.ImageEntryBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import java.net.URL
import java.util.regex.Pattern

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

fun <T> List<T>.findByCondition(condition: (T) -> Boolean): T? {
    forEach {
        if (condition.invoke(it)) {
            return it
        }
    }
    return null
}

fun <T> List<T>.findIndexByCondition(condition: (T) -> Boolean): Int? {
    forEachIndexed { index, t ->
        if (condition.invoke(t)) {
            return index
        }
    }
    return null
}


/**
 * 尝试提取Http链接
 *
 * 常见于从第三方APP直接分享到这里，通常链接为 xxx，http...
 */
fun String.tryExtractHttp() = indexOf("http").let {
    if (it != -1) {
        substring(it)
    } else {
        this
    }
}

/**
 * 尝试提取Http链接
 *
 * 正则方式
 */
fun String.tryExtractHttpByMatcher(): String {
    if (!startsWith("http")) {
        val regex =
            "((http[s]{0,1}|ftp)://[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#\$%^&*+?:_/=<>[\\u4e00-\\u9fa5]*]*)+)|(www.[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#\$%^&*+?:_/=<>[\\u4e00-\\u9fa5]*]*)+)";
        val matcher = Pattern.compile(regex).matcher(this)
        if (matcher.find()) {
            return matcher.group()
        }
        return this
    }
    return this
}

/**
 * 根据BaseEntryBean的实际类型进行特殊处理
 */
fun BaseEntryBean.process(linkAction: (linkEntry: LinkEntryBean) -> Unit, imageAction: (imageEntry: ImageEntryBean) -> Unit) {
    (this as? LinkEntryBean)?.let {
        linkAction.invoke(it)
    }
    (this as? ImageEntryBean)?.let {
        imageAction.invoke(it)
    }
}
package com.app.dixon.facorites.core.util

import android.util.Log
import com.app.dixon.facorites.BuildConfig

/**
 * 全路径：com.app.dixon.facorites.core.ex
 * 类描述：日志
 * 创建人：xuzheng
 * 创建时间：3/21/22 7:57 PM
 */
object Ln {

    fun d(tag: String = "facorites", msg: String) {
        if (msg.isNotEmpty() && BuildConfig.DEBUG) {
            longLog(tag, msg, LogType.D)
        }
    }

    fun i(tag: String = "facorites", msg: String) {
        if (msg.isNotEmpty() && BuildConfig.DEBUG) {
            longLog(tag, msg, LogType.I)
        }
    }

    fun e(tag: String = "facorites", msg: String) {
        if (msg.isNotEmpty() && BuildConfig.DEBUG) {
            longLog(tag, msg, LogType.E)
        }
    }

    // 日志中包含耗时操作时使用
    // 比如 Ln.i(tag, 耗时操作输入String)，其实该耗时操作是没必要执行的
    fun safeD(tag: String = "facorites", msg: () -> String) {
        if (BuildConfig.DEBUG) {
            longLog(tag, msg.invoke(), LogType.D)
        }
    }

    fun safeI(tag: String = "facorites", msg: () -> String) {
        if (BuildConfig.DEBUG) {
            longLog(tag, msg.invoke(), LogType.I)
        }
    }

    fun safeE(tag: String = "facorites", msg: () -> String) {
        if (BuildConfig.DEBUG) {
            longLog(tag, msg.invoke(), LogType.E)
        }
    }

    // 支持输出长日志
    private fun longLog(tag: String, msg: String, type: LogType) {
        var msg = msg
        if (tag.isEmpty() || msg.isEmpty()) return
        val segmentSize = 3 * 1024
        val length = msg.length.toLong()
        // 长度小于等于限制直接打印
        if (length <= segmentSize) {
            outputLog(tag, msg, type)
        } else {
            // 循环分段打印日志
            while (msg.length > segmentSize) {
                val logContent = msg.substring(0, segmentSize)
                msg = msg.replace(logContent, "")
                outputLog(tag, logContent, type)
            }
            // 打印剩余日志
            outputLog(tag, msg, type)
        }
    }

    private fun outputLog(tag: String, msg: String, type: LogType) = when (type) {
        LogType.D -> Log.d(tag, msg)
        LogType.I -> Log.i(tag, msg)
        LogType.E -> Log.e(tag, msg)
    }

    private enum class LogType {
        D, I, E
    }
}
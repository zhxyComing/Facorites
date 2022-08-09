package com.app.dixon.facorites.core.util

import android.util.Log
import com.app.dixon.facorites.BuildConfig

/**
 * 全路径：com.app.dixon.facorites.core.ex
 * 类描述：日志
 * 创建人：xuzheng
 * 创建时间：3/21/22 7:57 PM
 */
class Ln {

    companion object {

        fun i(tag: String = "facorites", msg: String) {
            if (msg.isNotEmpty() && BuildConfig.DEBUG) {
                longLog(tag, msg)
            }
        }

        fun e(tag: String = "facorites", msg: String) {
            if (msg.isNotEmpty() && BuildConfig.DEBUG) {
                Log.e(tag, msg)
            }
        }

        // 日志中包含耗时操作时使用
        fun i(tag: String = "facorites", msg: () -> String) {
            if (BuildConfig.DEBUG) {
                Log.i(tag, msg.invoke())
            }
        }

        fun e(tag: String = "facorites", msg: () -> String) {
            if (BuildConfig.DEBUG) {
                Log.e(tag, msg.invoke())
            }
        }

        private fun longLog(tag: String, msg: String) {
            var msg = msg
            if (tag.isEmpty() || msg.isEmpty()) return
            val segmentSize = 3 * 1024
            val length = msg.length.toLong()
            // 长度小于等于限制直接打印
            if (length <= segmentSize) {
                Log.e(tag, msg)
            } else {
                // 循环分段打印日志
                while (msg.length > segmentSize) {
                    val logContent = msg.substring(0, segmentSize)
                    msg = msg.replace(logContent, "")
                    Log.e(tag, logContent)
                }
                // 打印剩余日志
                Log.e(tag, msg)
            }
        }
    }
}
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
                Log.i(tag, msg)
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
    }
}
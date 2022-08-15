package com.app.dixon.facorites.core.util

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.MessageQueue

/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：临时
 * 创建人：xuzheng
 * 创建时间：3/18/22 3:03 PM
 */
object HandlerUtil {

    val UIHandler = Handler(Looper.getMainLooper())

    /**
     * 空闲处理 注意 不一定是主线程 和当前线程有关
     */
    fun postIdle(r: Runnable): MessageQueue.IdleHandler {
        val idleHandler = MessageQueue.IdleHandler {
            r.run()
            false
        }
        getQueue().addIdleHandler(idleHandler)
        return idleHandler
    }

    private fun getQueue(): MessageQueue {
        val myLooper = Looper.myLooper()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            myLooper?.queue ?: UIHandler.looper.queue
        } else {
            Looper.myQueue()
        }
    }
}
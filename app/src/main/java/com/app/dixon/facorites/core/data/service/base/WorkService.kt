package com.app.dixon.facorites.core.data.service.base

import com.app.dixon.facorites.core.util.Ln
import java.util.concurrent.LinkedBlockingQueue
import kotlin.system.measureTimeMillis

/**
 * 全路径：com.app.dixon.facorites.core.data.service
 * 类描述：提供IO服务
 * 创建人：xuzheng
 * 创建时间：3/18/22 11:09 AM
 *
 * 将读写事件交给专门的子线程去处理
 * 该线程处理事件是有序的，无需加锁
 */
class WorkService : IService {

    private val actionQueue = LinkedBlockingQueue<ActionEvent>()

    private lateinit var thread: Thread

    /**
     * 运行服务
     * 新建子线程循环读取Queue进行写入
     */
    override fun runService() {
        thread = Thread(actionRunnable, "ioThread")
        thread.start()
    }

    private val actionRunnable = Runnable {
        try {
            while (true) {
                val ioEvent = actionQueue.take()
                val time = measureTimeMillis {
                    ioEvent.action.invoke()
                }
                Ln.i("WorkService", "ActionEvent Time Consume: $time")
            }
        } catch (e: InterruptedException) {
            Ln.e("WorkService", "Error: $e")
            // 重启服务
            runService()
        }
    }

    /**
     * 发送读写事件
     */
    fun postEvent(actionEvent: ActionEvent) {
        actionQueue.put(actionEvent)
    }

    /**
     * 发送读写事件
     */
    fun postEvent(ioAction: () -> Unit) {
        actionQueue.put(ActionEvent(ioAction))
    }
}
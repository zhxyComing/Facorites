package com.app.dixon.facorites.core.util

import java.util.concurrent.Executor
import java.util.concurrent.Executors

object ThreadExecutor : Executor {

    /**
     * 特点：没有核心线程，任务立即执行，线程空闲可被复用，空闲超 60s 回收
     * 用途：适合随机的、或者大量耗时少的任务。
     */
    private val cacheThreadPool = Executors.newCachedThreadPool()

    override fun execute(command: Runnable) {
        cacheThreadPool.execute(command)
    }
}
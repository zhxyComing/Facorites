package com.app.dixon.facorites.core.ex

import com.app.dixon.facorites.core.util.HandlerUtil

/**
 * 全路径：com.app.dixon.facorites.core.ex
 * 类描述：线程常用扩展方法
 * 创建人：xuzheng
 * 创建时间：3/18/22 2:57 PM
 */

/**
 * 返回主线程执行
 */
fun <T> T.backUi(block: T.() -> Unit) {
    HandlerUtil.UIHandler.post {
        block()
    }
}

/**
 * 返回主线程执行
 */
fun <T> T.backUi(timeDelay: Long, block: T.() -> Unit) {
    HandlerUtil.UIHandler.postDelayed({
        block()
    }, timeDelay)
}
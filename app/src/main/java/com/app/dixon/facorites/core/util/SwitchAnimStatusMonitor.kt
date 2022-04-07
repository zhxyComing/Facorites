package com.app.dixon.facorites.core.util

/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：开关动画的状态监控
 * 创建人：xuzheng
 * 创建时间：4/6/22 4:52 PM
 */

const val SWITCH_STATUS_OPEN = 1
const val SWITCH_STATUS_CLOSE = 0
const val SWITCH_STATUS_CHANGING = -1

class SwitchAnimStatusMonitor(var status: Int) {

    fun setOpen() {
        status = SWITCH_STATUS_OPEN
    }

    fun setClose() {
        status = SWITCH_STATUS_CLOSE
    }

    fun setChanging() {
        status = SWITCH_STATUS_CHANGING
    }

    fun canOpen() = status == SWITCH_STATUS_CLOSE

    fun canClose() = status == SWITCH_STATUS_OPEN
}
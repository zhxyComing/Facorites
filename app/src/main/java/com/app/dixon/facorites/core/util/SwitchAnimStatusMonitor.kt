package com.app.dixon.facorites.core.util

/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：开关动画的状态监控
 * 创建人：xuzheng
 * 创建时间：4/6/22 4:52 PM
 */

const val SWITCH_STATUS_OPEN = 1
const val SWITCH_STATUS_CLOSE = 0
const val SWITCH_STATUS_HIDING = 2
const val SWITCH_STATUS_OPENING = 3

class SwitchAnimStatusMonitor(var status: Int) {

    fun setOpen() {
        status = SWITCH_STATUS_OPEN
    }

    fun setClose() {
        status = SWITCH_STATUS_CLOSE
    }

    fun setClosing() {
        status = SWITCH_STATUS_HIDING
    }

    fun setOpening() {
        status = SWITCH_STATUS_OPENING
    }

    fun canOpen() = status == SWITCH_STATUS_CLOSE

    fun canClose() = status == SWITCH_STATUS_OPEN

    fun isChanging() = status == SWITCH_STATUS_HIDING || status == SWITCH_STATUS_OPENING

    fun isClosing() = status == SWITCH_STATUS_HIDING

    fun isOpening() = status == SWITCH_STATUS_OPENING

    fun isClosed() = status == SWITCH_STATUS_CLOSE

    fun isOpened() = status == SWITCH_STATUS_OPEN
}
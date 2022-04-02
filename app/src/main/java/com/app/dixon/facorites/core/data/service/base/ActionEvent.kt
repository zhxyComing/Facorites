package com.app.dixon.facorites.core.data.service.base

/**
 * 全路径：com.app.dixon.facorites.core.data.service
 * 类描述：耗时操作 由业务方定义
 * 创建人：xuzheng
 * 创建时间：3/18/22 11:11 AM
 */
data class ActionEvent(val action: () -> Unit)
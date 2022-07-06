package com.app.dixon.facorites.core.common

import com.app.dixon.facorites.core.util.Ln
import com.dixon.dlibrary.util.ToastUtil

/**
 * 全路径：com.app.dixon.facorites.core.common
 * 类描述：通用回调
 * 创建人：xuzheng
 * 创建时间：4/5/22 11:05 AM
 */
interface Callback<T> {

    fun onSuccess(data: T)

    fun onFail(msg: String)
}

/**
 * 通用回调
 */
class CommonCallback<T>(private val success: String) : Callback<T> {

    override fun onSuccess(data: T) {
        Ln.i("CommonCallback", "success $data")
        ToastUtil.toast(success)
    }

    override fun onFail(msg: String) {
        Ln.i("CommonCallback", "fail $msg")
        ToastUtil.toast(msg)
    }
}

/**
 * 通用回调
 */
open class SuccessCallback<T>(private val action: (T) -> Unit) : Callback<T> {

    override fun onSuccess(data: T) {
        Ln.i("CommonCallback", "success $data")
        action.invoke(data)
    }

    override fun onFail(msg: String) {
        Ln.i("CommonCallback", "fail $msg")
        ToastUtil.toast(msg)
    }
}

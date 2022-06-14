package com.app.dixon.facorites.core.util

import com.google.gson.Gson
import java.util.*

/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：设备信息
 * 创建人：xuzheng
 * 创建时间：6/14/22 11:18 AM
 */
object DeviceUtil {

    /**
     * 获取设备关键信息
     * 用户Bugly监控
     */
    fun getDeviceKeyInfo(): String {
        // 厂商
        val manufacturer = android.os.Build.MANUFACTURER
        // 产品名
        val product = android.os.Build.PRODUCT
        // 品牌
        val brand = android.os.Build.BRAND
        // 型号
        val model = android.os.Build.MODEL
        // 设备名
        val device = android.os.Build.DEVICE
        // 设备ID
        val deviceId = android.os.Build.ID
        // android 版本
        val androidVersion = android.os.Build.VERSION.RELEASE
        // 语言
        val language = Locale.getDefault().language
        // 返回关键数据的json
        return Gson().toJson(DeviceKeyInfo(manufacturer, product, brand, model, device, deviceId, androidVersion, language))
    }

    // 设备关键信息
    private data class DeviceKeyInfo(
        val manufacturer: String,
        val product: String,
        val brand: String,
        val model: String,
        val device: String,
        val deviceId: String,
        val androidVersion: String,
        val language: String
    )

    /**
     * 获取设备ID
     */
    fun getDeviceID(): String = android.os.Build.ID
}
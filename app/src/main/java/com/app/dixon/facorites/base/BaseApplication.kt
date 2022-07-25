package com.app.dixon.facorites.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.app.dixon.facorites.BuildConfig
import com.app.dixon.facorites.core.common.AGREEMENT_CONFIRM
import com.app.dixon.facorites.core.data.service.BitmapIOService
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.data.service.NoteService
import com.app.dixon.facorites.core.ie.IEService
import com.app.dixon.facorites.core.util.DeviceUtil
import com.dixon.dlibrary.util.DUtil
import com.dixon.dlibrary.util.SharedUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.tencent.bugly.crashreport.CrashReport
import com.umeng.commonsdk.UMConfigure
import java.lang.ref.WeakReference

/**
 * 全路径：com.app.dixon.facorites.base
 * 类描述：BaseApplication
 * 创建人：xuzheng
 * 创建时间：3/17/22 7:37 PM
 */
open class BaseApplication : Application() {

    companion object {

        lateinit var application: Application
        lateinit var currentActivity: WeakReference<Activity>
    }

    override fun onCreate() {
        super.onCreate()
        application = this

        init()
    }

    private fun init() {
        DUtil.init(this)
        if (BuildConfig.DEBUG) {
            DUtil.setDebug(true)
        }
        DUtil.setDefaultFont("MiSans-Normal.ttf")
        DUtil.setSharedPreference(this, "facorites")

        DataService.runService()
        JSoupService.runService()
        BitmapIOService.runService()
        NoteService.runService()
        IEService.runService()

        Fresco.initialize(this)
        initLifecycle()

        // 友盟初始化
        UMConfigure.setLogEnabled(BuildConfig.DEBUG)
        UMConfigure.preInit(this, "62a33c8e05844627b5ab258d", "android")
        if (SharedUtil.getBoolean(AGREEMENT_CONFIRM, false)) {
            UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "")
            CrashReport.initCrashReport(applicationContext, "1a7de272df", BuildConfig.DEBUG)
            // 设置为开发设备
            CrashReport.setIsDevelopmentDevice(applicationContext, BuildConfig.DEBUG)
            // 设置设备信息
            CrashReport.setDeviceModel(applicationContext, DeviceUtil.getDeviceKeyInfo())
            CrashReport.setDeviceId(applicationContext, DeviceUtil.getDeviceID())
        }
    }

    private fun initLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = WeakReference(activity)
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = WeakReference(activity)
            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })
    }
}
package com.app.dixon.facorites.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.dixon.dlibrary.util.DUtil
import com.app.dixon.facorites.BuildConfig
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.JSoupService
import com.facebook.drawee.backends.pipeline.Fresco
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
        DUtil.setDefaultFont("Yun-Book.ttf")
        DUtil.setSharedPreference(this, "facorites")

        DataService.runService()
        JSoupService.runService()

        Fresco.initialize(this)
        initLifecycle()
    }

    private fun initLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

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
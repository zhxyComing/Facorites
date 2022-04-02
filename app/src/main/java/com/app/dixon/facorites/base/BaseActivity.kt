package com.app.dixon.facorites.base

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.dixon.dlibrary.util.StatusBarUtil

/**
 * 全路径：com.app.dixon.facorites.base
 * 类描述：BaseActivity
 * 创建人：xuzheng
 * 创建时间：3/17/22 7:37 PM
 */
open class BaseActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setColorForStatus(this)
    }
}
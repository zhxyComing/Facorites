package com.app.dixon.facorites.core.util

import android.app.Activity
import android.view.View
import com.dixon.dlibrary.util.FontUtil

/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：字体工具类
 * 创建人：xuzheng
 * 创建时间：6/14/22 3:23 PM
 */

/**
 * 普通字体
 */
fun View.normalFont() = FontUtil.font("MiSans-Normal.ttf", this)

/**
 * 中等加粗字体
 */
fun View.mediumFont() = FontUtil.font("MiSans-Medium.ttf", this)

/**
 * 普通字体
 */
fun Activity.normalFont() = FontUtil.font("MiSans-Normal.ttf", this.window.decorView)

/**
 * 中等加粗字体
 */
fun Activity.mediumFont() = FontUtil.font("MiSans-Medium.ttf", this.window.decorView)
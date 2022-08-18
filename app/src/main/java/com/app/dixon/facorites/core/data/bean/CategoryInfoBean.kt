package com.app.dixon.facorites.core.data.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 全路径：com.app.dixon.facorites.core.data.bean.dir
 * 类描述：文件夹类型
 * 创建人：xuzheng
 * 创建时间：3/18/22 3:07 PM
 */
@Parcelize
data class CategoryInfoBean(val id: Long, val name: String, val bgPath: String? = null, val topTimeMs: Long = 0L, val belongTo: Long? = null) : Parcelable
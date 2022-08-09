package com.app.dixon.facorites.page.empty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment

/**
 * 全路径：com.app.dixon.facorites.page.home
 * 类描述：施工空页面
 * 创建人：xuzheng
 * 创建时间：3/22/22 2:49 PM
 *
 * 施工中..
 */

class EmptyFragment : VisibleExtensionFragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.app_fragment_empty_content, container, false)

    override fun onVisibleFirst() {
        super.onVisibleFirst()
    }
}
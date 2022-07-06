package com.app.dixon.facorites.page.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.util.normalFont


/**
 * 浏览器 Fragment
 */

class BrowseFragment : VisibleExtensionFragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.app_fragment_browse_content, container, false).apply {
        normalFont()
    }

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        initView()
    }

    private fun initView() {

    }
}
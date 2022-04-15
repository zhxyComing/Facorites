package com.app.dixon.facorites.page.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import kotlinx.android.synthetic.main.app_fragment_browse_content.*

/**
 * 浏览页 可以浏览网页、图片、视频等
 *
 *  TODO 做成类似Chrome标签卡的样式
 */
class BrowseFragment : VisibleExtensionFragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.app_fragment_browse_content, container, false)

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        initView()
    }

    private fun initView() {
        context?.let {
            webView.loadUrl("https://www.jianshu.com/p/4b6046fb6ee2")
        }
    }
}
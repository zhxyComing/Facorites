package com.app.dixon.facorites.page.empty

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.common.PageJumper
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.ex.hide
import com.app.dixon.facorites.core.ex.process
import com.app.dixon.facorites.core.ex.show
import com.app.dixon.facorites.core.util.CollectionUtil
import com.app.dixon.facorites.core.view.EntryView
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.app_fragment_home_content.*

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
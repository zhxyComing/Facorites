package com.app.dixon.facorites.page.mine

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
import com.app.dixon.facorites.core.ex.*
import com.app.dixon.facorites.core.util.CollectionUtil
import com.app.dixon.facorites.core.view.EntryView
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.app_fragment_home_content.*
import kotlinx.android.synthetic.main.app_fragment_mine_content.*

/**
 * 全路径：com.app.dixon.facorites.page.home
 * 类描述：个人页面
 * 创建人：xuzheng
 * 创建时间：3/22/22 2:49 PM
 */

class MineFragment : VisibleExtensionFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.app_fragment_mine_content, container, false)

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        ivBg.setActualImageResource(R.drawable.app_mine_bg_cover)

        appEdit.setOnClickListener {
            PageJumper.openEditPage(this)
        }
    }
}
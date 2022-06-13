package com.app.dixon.facorites.page.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.common.PageJumper
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

        appAbout.setOnClickListener {
            PageJumper.openMarkdownPage(this, "about.md")
        }
    }

}
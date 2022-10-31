package com.app.dixon.facorites.page.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.AGREEMENT_CONFIRM
import com.app.dixon.facorites.core.common.VERSION_UPDATE_TIP
import com.app.dixon.facorites.core.function.fromshare.FromShareHelper
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.core.view.AgreementDialog
import com.app.dixon.facorites.core.view.CreateEntryDialog
import com.app.dixon.facorites.core.view.TipDialog
import com.app.dixon.facorites.page.category.CategoryFragment
import com.app.dixon.facorites.page.mine.MineFragment
import com.app.dixon.facorites.page.note.NoteFragment
import com.dixon.dlibrary.util.SharedUtil


const val CATEGORY_BG_IMAGE_REQUEST = 100

class HomeActivity : BaseActivity() {

    private lateinit var pager: ViewPager2
    private lateinit var create: View

    private val pages = listOf<Fragment>(
        HomeFragment(),
        CategoryFragment(),
//        BrowseFragment(),
        NoteFragment(),
        MineFragment()
    )

    private lateinit var tabs: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        normalFont()

        initLogic()
        autoParse()
        agreementLogic()
        versionUpdateTip()
    }

    // 版本更新提示
    private fun versionUpdateTip() {
        val pi = packageManager.getPackageInfo(packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode.toInt() else pi.versionCode
        val versionName = pi.versionName
        // 隐私协议已同意、并且没有显示过最新版本的更新提示
        if (SharedUtil.getBoolean(AGREEMENT_CONFIRM, false) &&
            SharedUtil.getInt(VERSION_UPDATE_TIP, 0) < versionCode
        ) {
            TipDialog(this, resources.getString(R.string.app_version_update_tip_1_1_3), "版本更新提醒（$versionName）").show()
            SharedUtil.putInt(VERSION_UPDATE_TIP, versionCode)
        }
    }

    /*
     * 隐私协议
     */
    private fun agreementLogic() {
        if (!SharedUtil.getBoolean(AGREEMENT_CONFIRM, false)) {
            AgreementDialog(this) {
                versionUpdateTip()
            }.show()
        }
    }

    /*
     * 初始化逻辑
     */
    private fun initLogic() {
        initPageView()
        initCreateView()
        initTabView()
    }

    // 页面VP
    private fun initPageView() {
        // 初始化页面列表
        val pagerAdapter = HomePagerAdapter(this)
        pager.adapter = pagerAdapter
        // TODO 优化动画
        // pager.setPageTransformer(ZoomOutPageTransformer())
    }

    // 创建按钮
    private fun initCreateView() {
        create.setOnClickListener {
            // 创建Entry
            CreateEntryDialog(this).show()
        }
    }

    // tabs
    private fun initTabView() {
        tabs.forEachIndexed { index, view ->
            view.setOnClickListener { tab ->
                if (!tab.isSelected) {
                    pager.currentItem = index
                }
            }
        }
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            // 俩种触发页面切换的场景，页面切换后要触发Tab选中：
            // 1.滑动ViewPager；
            // 2.点击Tab；
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabs.forEach { it.isSelected = false }
                tabs[position].isSelected = true
            }
        })
        pager.offscreenPageLimit = 3
    }

    /*
     * 自动解析逻辑 包括：
     * 1.分享而来的数据
     * 2.复制的数据
     */
    private fun autoParse() {
        FromShareHelper().parse(intent)
    }

    override fun onContentChanged() {
        super.onContentChanged()
        pager = findViewById(R.id.pager)
        create = findViewById(R.id.ivCreate)
        tabs = listOf(
            findViewById(R.id.tabHome),
            findViewById(R.id.tabCategory),
            findViewById(R.id.tabDisplay),
            findViewById(R.id.tabMine)
        )
    }

    private inner class HomePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = pages.size

        override fun createFragment(position: Int): Fragment = pages[position]
    }

    override fun onNewIntent(intent: Intent?) {
        // 这句必须调
        setIntent(intent)
        super.onNewIntent(intent)
        autoParse()
    }
}
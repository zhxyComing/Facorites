package com.app.dixon.facorites.page.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.bean.CropInfo
import com.app.dixon.facorites.core.common.AGREEMENT_CONFIRM
import com.app.dixon.facorites.core.common.VERSION_UPDATE_TIP
import com.app.dixon.facorites.core.data.service.BitmapIOService
import com.app.dixon.facorites.core.ex.dp
import com.app.dixon.facorites.core.function.fromshare.FromShareHelper
import com.app.dixon.facorites.core.util.ImageSelectHelper
import com.app.dixon.facorites.core.util.Ln
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.core.view.AgreementDialog
import com.app.dixon.facorites.core.view.CreateEntryDialog
import com.app.dixon.facorites.core.view.ENTRY_IMAGE_REQUEST
import com.app.dixon.facorites.core.view.TipDialog
import com.app.dixon.facorites.page.category.CategoryFragment
import com.app.dixon.facorites.page.category.event.CategoryImageCompleteEvent
import com.app.dixon.facorites.page.mine.MineFragment
import com.app.dixon.facorites.page.note.NoteFragment
import com.dixon.dlibrary.util.SharedUtil
import com.yalantis.ucrop.UCrop
import org.greenrobot.eventbus.EventBus


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

    // ??????????????????
    private fun versionUpdateTip() {
        val pi = packageManager.getPackageInfo(packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode.toInt() else pi.versionCode
        val versionName = pi.versionName
        // ????????????????????????????????????????????????????????????????????????
        if (SharedUtil.getBoolean(AGREEMENT_CONFIRM, false) &&
            SharedUtil.getInt(VERSION_UPDATE_TIP, 0) < versionCode
        ) {
            TipDialog(this, resources.getString(R.string.app_version_update_tip_1_0_5), "?????????????????????$versionName???").show()
            SharedUtil.putInt(VERSION_UPDATE_TIP, versionCode)
        }
    }

    /*
     * ????????????
     */
    private fun agreementLogic() {
        if (!SharedUtil.getBoolean(AGREEMENT_CONFIRM, false)) {
            AgreementDialog(this) {
                versionUpdateTip()
            }.show()
        }
    }

    /*
     * ???????????????
     */
    private fun initLogic() {
        initPageView()
        initCreateView()
        initTabView()
    }

    // ??????VP
    private fun initPageView() {
        // ?????????????????????
        val pagerAdapter = HomePagerAdapter(this)
        pager.adapter = pagerAdapter
        // TODO ????????????
        // pager.setPageTransformer(ZoomOutPageTransformer())
    }

    // ????????????
    private fun initCreateView() {
        create.setOnClickListener {
            // ??????Entry
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
            // ????????????????????????????????????????????????????????????Tab?????????
            // 1.??????ViewPager???
            // 2.??????Tab???
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabs.forEach { it.isSelected = false }
                tabs[position].isSelected = true
            }
        })
        pager.offscreenPageLimit = 3
    }

    /*
     * ?????????????????? ?????????
     * 1.?????????????????????
     * 2.???????????????
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
        // ???????????????
        setIntent(intent)
        super.onNewIntent(intent)
        autoParse()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let {
            if (requestCode == CATEGORY_BG_IMAGE_REQUEST) {
                // 1.???????????????????????????
                it.data?.let { uri ->
                    openCategoryBgCrop(uri)
                }
            } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
                // 2.1 ????????????
                UCrop.getOutput(it)?.let { resultUri ->
                    Ln.i("CropResult", "success $resultUri")
                    EventBus.getDefault().post(CategoryImageCompleteEvent(resultUri))
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                // 2.2 ????????????
                val cropError = UCrop.getError(it)
                Ln.i("CropResult", "fail ${cropError.toString()}")
            } else if (requestCode == ENTRY_IMAGE_REQUEST) {
                // ????????????????????????
                it.data?.let { uri ->
                    Ln.i("ImageResult", "$uri")
                    EventBus.getDefault().post(CategoryImageCompleteEvent(uri))
                }
            } else {
                // do nothing
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // ???????????????????????????
    private fun openCategoryBgCrop(uri: Uri) {
        Ln.i("openCategoryBgCrop", "${400.dp} ${100.dp}")
        ImageSelectHelper.openImageCropPage(
            this, uri,
            BitmapIOService.createBitmapSavePath(),
            CropInfo(aspectX = 3f, aspectY = 1f, outputX = 390.dp, outputY = 130.dp)
        )
    }
}
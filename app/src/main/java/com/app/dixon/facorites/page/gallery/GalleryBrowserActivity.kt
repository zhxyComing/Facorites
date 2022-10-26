package com.app.dixon.facorites.page.gallery

import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.GALLERY_INDEX
import com.app.dixon.facorites.core.common.GALLERY_LIST
import com.app.dixon.facorites.core.ex.byteToString
import com.app.dixon.facorites.core.util.*
import com.app.dixon.facorites.core.view.TipDialog
import com.app.dixon.facorites.page.gallery.adapter.GalleryViewPagerAdapter
import kotlinx.android.synthetic.main.activity_gallery_browser.*
import java.io.File

class GalleryBrowserActivity : BaseActivity() {

    private lateinit var path: List<String>
    private var index: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_browser)

        normalFont()

        intent.getStringArrayListExtra(GALLERY_LIST)?.let {
            path = it
        } ?: let {
            finish()
            return
        }
        index = intent.getIntExtra(GALLERY_INDEX, 0)

        initView()
    }

    private fun initView() {
        vpGalleryList.adapter = GalleryViewPagerAdapter(this, path)
        vpGalleryList.setCurrentItem(index, false)

        tvShare.setOnClickListener {
            val currentIndex = vpGalleryList.currentItem
            path[currentIndex].shareAsImage(this)
        }
        tvSet.setOnClickListener {
            val currentIndex = vpGalleryList.currentItem
            val imagePath = path[currentIndex]
            setSystemWrapper(Uri.fromFile(File(imagePath)))
        }
        tvInfo.setOnClickListener {
            val currentIndex = vpGalleryList.currentItem
            val imagePath = path[currentIndex]
            TipDialog(
                context = this,
                title = "图片信息",
                content = ImageUtil.obtainImageInfo(imagePath)
            ).show()
        }
    }

    override fun statusBarColor(): Int = R.color.black

    override fun onStop() {
        super.onStop()
        ThreadExecutor.execute {
            path.forEach {
                it.clearShareTempFile()
            }
        }
    }
}
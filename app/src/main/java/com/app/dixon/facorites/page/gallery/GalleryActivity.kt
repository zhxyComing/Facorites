package com.app.dixon.facorites.page.gallery

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.GALLERY_LIST
import com.app.dixon.facorites.core.common.GALLERY_NAME
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.page.gallery.adapter.GalleryListAdapter
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : BaseActivity() {

    private lateinit var path: List<String>
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        normalFont()
        tvPageTitle.mediumFont()

        intent.getStringArrayListExtra(GALLERY_LIST)?.let {
            path = it
        } ?: let {
            finish()
            return
        }
        name = intent.getStringExtra(GALLERY_NAME)

        initView()
    }

    private fun initView() {
        rvGalleryList.layoutManager = GridLayoutManager(this, 3)
        rvGalleryList.adapter = GalleryListAdapter(this, path)
        name?.let {
            tvPageTitle.text = name
        }
    }

    override fun statusBarColor(): Int = R.color.black
}
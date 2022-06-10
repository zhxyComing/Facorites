package com.app.dixon.facorites.page.image

import android.net.Uri
import android.os.Bundle
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.IMAGE_PATH
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : BaseActivity() {

    private lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        parseIntent()

        initView()
    }

    private fun initView() {
        photoView.setImageURI(Uri.parse(path))
    }

    private fun parseIntent() {
        intent.getStringExtra(IMAGE_PATH)?.let {
            path = it
        } ?: finish()
    }

    override fun statusBarColor(): Int = R.color.black
}
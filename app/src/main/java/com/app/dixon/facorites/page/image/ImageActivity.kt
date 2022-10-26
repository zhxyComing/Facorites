package com.app.dixon.facorites.page.image

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.IMAGE_PATH
import com.app.dixon.facorites.core.data.service.BitmapIOService
import com.app.dixon.facorites.core.ex.hide
import com.app.dixon.facorites.core.ex.show
import com.app.dixon.facorites.core.util.*
import com.app.dixon.facorites.core.view.CreateImageEntryDialog
import com.app.dixon.facorites.core.view.TipDialog
import com.dixon.dlibrary.util.ToastUtil
import kotlinx.android.synthetic.main.activity_image.*
import kotlinx.android.synthetic.main.activity_image.tvInfo
import kotlinx.android.synthetic.main.activity_image.tvSet
import kotlinx.android.synthetic.main.activity_image.tvShare
import java.io.File

class ImageActivity : BaseActivity() {

    private lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        normalFont()

        intent.getStringExtra(IMAGE_PATH)?.let {
            path = it
        } ?: let {
            finish()
            return
        }

        initView()
    }

    private fun initView() {
        // 从网络读取图片
        if (path.startsWith("http")) {
            tvLoading.show()
            functionBar.hide()
            BitmapIOService.readBitmapFromUrl(path, object : Callback<Bitmap> {
                override fun onSuccess(data: Bitmap) {
                    tvLoading.hide()
                    photoView.setImageBitmap(data)
                    // 初始化功能条
                    initUrlBanner(data)
                }

                override fun onFail(msg: String) {
                    tvLoading.hide()
                    ToastUtil.toast("图片加载失败")
                    finish()
                }
            })
            return
        }
        // 从本地读取图片
        photoView.setImageURI(Uri.parse(path))
        tvShare.setOnClickListener {
            path.shareAsImage(this)
        }
        tvSet.setOnClickListener {
            setSystemWrapper(Uri.fromFile(File(path)))
        }
        tvInfo.setOnClickListener {
            TipDialog(
                context = this,
                title = "图片信息",
                content = ImageUtil.obtainImageInfo(path)
            ).show()
        }
    }

    private fun initUrlBanner(bitmap: Bitmap) {
        llUrlBanner.show()
        tvAdd.setOnClickListener {
            CreateImageEntryDialog(this, bitmap).show()
        }
        tvSave.setOnClickListener {
            BitmapIOService.saveBitmapToAlbum(this, bitmap)
        }
    }

    override fun statusBarColor(): Int = R.color.black

    override fun onStop() {
        super.onStop()
        ThreadExecutor.execute {
            path.clearShareTempFile()
        }
    }
}
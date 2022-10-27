package com.app.dixon.facorites.core.util

import android.content.Intent
import android.provider.MediaStore
import androidx.core.app.ActivityCompat.startActivityForResult
import com.app.dixon.facorites.base.BaseApplication


/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：视频选择器
 * 创建人：xuzheng
 * 创建时间：2022/10/26 15:52
 */

object VideoSelectHelper {

    // 打开视频选择页
    fun openVideoSelectPage(requestCode: Int) {
//        val intent = Intent()
//        /* 开启Pictures画面Type设定为image */
//        //intent.setType("image/*");
//        // intent.setType("audio/*"); //选择音频
//        intent.type = "video/*" //选择视频 （mp4 3gp 是android支持的视频格式）
//        // intent.setType("video/*;image/*");//同时选择视频和图片
//        intent.action = Intent.ACTION_GET_CONTENT
//        BaseApplication.currentActivity.get()?.startActivityForResult(intent, requestCode)

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        BaseApplication.currentActivity.get()?.startActivityForResult(intent, requestCode)
    }
}
package com.app.dixon.facorites.core.data.service

import android.graphics.Bitmap
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import java.util.*

/**
 * 全路径：com.app.dixon.facorites.core.data.service
 * 类描述：图片保存、读取
 * 创建人：xuzheng
 * 创建时间：2022/4/24 17:17
 *
 * (data/data/xxx/file/image)
 */
object BitmapIOService : IService {

    private const val ROOT_PATH = "image" // 根Dir

    private val ioService: WorkService = WorkService()

    override fun runService() {
        ioService.runService()
        if (!FileUtils.exists(ROOT_PATH)) {
            FileUtils.createDir(ROOT_PATH)
        }
    }

    /**
     * 保存图片 现在裁剪页自动保存图片，返回路径，不需要手动保存
     */
    fun saveBitmap(absolutePath: String, bitmap: Bitmap, callback: Callback<String>) {
        ioService.postEvent {
            FileUtils.saveBitmap(absolutePath, bitmap, object : Callback<String> {
                override fun onSuccess(data: String) {
                    backUi { callback.onSuccess(data) }
                }

                override fun onFail(msg: String) {
                    backUi { callback.onFail(msg) }
                }
            })
        }
    }

    /**
     * 从指定路径读取图片
     */
    fun readBitmap(absolutePath: String, callback: Callback<Bitmap>) {
        ioService.postEvent {
            FileUtils.readBitmap(absolutePath)?.let {
                backUi { callback.onSuccess(it) }
            } ?: backUi { callback.onFail("获取图片失败") }
        }
    }

    /**
     * 创建图片的保存路径
     */
    fun createBitmapSavePath() = FileUtils.createBitmapSavePath("$ROOT_PATH/${Date().time}.png")

    /**
     * 删除图片
     */
    fun deleteBitmap(absolutePath: String) {
        ioService.postEvent {
            FileUtils.deleteBitmap(absolutePath)
        }
    }
}
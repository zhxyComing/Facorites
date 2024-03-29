package com.app.dixon.facorites.core.data.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.common.ProgressCallback
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.util.Ln
import com.dixon.dlibrary.util.ToastUtil
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.*


/**
 * 全路径：com.app.dixon.facorites.core.data.service
 * 类描述：文件保存、读取  图片、视频等均属于文件的范畴
 * 创建人：xuzheng
 * 创建时间：2022/10/26 15:52
 *
 * (data/data/xxx/file/file)
 * (data/data/xxx/file/image)
 * (data/data/xxx/file/video)
 */
object FileIOService : IService {

    enum class FileType {
        FILE, IMAGE, VIDEO
    }

    private const val ROOT_PATH_FILE = "file" // 根Dir
    private const val ROOT_PATH_IMAGE = "image" // 根Dir
    private const val ROOT_PATH_VIDEO = "video" // 根Dir

    private val FILE_TYPE_MAP = mapOf(
        FileType.FILE to ROOT_PATH_FILE,
        FileType.IMAGE to ROOT_PATH_IMAGE,
        FileType.VIDEO to ROOT_PATH_VIDEO
    )

    private val ioService: WorkService = WorkService()

    override fun runService() {
        ioService.runService()
        if (!FileUtils.exists(ROOT_PATH_FILE)) {
            FileUtils.createDir(ROOT_PATH_FILE)
        }
        if (!FileUtils.exists(ROOT_PATH_IMAGE)) {
            FileUtils.createDir(ROOT_PATH_IMAGE)
        }
        if (!FileUtils.exists(ROOT_PATH_VIDEO)) {
            FileUtils.createDir(ROOT_PATH_VIDEO)
        }
    }

    /**
     * 保存文件
     * 图片、视频、文件均属于文件
     */
    fun saveFile(fileType: FileType, uri: Uri, callback: ProgressCallback<String>) {
        ioService.postEvent {
            val mime = ContextAssistant.application().contentResolver.getType(uri)
            var suffix = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
            Ln.i("saveFile", "mime:$mime type:$suffix uri:${uri.path}")
            suffix ?: let {
                uri.path?.let { path ->
                    suffix = path.substring(path.lastIndexOf(".") + 1)
                }
            }
            Ln.i("saveFile", "suffix $suffix")
            val savePath = createFileSavePath(fileType, suffix)
            FileUtils.saveFile(uri, savePath, object : ProgressCallback<String> {
                override fun onProgress(progress: Int) {
                    backUi { callback.onProgress(progress) }
                }

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
     * 保存Bitmap
     */
    fun saveBitmap(bitmap: Bitmap, callback: Callback<String>) {
        ioService.postEvent {
            val savePath = createFileSavePath(FileType.IMAGE, "png")
            FileUtils.saveBitmap(savePath, bitmap, object : Callback<String> {
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
     * 读取图片
     */
    fun readBitmap(absolutePath: String, options: BitmapFactory.Options? = null, callback: Callback<Bitmap>) {
        ioService.postEvent {
            FileUtils.readBitmap(absolutePath, options)?.let {
                backUi { callback.onSuccess(it) }
            } ?: backUi { callback.onFail("获取图片失败") }
        }
    }

    /**
     * 从网络读取图片
     */
    fun readBitmapByUrl(url: String, callback: Callback<Bitmap>) {
        ioService.postEvent {
            var bm: Bitmap? = null
            try {
                val iconUrl = URL(url)
                val conn: URLConnection = iconUrl.openConnection()
                val http: HttpURLConnection = conn as HttpURLConnection
                val length: Int = http.contentLength
                conn.connect()
                // 获得图像的字符流
                val `is`: InputStream = conn.getInputStream()
                val bis = BufferedInputStream(`is`, length)
                bm = BitmapFactory.decodeStream(bis)
                bis.close()
                `is`.close() // 关闭流
                backUi { callback.onSuccess(bm) }
            } catch (e: Exception) {
                e.printStackTrace()
                backUi { callback.onFail(e.toString()) }
            }
        }
    }

    /**
     * 保存图片到相册
     */
    fun saveBitmapToAlbum(activity: Context, bitmap: Bitmap) {
        ioService.postEvent {
            doSaveBitmapToAlbum(activity, bitmap)
        }
    }

    /*
     * 将bitmap保存到图库
     */
    private fun doSaveBitmapToAlbum(activity: Context, bitmap: Bitmap) {
        val imageName = System.currentTimeMillis().toString() + ".png"
        // Android Q 10为每个应用程序提供了一个独立的在外部存储设备的存储沙箱，没有其他应用可以直接访问您应用的沙盒文件
        val f = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        f?.let {
            val file = File(f.path.toString() + "/" + imageName) //创建文件
            try {
                // 文件输出流
                val fileOutputStream = FileOutputStream(file)
                // 压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                // 写入，这里会卡顿，因为图片较大
                fileOutputStream.flush()
                // 记得要关闭写入流
                fileOutputStream.close()
                // 成功的提示，写入成功后，请在对应目录中找保存的图片
                Log.e("写入成功！位置目录", f.path.toString() + "/" + imageName)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                ToastUtil.toast(e.message)
            } catch (e: IOException) {
                e.printStackTrace()
                ToastUtil.toast(e.message)
            }

            // 下面的步骤必须有，不然在相册里找不到图片，若不需要让用户知道你保存了图片，可以不写下面的代码。
            // 把文件插入到系统图库
            try {
                MediaStore.Images.Media.insertImage(
                    activity.contentResolver,
                    file.absolutePath, imageName, null
                )
                ToastUtil.toast("保存成功，请您到 相册/图库 中查看")
            } catch (e: FileNotFoundException) {
                ToastUtil.toast("保存失败")
                e.printStackTrace()
            }
            // 最后通知图库更新
            activity.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(File(file.path))
                )
            )
        }
    }

    /**
     * 创建文件的保存路径
     */
    fun createFileSavePath(fileType: FileType, suffix: String? = null): String {
        return suffix?.let {
            FileUtils.createFileSavePath("${FILE_TYPE_MAP[fileType]}/${Date().time}.$suffix")
        } ?: let {
            FileUtils.createFileSavePath("${FILE_TYPE_MAP[fileType]}/${Date().time}")
        }
    }

    /**
     * 删除文件
     */
    fun deleteFile(absolutePath: String) {
        ioService.postEvent {
            FileUtils.deleteFileAbs(absolutePath)
        }
    }

}
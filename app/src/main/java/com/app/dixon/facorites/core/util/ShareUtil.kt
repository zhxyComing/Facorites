import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.text.TextUtils
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.Callback
import com.app.dixon.facorites.core.data.service.base.FileUtils
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.util.ThreadExecutor
import com.app.dixon.facorites.core.view.OptionDialog
import com.app.dixon.facorites.core.view.TipDialog
import com.dixon.dlibrary.util.ToastUtil
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.io.File
import java.util.*

/**
 * 分享文件、图片、文本
 *
 * 来自 https://developer.aliyun.com/article/676802
 */
object ShareUtil {
    /**
     * 分享文本
     *
     * @param context
     * @param path
     */
    fun shareUrl(context: Context, path: String?) {
        if (TextUtils.isEmpty(path)) {
            return
        }
        checkFileUriExposure()
        val it = Intent(Intent.ACTION_SEND)
        it.putExtra(Intent.EXTRA_TEXT, path)
        it.type = "text/plain"
        context.startActivity(Intent.createChooser(it, "分享APP"))
    }

    /**
     * 分享文件
     *
     * @param context
     * @param path
     */
    fun shareFile(context: Context, path: String?) {
        if (TextUtils.isEmpty(path)) {
            return
        }
        checkFileUriExposure()
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(path))) //传输图片或者文件 采用流的方式
        intent.type = "*/*" //分享文件
        context.startActivity(Intent.createChooser(intent, "分享"))
    }

    /**
     * 分享单张图片
     *
     * @param context
     * @param path
     */
    fun shareImage(context: Context, path: String?) {
        shareImage(context, path, null, null, null)
    }

    /**
     * 分享多张图片
     *
     * @param context
     * @param pathList
     */
    fun shareImage(context: Context, pathList: List<String>?) {
        shareImage(context, null, pathList, null, null)
    }

    /**
     * 分享到微信好友，单图
     */
    fun shareImageToWeChat(context: Context, path: String?) {
        //判断是否安装微信，如果没有安装微信 又没有判断就直达微信分享是会挂掉的
        if (!isAppInstall(context, "com.tencent.mm")) {
            ToastUtil.toast(context, "您还没有安装微信")
            return
        }
        shareImage(context, path, null, "com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
    }

    /**
     * 分享到微信好友，多图
     */
    fun shareImageToWeChat(context: Context, pathList: List<String>?) {
        //判断是否安装微信，如果没有安装微信 又没有判断就直达微信分享是会挂掉的
        if (!isAppInstall(context, "com.tencent.mm")) {
            ToastUtil.toast(context, "您还没有安装微信")
            return
        }
        shareImage(context, null, pathList, "com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
    }

    /**
     * 分享到微信朋友圈，单图
     */
    fun shareImageToWeChatFriend(context: Context, path: String?) {
        if (!isAppInstall(context, "com.tencent.mm")) {
            ToastUtil.toast(context, "您还没有安装微信")
            return
        }
        shareImage(context, path, null, "com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI")
    }

    /**
     * 分享到微信朋友圈，多图
     */
    fun shareImageToWeChatFriend(context: Context, pathList: List<String>?) {
        if (!isAppInstall(context, "com.tencent.mm")) {
            ToastUtil.toast(context, "您还没有安装微信")
            return
        }
        shareImage(context, null, pathList, "com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI")
    }

    /**
     * 分享图片给QQ好友，单图
     */
    fun shareImageToQQ(context: Context, path: String?) {
        if (!isAppInstall(context, "com.tencent.mobileqq")) {
            ToastUtil.toast(context, "您还没有安装QQ")
            return
        }
        shareImage(context, path, null, "com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity")
    }

    /**
     * 分享图片给QQ好友，多图
     */
    fun shareImageToQQ(context: Context, pathList: List<String>?) {
        if (!isAppInstall(context, "com.tencent.mobileqq")) {
            ToastUtil.toast(context, "您还没有安装QQ")
            return
        }
        shareImage(context, null, pathList, "com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity")
    }

    /**
     * 分享图片到QQ空间，单图
     */
    fun shareImageToQZone(context: Context, path: String?) {
        if (!isAppInstall(context, "com.qzone")) {
            ToastUtil.toast(context, "您还没有安装QQ空间")
            return
        }
        shareImage(context, path, null, "com.qzone", "com.qzonex.module.operation.ui.QZonePublishMoodActivity")
    }

    /**
     * 分享图片到QQ空间，多图
     */
    fun shareImageToQZone(context: Context, pathList: List<String>?) {
        if (!isAppInstall(context, "com.qzone")) {
            ToastUtil.toast(context, "您还没有安装QQ空间")
            return
        }
        shareImage(context, null, pathList, "com.qzone", "com.qzonex.module.operation.ui.QZonePublishMoodActivity")
    }

    /**
     * 分享图片到微博，单图
     */
    fun shareImageToWeibo(context: Context, path: String?) {
        if (!isAppInstall(context, "com.sina.weibo")) {
            ToastUtil.toast(context, "您还没有安装新浪微博")
            return
        }
        shareImage(context, path, null, "com.sina.weibo", "com.sina.weibo.EditActivity")
    }

    /**
     * 分享图片到微博，多图
     */
    fun shareImageToWeibo(context: Context, pathList: List<String>?) {
        if (!isAppInstall(context, "com.sina.weibo")) {
            ToastUtil.toast(context, "您还没有安装新浪微博")
            return
        }
        shareImage(context, null, pathList, "com.sina.weibo", "com.sina.weibo.EditActivity")
    }

    /**
     * 检测手机是否安装某个应用
     *
     * @param context
     * @param appPackageName 应用包名
     * @return true-安装，false-未安装
     */
    fun isAppInstall(context: Context, appPackageName: String): Boolean {
        val packageManager = context.packageManager // 获取packagemanager
        val pinfo = packageManager.getInstalledPackages(0) // 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (i in pinfo.indices) {
                val pn = pinfo[i].packageName
                if (appPackageName == pn) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 分享前必须执行本代码，主要用于兼容SDK18以上的系统
     */
    private fun checkFileUriExposure() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val builder = VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
            builder.detectFileUriExposure()
        }
    }

    /**
     * @param context  上下文
     * @param path     不为空的时候，表示分享单张图片，会检验图片文件是否存在
     * @param pathList 不为空的时候表示分享多张图片，会检验每一张图片是否存在
     * @param pkg      分享到的指定app的包名
     * @param cls      分享到的页面（微博不需要指定页面）
     */
    private fun shareImage(context: Context, path: String?, pathList: List<String>?, pkg: String?, cls: String?) {
        if (path == null && pathList == null) {
            ToastUtil.toast(context, "找不到您要分享的图片文件")
            return
        }
        checkFileUriExposure()
        try {
            if (path != null) {
                //单张图片
                if (!FileUtils.existsAbs(path)) {
                    ToastUtil.toast(context, "图片不存在，请检查后重试")
                    return
                }
                val intent = Intent()
                if (pkg != null && cls != null) {
                    //指定分享到的app
                    if (pkg == "com.sina.weibo") {
                        //微博分享的需要特殊处理
                        intent.setPackage(pkg)
                    } else {
                        val comp = ComponentName(pkg, cls)
                        intent.component = comp
                    }
                }
                intent.action = Intent.ACTION_SEND
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(path)))
                intent.type = "image/*" //分享文件
                context.startActivity(Intent.createChooser(intent, "分享"))
            } else {
                //多张图片
                val uriList = ArrayList<Uri>()
                for (i in pathList!!.indices) {
                    if (!FileUtils.existsAbs(pathList[i])) {
                        ToastUtil.toast(context, "第" + (i + 1) + "张图片不存在，请检查后重试")
                        return
                    }
                    uriList.add(Uri.fromFile(File(pathList[i])))
                }
                val intent = Intent()
                if (pkg != null && cls != null) {
                    //指定分享到的app
                    if (pkg == "com.sina.weibo") {
                        //微博分享的需要特殊处理
                        intent.setPackage(pkg)
                    } else {
                        val comp = ComponentName(pkg, cls)
                        intent.component = comp
                    }
                }
                intent.action = Intent.ACTION_SEND_MULTIPLE
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.type = "image/*"
                context.startActivity(Intent.createChooser(intent, "分享"))
            }
        } catch (e: Exception) {
            ToastUtil.toast(context, "分享失败，未知错误")
        }
    }
}
package com.app.dixon.facorites.core.data.service.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.app.dixon.facorites.base.ContextAssistant
import com.app.dixon.facorites.core.common.REQUEST_CODE_FOR_SAF_DIR
import java.io.File
import java.io.InputStream

/**
 * 私有目录文件访问类
 * <p>
 * 获取的DocumentFile都是通过fromTreeUri获取的，这种方式获取的DocumentFile才能调用listFiles方法
 */
object DocumentFileUtils {

    private var askPermissionCallback: (() -> Unit)? = null

    // 应用私有目录下的文件都是 DocumentFile
    val documentPath = "${FileUtils.getSDPath()}/Android/data"

    /**
     * 判断 Android/data 私有目录是否有访问权限
     */
    fun isGrant(context: Context): Boolean {
        val uri = Uri.parse(getRootUri())
        for (persistedUriPermission in context.contentResolver.persistedUriPermissions) {
            if (persistedUriPermission.isReadPermission && persistedUriPermission.uri.toString() == uri.toString()) {
                return true
            }
        }
        return false
    }

    /**
     * 申请 Android/data 私有目录权限
     */
    fun askPermission(context: Activity, askPermissionCallback: (() -> Unit)? = null) {
        this.askPermissionCallback = askPermissionCallback
        val uri = changeToTreeUri(documentPath)
        val parse: Uri = Uri.parse(uri)
        val intent = Intent("android.intent.action.OPEN_DOCUMENT_TREE")
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, parse)
        }
        context.startActivityForResult(intent, REQUEST_CODE_FOR_SAF_DIR)
    }

    /**
     * 申请权限的回调
     * 保存这个目录的访问权限
     */
    @SuppressLint("WrongConstant")
    fun askPermissionCallback(contentResolver: ContentResolver, requestCode: Int, data: Intent) {
        // 保存这个目录的访问权限
        val uri: Uri? = data.data
        if (requestCode == REQUEST_CODE_FOR_SAF_DIR && uri != null) {
            contentResolver.takePersistableUriPermission(
                uri, data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            )
        }
        askPermissionCallback?.invoke()
        askPermissionCallback = null
    }

    /**
     * 获取 TreeDocumentFile
     *
     * @param path 文件路径
     */
    fun findDocumentFileByPath(context: Context, path: String): DocumentFile? {
        var document = DocumentFile.fromTreeUri(context, Uri.parse(changeToTreeUri(path)))
        var realPath = path.replace("/storage/emulated/0/", "").replace("Android/data", "")
        if (realPath.startsWith("/")) realPath = realPath.substring(1, realPath.length)
        if (realPath.isEmpty()) return document
        val parts = realPath.split("/").toTypedArray()
        for (element in parts) {
            document = document?.findFile(element)
        }
        return document
    }

    // 转换至uriTree的路径
    private fun changeToTreeUri(path: String): String {
        var path = path
        if (path.endsWith("/")) {
            path = path.substring(0, path.length - 1)
        }
        val path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F")
        return "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A$path2"
    }

    // 获取 Android/data 私有目录的 URI
    private fun getRootUri() = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"

    fun exchangeDFtoFile(documentFile: DocumentFile, absDestPath: String): File? {
        val dirPath = absDestPath.substring(0, absDestPath.lastIndexOf("/"))
        if (!FileUtils.createDirAbs(dirPath)) {
            // 创建文件夹失败
            return null
        }
        if (!FileUtils.existsAbs(absDestPath)) {
            FileUtils.createNewFileAbs(absDestPath)
            if (!FileUtils.existsAbs(absDestPath)) {
                // 创建文件失败
                return null
            }
        }
        val file = File(absDestPath)
        if (documentFile.canRead()) {
            ContextAssistant.asContext {
                it.contentResolver.openInputStream(documentFile.uri)?.let { inputStream ->
                    val bytes = inputStream.readBytes()
                    file.writeBytes(bytes)
                }
            }
            return file
        }
        // 文件不可读
        FileUtils.deleteFile(absDestPath)
        return null
    }
}


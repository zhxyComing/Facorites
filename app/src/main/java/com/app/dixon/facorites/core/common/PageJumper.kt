package com.app.dixon.facorites.core.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.app.dixon.facorites.core.data.bean.CategoryInfoBean
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.page.browse.BrowseActivity
import com.app.dixon.facorites.page.edit.CourseActivity
import com.app.dixon.facorites.page.edit.MarkdownActivity
import com.app.dixon.facorites.page.edit.EditActivity
import com.app.dixon.facorites.page.entry.AllEntryActivity
import com.app.dixon.facorites.page.entry.EntryActivity
import com.app.dixon.facorites.page.home.HomeActivity
import com.app.dixon.facorites.page.image.ImageActivity

/**
 * 全路径：com.app.dixon.facorites.core.common
 * 类描述：负责页面跳转
 * 创建人：xuzheng
 * 创建时间：4/7/22 5:20 PM
 */
object PageJumper {

    /**
     * 打开首页
     */
    fun openHomePage(asContext: Any, requestCode: Int = -1) {
        resultJump(asContext) {
            val intent = Intent(it, HomeActivity::class.java)
            startActivity(it, intent, requestCode)
        }
    }

    /**
     * 打开首页
     */
    fun openHomePage(asContext: Any, link: String, requestCode: Int = -1) {
        resultJump(asContext) {
            val intent = Intent(it, HomeActivity::class.java).apply {
                putExtra(TRANSIT_LINK, link)
            }
            startActivity(it, intent, requestCode)
        }
    }

    /**
     * 打开收藏列表页
     */
    fun openEntryPage(asContext: Any, categoryInfo: CategoryInfoBean, requestCode: Int = -1) {
        resultJump(asContext) {
            val intent = Intent(it, EntryActivity::class.java).apply {
                putExtra(CATEGORY_INFO, categoryInfo)
            }
            startActivity(it, intent, requestCode)
        }
    }

    /**
     * 打开所有收藏列表页
     */
    fun openAllEntryPage(asContext: Any, requestCode: Int = -1) {
        resultJump(asContext) {
            val intent = Intent(it, AllEntryActivity::class.java)
            startActivity(it, intent, requestCode)
        }
    }

    /**
     * 打开浏览页
     */
    fun openBrowsePage(asContext: Any, categoryId: Long, entryId: Long, link: String, requestCode: Int = -1) {
        resultJump(asContext) {
            val intent = Intent(it, BrowseActivity::class.java).apply {
                putExtra(ENTRY_ID, entryId)
                putExtra(CATEGORY_ID, categoryId)
                putExtra(BROWSE_LINK, link)
            }
            startActivity(it, intent, requestCode)
        }
    }

    /**
     * 打开图片浏览页
     */
    fun openImagePage(asContext: Any, path: String, requestCode: Int = -1) {
        resultJump(asContext) {
            val intent = Intent(it, ImageActivity::class.java).apply {
                putExtra(IMAGE_PATH, path)
            }
            startActivity(it, intent, requestCode)
        }
    }

    /**
     * 打开设置页
     */
    fun openEditPage(asContext: Any, requestCode: Int = -1) {
        resultJump(asContext) {
            val intent = Intent(it, EditActivity::class.java)
            startActivity(it, intent, requestCode)
        }
    }

    /**
     * 打开关于页
     */
    fun openMarkdownPage(asContext: Any, assetsName: String, requestCode: Int = -1) {
        resultJump(asContext) {
            val intent = Intent(it, MarkdownActivity::class.java).apply {
                putExtra(MARKDOWN_ASSETS_NAME, assetsName)
            }
            startActivity(it, intent, requestCode)
        }
    }

    /**
     * 打开使用帮助页
     */
    fun openCoursePage(asContext: Any, requestCode: Int = -1) {
        resultJump(asContext) {
            val intent = Intent(it, CourseActivity::class.java)
            startActivity(it, intent, requestCode)
        }
    }

    /**
     * 最终跳转的方法
     *
     * @param asContext
     * @param action
     */
    private fun resultJump(asContext: Any?, action: (Context) -> Unit) {
        val context = when (asContext) {
            is Fragment -> asContext.context
            is android.app.Fragment -> asContext.activity
            is Activity -> asContext
            is Context -> asContext
            else -> null
        } ?: return

        action.invoke(context)
    }

    private fun startActivity(context: Any?, intent: Intent, requestCode: Int) {
        backUi {
            context?.let {
                when (it) {
                    is Fragment -> if (it.isAdded) it.startActivityForResult(intent, requestCode)
                    is android.app.Fragment -> if (it.isAdded) it.startActivityForResult(intent, requestCode)
                    is Activity -> it.startActivityForResult(intent, requestCode)
                    is Context -> it.startActivity(intent.newTask())
                    else -> Unit
                }
            }
        }
    }

    private fun Intent.newTask(): Intent = apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
}
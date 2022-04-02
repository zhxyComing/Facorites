package com.app.dixon.facorites.core.data.service

import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import org.jsoup.Jsoup

/**
 * 全路径：com.app.dixon.facorites.core.data.service
 * 类描述：爬虫 用于爬取网页信息
 * 创建人：xuzheng
 * 创建时间：3/29/22 5:03 PM
 */
object JSoupService : IService {

    private val netService: WorkService = WorkService()

    override fun runService() {
        netService.runService()
    }

    /**
     * 获取标题
     */
    fun askTitle(url: String, callback: JSoupAskCallback<String>) = run {
        try {
            val doc = Jsoup.connect(url).get()
            val head = doc.head()
            val titleElements = head.getElementsByTag("title")
            if (titleElements.isNotEmpty()) {
                val titleElement = titleElements[0]
                var title = ""
                if (titleElement != null) {
                    title = titleElement.text()
                }
                if (title.isNotEmpty()) {
                    backUi { callback.onSuccess(title) }
                    return@run
                }
            }
            backUi { callback.onFail("获取标题失败") }
        } catch (e: Exception) {
            backUi { callback.onFail("链接异常") }
        }
    }

    /**
     * 获取标题的Lambda形式
     */
    fun askTitle(url: String, onSuccess: ((String) -> Unit)? = null, onFail: ((String) -> Unit)? = null) = askTitle(url, lambdaToCallback(onSuccess, onFail))

    // Lambda 转 JavaCallback
    private fun <T> lambdaToCallback(onSuccess: ((T) -> Unit)? = null, onFail: ((String) -> Unit)? = null) = object : JSoupAskCallback<T> {
        override fun onSuccess(data: T) {
            onSuccess?.invoke(data)
        }

        override fun onFail(msg: String) {
            onFail?.invoke(msg)
        }
    }

    // 抛到子线程执行网络请求
    private fun run(ioAction: () -> Unit) = netService.postEvent(ioAction)

    interface JSoupAskCallback<T> {

        fun onSuccess(data: T)

        fun onFail(msg: String)
    }
}
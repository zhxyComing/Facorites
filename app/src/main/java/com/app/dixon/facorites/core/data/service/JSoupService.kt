package com.app.dixon.facorites.core.data.service

import com.app.dixon.facorites.core.data.service.base.IService
import com.app.dixon.facorites.core.data.service.base.WorkService
import com.app.dixon.facorites.core.ex.backUi
import com.app.dixon.facorites.core.util.Ln
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException

/**
 * 全路径：com.app.dixon.facorites.core.data.service
 * 类描述：爬虫 用于爬取网页信息
 * 创建人：xuzheng
 * 创建时间：3/29/22 5:03 PM
 *
 * 需要优先启动的服务
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
            var title = ""
            if (titleElements.isNotEmpty()) {
                val titleElement = titleElements[0]
                if (titleElement != null) {
                    title = titleElement.text()
                }
                // 兜底方案 尝试获取标题
                if (title.isEmpty() && url.contains("mp.weixin.qq.com")) {
                    Ln.i("TryGetTitle", "兜底获取标题 -- 尝试识别微信公众号 -- $url")
                    title = WeiXinHelper.getTitle(url) ?: ""
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

    /**
     * 微信的公众号文章有特殊规则
     */
    private object WeiXinHelper {

        //获取文章封面图片
        @Throws(IOException::class)
        fun getCoverUrl(informationUrl: String?): String {
            var picUrl: String? = null
            var flag: Int
            val doc: Document = Jsoup.connect(informationUrl).timeout(3000).get()
            val htmlString: String = doc.toString()
            flag = htmlString.indexOf("msg_cdn_url")
            while (htmlString[flag] != '\"') {
                flag++
            }
            val beginIndex = ++flag
            while (htmlString[flag] != '\"') flag++
            val endIndex = --flag
            picUrl = htmlString.substring(beginIndex, endIndex)
            return picUrl
        }

        //获取文章作者
        @Throws(IOException::class)
        fun getAuthor(informationUrl: String?): String? {
            val doc: Document = Jsoup.connect(informationUrl).timeout(3000).get()
            val authors: Element = doc.getElementById("js_name")
            return authors.text()
        }

        //获取文章时间
        @Throws(IOException::class)
        fun getTime(informationUrl: String?): String? {
            var time: String? = null
            val doc: Document = Jsoup.connect(informationUrl).timeout(3000).get()
            val scripts: Elements = doc.select("script")
            for (script in scripts) {
                val html: String = script.html()
                if (html.contains("document.getElementById(\"publish_time\")")) {
                    val fromIndex = html.indexOf("s=\"")
                    time = html.substring(fromIndex + 3, fromIndex + 13)
                    return time
                }
            }
            return time
        }

        //获取文章标题
        @Throws(IOException::class)
        fun getTitle(informationUrl: String?): String? {
            val doc: Document = Jsoup.connect(informationUrl).timeout(3000).get()
            val titles: Elements = doc.getElementsByClass("rich_media_title")
            return titles.text()
        }
    }

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
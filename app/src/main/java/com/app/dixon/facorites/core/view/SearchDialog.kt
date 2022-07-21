package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.common.*
import com.app.dixon.facorites.core.ex.dp
import com.app.dixon.facorites.core.util.mediumFont
import com.dixon.dlibrary.util.SharedUtil
import kotlinx.android.synthetic.main.app_dialog_search.*

private const val SEARCH_CONSTANTS = "search_cache"

// 浏览器搜索弹窗
class SearchDialog(context: Context) : BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = 300.dp

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_search

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun gravity(): Int = Gravity.CENTER

    @SuppressLint("SetTextI18n")
    override fun initDialog() {
        llContainer.mediumFont()
        etSearch.setText(SharedUtil.getString(SEARCH_CONSTANTS, ""))
        ivSearchClear.setOnClickListener {
            etSearch.setText("")
        }
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                toSearch()
            }
            false
        }
        tvGoSearch.setOnClickListener {
            toSearch()
        }
    }

    private fun toSearch() {
        val searchContent = etSearch.text.toString()
        SharedUtil.putString(SEARCH_CONSTANTS, searchContent)
        if (searchContent.startsWith("http://") || searchContent.startsWith("https://")) {
            PageJumper.openBrowsePage(context, link = searchContent)
            return
        }
        when (val searchHost = SharedUtil.getString(SEARCH_ENGINE, SEARCH_ENGINE_BAIDU)) {
            SEARCH_ENGINE_GOOGLE -> PageJumper.openBrowsePage(context, link = "${searchHost}search?q=$searchContent", title = searchContent)
            SEARCH_ENGINE_BAIDU -> PageJumper.openBrowsePage(context, link = "${searchHost}s?wd=$searchContent", title = searchContent)
            SEARCH_ENGINE_SOUGOU -> PageJumper.openBrowsePage(context, link = "${searchHost}?query=$searchContent", title = searchContent)
            SEARCH_ENGINE_BING -> PageJumper.openBrowsePage(context, link = "${searchHost}search?q=$searchContent", title = searchContent)
            SEARCH_ENGINE_YANDEX -> PageJumper.openBrowsePage(context, link = "${searchHost}search/?text=$searchContent", title = searchContent)
        }
    }
}
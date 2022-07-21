package com.app.dixon.facorites.page.edit

import android.os.Bundle
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.*
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import com.app.dixon.facorites.core.view.CustomSpinner
import com.app.dixon.facorites.page.edit.event.LastEntryNumUpdateEvent
import com.dixon.dlibrary.util.SharedUtil
import kotlinx.android.synthetic.main.activity_edit.*
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates

class EditActivity : BaseActivity() {

    private var cacheParseLink by Delegates.notNull<Boolean>()
    private var cacheEntryNum by Delegates.notNull<Int>()
    private var cacheSearchEngine by Delegates.notNull<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        normalFont()

        initCacheParams()
        initView()

        ivBack.setOnClickListener {
            finish()
        }

        ivSave.setOnClickListener {
            if (checkParamsChanged()) {
                updateConfig()
            }
            finish()
        }
    }

    private fun initView() {
        linkParse.isChecked = cacheParseLink
        entryNum.setText(cacheEntryNum.toString())

        val expandInfoList = mutableListOf<CustomSpinner.ExpandInfo<String>>()
        expandInfoList.add(CustomSpinner.ExpandInfo(parseSearchEngineName(SEARCH_ENGINE_SOUGOU), cover = null, data = SEARCH_ENGINE_SOUGOU))
        expandInfoList.add(CustomSpinner.ExpandInfo(parseSearchEngineName(SEARCH_ENGINE_BAIDU), cover = null, data = SEARCH_ENGINE_BAIDU))
        expandInfoList.add(CustomSpinner.ExpandInfo(parseSearchEngineName(SEARCH_ENGINE_GOOGLE), cover = null, data = SEARCH_ENGINE_GOOGLE))
        expandInfoList.add(CustomSpinner.ExpandInfo(parseSearchEngineName(SEARCH_ENGINE_BING), cover = null, data = SEARCH_ENGINE_BING))
        expandInfoList.add(CustomSpinner.ExpandInfo(parseSearchEngineName(SEARCH_ENGINE_YANDEX), cover = null, data = SEARCH_ENGINE_YANDEX))
        searchEntrance.setData(expandInfoList)
        searchEntrance.setShowPos(CustomSpinner.ShowPos.LEFT)
        searchEntrance.setSelection(expandInfoList.indexOfFirst {
            it.data == cacheSearchEngine
        })

        linkParse.mediumFont()
        entryNum.mediumFont()
        searchEntrance.mediumFont()
    }

    private fun parseSearchEngineName(cacheSearchEngine: String): String = when (cacheSearchEngine) {
        SEARCH_ENGINE_SOUGOU -> "搜狗"
        SEARCH_ENGINE_BAIDU -> "百度"
        SEARCH_ENGINE_GOOGLE -> "Google"
        SEARCH_ENGINE_BING -> "必应"
        SEARCH_ENGINE_YANDEX -> "Yandex"
        else -> "未知"
    }

    private fun initCacheParams() {
        cacheParseLink = SharedUtil.getBoolean(AUTO_PARSE_LINK, true)
        cacheEntryNum = SharedUtil.getInt(LAST_ENTRY_NUM, 5)
        cacheSearchEngine = SharedUtil.getString(SEARCH_ENGINE, SEARCH_ENGINE_BAIDU)
    }

    // 判断参数有没有发生变化
    private fun checkParamsChanged(): Boolean = linkParse.isChecked != cacheParseLink
            || (entryNum.text.toString().isNotEmpty() && entryNum.text.toString().toInt() != cacheEntryNum)
            || searchEntrance.getSelectionData<String>() != cacheSearchEngine

    private fun updateConfig() {
        if (linkParse.isChecked != cacheParseLink) {
            SharedUtil.putBoolean(AUTO_PARSE_LINK, linkParse.isChecked)
        }
        val newNum = entryNum.text.toString().toInt()
        if (newNum != cacheEntryNum) {
            SharedUtil.putInt(LAST_ENTRY_NUM, newNum)
            EventBus.getDefault().post(LastEntryNumUpdateEvent(newNum))
        }
        val newSearchEngine = searchEntrance.getSelectionData<String>()
        if (newSearchEngine != cacheSearchEngine) {
            SharedUtil.putString(SEARCH_ENGINE, newSearchEngine)
        }
    }
}
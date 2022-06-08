package com.app.dixon.facorites.page.edit

import android.os.Bundle
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.AUTO_PARSE_LINK
import com.app.dixon.facorites.core.common.LAST_ENTRY_NUM
import com.app.dixon.facorites.page.edit.event.LastEntryNumUpdateEvent
import com.dixon.dlibrary.util.SharedUtil
import kotlinx.android.synthetic.main.activity_edit.*
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates

class EditActivity : BaseActivity() {

    private var cacheParseLink by Delegates.notNull<Boolean>()
    private var cacheEntryNum by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

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
    }

    private fun initCacheParams() {
        cacheParseLink = SharedUtil.getBoolean(AUTO_PARSE_LINK, true)
        cacheEntryNum = SharedUtil.getInt(LAST_ENTRY_NUM, 5)
    }

    // 判断参数有没有发生变化
    private fun checkParamsChanged(): Boolean = linkParse.isChecked != cacheParseLink || entryNum.text.toString().toInt() != cacheEntryNum

    private fun updateConfig() {
        if (linkParse.isChecked != cacheParseLink) {
            SharedUtil.putBoolean(AUTO_PARSE_LINK, linkParse.isChecked)
        }
        val newNum = entryNum.text.toString().toInt()
        if (newNum != cacheEntryNum) {
            SharedUtil.putInt(LAST_ENTRY_NUM, newNum)
            EventBus.getDefault().post(LastEntryNumUpdateEvent(newNum))
        }
    }
}
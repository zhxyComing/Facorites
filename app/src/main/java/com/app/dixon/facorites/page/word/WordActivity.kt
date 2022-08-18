package com.app.dixon.facorites.page.word

import android.os.Bundle
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.WORD_CONTENT
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import kotlinx.android.synthetic.main.activity_word.*

class WordActivity : BaseActivity() {

    private lateinit var word: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word)
        normalFont()
        etWordContent.mediumFont()

        intent.getStringExtra(WORD_CONTENT)?.let {
            word = it
        } ?: let {
            finish()
            return
        }

        initView()
    }

    private fun initView() {
        etWordContent.setText(word)
        tvShare.setOnClickListener {
            ShareUtil.shareUrl(this, word)
        }
    }
}
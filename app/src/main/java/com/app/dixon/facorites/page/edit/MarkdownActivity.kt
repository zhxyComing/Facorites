package com.app.dixon.facorites.page.edit

import android.os.Bundle
import br.tiagohm.markdownview.css.styles.Github
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import com.app.dixon.facorites.core.common.MARKDOWN_ASSETS_NAME
import kotlinx.android.synthetic.main.activity_markdown.*


class MarkdownActivity : BaseActivity() {

    private lateinit var assetsName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_markdown)

        intent.getStringExtra(MARKDOWN_ASSETS_NAME)?.let {
            assetsName = it
        } ?: let {
            finish()
            return
        }

        initView()
    }

    private fun initView() {
        // Markdown 初始化
        initMdView()

        ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initMdView() {
        mdView.addStyleSheet(Github())
        mdView.loadMarkdownFromAsset(assetsName)
    }
}
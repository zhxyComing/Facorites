package com.app.dixon.facorites.page.edit

import android.os.Bundle
import br.tiagohm.markdownview.css.styles.Github
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.BaseActivity
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

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
        //InternalStyleSheet css = new InternalStyleSheet();
//        val css = Github()
//        css.addFontFace("Yun-Book", "condensed", "italic", "bold", "url('font/Yun-Book.ttf')")
//        css.addMedia("screen and (min-width: 1281px)")
//        css.addRule("h1", "color: orange")
//        css.endMedia()
//        css.addRule("h1", "color: green", "font-family: MyFont")
//        mdView.addStyleSheet(css)

        mdView.addStyleSheet(Github())
        mdView.loadMarkdownFromAsset("about.md")

        // 其他加载MD的方式
//        mdView.loadMarkdown("**MarkdownView**")
//        mdView.loadMarkdownFromFile(File())
//        mdView.loadMarkdownFromUrl("url")
    }
}
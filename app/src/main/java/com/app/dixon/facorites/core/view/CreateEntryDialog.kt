package com.app.dixon.facorites.core.view

import android.content.Context
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.data.bean.BaseEntryBean
import com.app.dixon.facorites.core.data.bean.LinkEntryBean
import com.app.dixon.facorites.core.data.service.DataService
import com.app.dixon.facorites.core.data.service.JSoupService
import com.app.dixon.facorites.core.ex.try2URL
import com.dixon.dlibrary.util.ScreenUtil
import kotlinx.android.synthetic.main.app_dialog_create_entry_content.*
import java.util.*

class CreateEntryDialog(context: Context, val link: String? = null, private val createCallback: ((Boolean) -> Unit)? = null) :
    BaseDialog(context) {

    override fun heightPx(): Int = PX_AUTO

    override fun widthPx(): Int = ScreenUtil.getDisplayWidth(context) / 4 * 3

    override fun isCancelOnOutSide(): Boolean = true

    override fun contentLayout(): Int = R.layout.app_dialog_create_entry_content

    override fun windowAnimStyle(): Int = R.style.DialogAnimStyle

    override fun initDialog() {
        tvCreate.setOnClickListener {
            val text = etEntryInput.text.toString()
            val title = etEntryTitle.text.toString()
            val remark = etEntryRemark.text.toString()
            if (text.isNotEmpty() && title.isNotEmpty()) {
                DataService.createEntry(
                    DataService.getCategoryList()[0].id,
                    LinkEntryBean(
                        link = text,
                        title = title,
                        remark = remark,
                        date = Date().time
                    )
                ) {
                    createCallback?.invoke(it)
                }
            }
            dismiss()
        }
        etEntryInput.setOnPasteListener {
            parseTitle(it)
        }
        link?.let {
            etEntryInput.setText(it)
            parseTitle(it)
        }
    }

    private fun parseTitle(link: String) {
        if (link.isNotEmpty()) {
            etEntryTitle.hint = "标题解析中.."
            JSoupService.askTitle(link.try2URL(), { data ->
                etEntryTitle.hint = "标题"
                etEntryTitle.setText(data)
            }, {
                etEntryTitle.hint = "标题解析失败，请自行输入"
            })
        }
    }
}
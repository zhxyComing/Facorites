package com.app.dixon.facorites.page.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dixon.facorites.R
import com.app.dixon.facorites.base.VisibleExtensionFragment
import com.app.dixon.facorites.core.data.bean.NoteBean
import com.app.dixon.facorites.core.data.service.NoteService
import com.app.dixon.facorites.core.ex.hide
import com.app.dixon.facorites.core.ex.show
import com.app.dixon.facorites.core.util.mediumFont
import com.app.dixon.facorites.core.util.normalFont
import kotlinx.android.synthetic.main.app_fragment_note_content.*


/**
 * 笔记 Fragment
 */

class NoteFragment : VisibleExtensionFragment(), NoteService.INoteChanged {

    private val notes = arrayListOf<NoteBean>()
    private var adapter: NoteAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.app_fragment_note_content, container, false).apply {
        normalFont()
        findViewById<View>(R.id.tvPageTitle).mediumFont()
    }

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        NoteService.register(this)
        initView()
    }

    private fun initView() {
        notes.addAll(NoteService.obtainNotes())
        context?.let {
            adapter = NoteAdapter(it, notes)
            val controller: LayoutAnimationController = AnimationUtils.loadLayoutAnimation(it, R.anim.app_rv_in_anim)
            rvNotes.layoutAnimation = controller
            rvNotes.layoutManager = LinearLayoutManager(it)
            rvNotes.adapter = adapter
        }
        updateTip()
    }

    private fun updateTip() {
        if (notes.isEmpty()) {
            emptyTip.show()
            rvNotes.hide()
        } else {
            emptyTip.hide()
            rvNotes.show()
        }
    }

    override fun onDataCreated(bean: NoteBean) {
        notes.add(0, bean)
        adapter?.notifyDataSetChanged()
        updateTip()
    }

    override fun onDataDeleted(bean: NoteBean) {
        val index = notes.indexOf(bean)
        notes.remove(bean)
        adapter?.notifyItemRemoved(index)
        updateTip()
    }

    override fun onDataUpdated(bean: NoteBean) {

    }
}
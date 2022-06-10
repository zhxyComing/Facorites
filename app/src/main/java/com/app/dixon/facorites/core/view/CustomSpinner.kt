package com.app.dixon.facorites.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.core.widget.PopupWindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dixon.facorites.R
import com.app.dixon.facorites.core.ex.setImageByPath
import com.app.dixon.facorites.core.ex.show
import com.dixon.dlibrary.util.FontUtil
import kotlinx.android.synthetic.main.app_custom_spinner_expand.view.*
import kotlinx.android.synthetic.main.app_custom_spinner_expand_item.view.*
import kotlinx.android.synthetic.main.app_custom_spinner_group.view.*
import kotlin.math.abs

/**
 * 全路径：com.app.dixon.facorites.core.view
 * 类描述：
 * 创建人：xuzheng
 * 创建时间：6/10/22 11:22 AM
 */
class CustomSpinner @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {

    private lateinit var expand: PopupWindow

    private var data: List<ExpandInfo<*>>? = null
    private var selectionIndex: Int = -1

    private var showPos: ShowPos = ShowPos.BOTTOM

    /**
     * Expand 显示位置
     */
    enum class ShowPos {
        TOP, LEFT, RIGHT, BOTTOM
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.app_custom_spinner_group, this, true)
    }

    /**
     * 设置 Expand 显示位置
     */
    fun setShowPos(type: ShowPos) {
        showPos = type
    }

    /**
     * 设置数据
     */
    fun <T> setData(list: List<ExpandInfo<T>>) {
        data = list
        setSelection(0)
        initExpand()
        tvChoose.setOnClickListener {
            showExpand()
        }
    }

    private fun makeDropDownMeasureSpec(measureSpec: Int): Int {
        val mode = if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
            MeasureSpec.UNSPECIFIED
        } else {
            MeasureSpec.EXACTLY
        }
        return MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(measureSpec), mode)
    }

    // 参考链接 https://www.jianshu.com/p/6c32889e6377
    private fun showExpand() = when (showPos) {
        ShowPos.TOP -> {
            val offsetX = abs(expand.contentView.measuredWidth - width) / 2
            val offsetY = -(expand.contentView.measuredHeight + height)
            PopupWindowCompat.showAsDropDown(expand, this, offsetX, offsetY, Gravity.START)
        }
        ShowPos.LEFT -> {
            val offsetX = -expand.contentView.measuredWidth;
            val offsetY = -(expand.contentView.measuredHeight + height) / 2
            PopupWindowCompat.showAsDropDown(expand, this, offsetX, offsetY, Gravity.START)
        }
        ShowPos.RIGHT -> {
            val offsetX = 0
            val offsetY = -(expand.contentView.measuredHeight + height) / 2
            PopupWindowCompat.showAsDropDown(expand, this, offsetX, offsetY, Gravity.END)
        }
        ShowPos.BOTTOM -> {
            val offsetX = abs(expand.contentView.measuredWidth - width) / 2
            val offsetY = 0
            PopupWindowCompat.showAsDropDown(expand, this, offsetX, offsetY, Gravity.START)
        }
    }

    /**
     * 设置选中条目
     */
    fun setSelection(index: Int) {
        val expandInfo = data?.getOrNull(index)
        tvChoose.text = expandInfo?.content ?: ""
        selectionIndex = index
    }

    /**
     * 获取选中条目附加数据
     *
     * 注意需要和设置的数据保持同种类型 否则指定类型错误会返回空
     */
    fun <T> getSelectionData(): T? {
        val expandInfo = data?.getOrNull(selectionIndex)
        return expandInfo?.data as? T
    }

    private fun initExpand() {
        val contentView: View = LayoutInflater.from(context).inflate(R.layout.app_custom_spinner_expand, null)
        with(contentView.rvExpand) {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = ExpandAdapter(context, data as List<ExpandInfo<Any>>) { index, _ ->
                setSelection(index)
                expand.dismiss()
            }
        }

        expand = PopupWindow(this)
        expand.contentView = contentView
        expand.width = ViewGroup.LayoutParams.WRAP_CONTENT
        expand.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // popWindow 显示时，点击外部优先关闭 window
        expand.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        expand.isOutsideTouchable = true
        expand.isTouchable = true

        // 在展示之前先执行一次测量 避免后续获取宽高为0
        expand.contentView.measure(
            makeDropDownMeasureSpec(expand.width),
            makeDropDownMeasureSpec(expand.height)
        )
    }

    data class ExpandInfo<T>(val content: String, val cover: String?, val data: T)

    private class ExpandAdapter<T>(val context: Context, val data: List<ExpandInfo<T>>, val itemClickListener: (Int, T) -> Unit) : RecyclerView.Adapter<ExpandAdapter.ExpandViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandViewHolder {
            val item = LayoutInflater.from(context).inflate(R.layout.app_custom_spinner_expand_item, parent, false)
            FontUtil.font(item)
            return ExpandViewHolder(item)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ExpandViewHolder, position: Int) {
            val expandInfo = data[position]
            holder.itemView.tvItem.text = expandInfo.content
            holder.itemView.itemContainer.setOnClickListener {
                itemClickListener.invoke(position, expandInfo.data)
            }
            expandInfo.cover?.let {
                holder.itemView.bgView.show()
                holder.itemView.maskView.show()
                holder.itemView.bgView.setImageByPath(it, 300, 30)
            }
        }

        override fun getItemCount(): Int = data.size

        class ExpandViewHolder(item: View) : RecyclerView.ViewHolder(item)
    }
}
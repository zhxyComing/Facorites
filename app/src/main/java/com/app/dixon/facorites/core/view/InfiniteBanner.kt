package com.app.dixon.facorites.core.view

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.dixon.dlibrary.util.Ln

/**
 * 创建人：xuzheng
 * 说明：无限轮播Banner
 */
class InfiniteBanner @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    private val looper = Handler(context.mainLooper)
    private var loopEnable = false // 是否可循环
    private var isLooping = false // 是否在循环中
    private var loopDelay = 5000L

    override fun setAdapter(adapter: PagerAdapter?) {
        throw IllegalAccessException("You cannot set an Adapter in this class")
    }

    /**
     * 配置数据和如何展示数据
     *
     * @param data 数据
     * @param bindView 如何展示数据
     */
    fun <T> setParams(data: List<T>, bindView: (inflate: LayoutInflater, container: ViewGroup, bean: T) -> View, loop: Boolean = true) {
        val adapter = object : InfiniteAdapter() {
            override fun getItemCount(): Int = data.size
            override fun instantiateItem(container: ViewGroup, itemPos: Int, dataPos: Int): View =
                bindView(LayoutInflater.from(context), container, data[dataPos]).apply {
                    container.addView(this)
                }
        }
        super.setAdapter(adapter)
        adapter.initialize(this)

        loopEnable = loop
        startLoop()
    }

    /**
     * 轮播设置
     */
    fun setLoop(loopEnable: Boolean, millisecond: Long = 5000) {
        this.loopEnable = loopEnable
        this.loopDelay = millisecond
    }

    private fun startLoop() {
        if (!isLooping && loopEnable) {
            Ln.i("InfiniteBanner", "startLoop")
            looper.postDelayed(loopRunnable, loopDelay)
            isLooping = true
        }
    }

    private fun stopLoop() {
        if (isLooping) {
            Ln.i("InfiniteBanner", "stopLoop")
            looper.removeCallbacks(loopRunnable)
            isLooping = false
        }
    }

    private val loopRunnable = object : Runnable {
        override fun run() {
            var targetItem = currentItem + 1
            if (targetItem == adapter!!.count - 1) {
                targetItem = (adapter as InfiniteAdapter).getStartItem()
                setCurrentItem(targetItem, false)
                looper.postDelayed(this, loopDelay)
            } else {
                currentItem = targetItem
                Ln.i("InfiniteBanner", "setItem $currentItem")
                looper.postDelayed(this, loopDelay)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> stopLoop()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> startLoop()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == View.VISIBLE) {
            startLoop()
        } else {
            stopLoop()
        }
    }

    /**
     * 伪无限Adapter
     */
    private abstract class InfiniteAdapter : PagerAdapter() {

        companion object {
            const val MAX_COUNT = 300
        }

        override fun getCount(): Int = if (getItemCount() > 1) MAX_COUNT else getItemCount()

        abstract fun getItemCount(): Int

        /**
         * 初始化位置
         */
        fun initialize(pager: ViewPager) {
            pager.currentItem = getStartItem()
        }

        /** 初始位置 */
        fun getStartItem(): Int {
            if (getItemCount() == 0) return 0
            var currentItem: Int = MAX_COUNT / 2
            if (currentItem % getItemCount() == 0) {
                return currentItem
            }
            while (currentItem % getItemCount() != 0) {
                currentItem++
            }
            return currentItem
        }

        override fun isViewFromObject(view: View, item: Any): Boolean = view === item

        override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
            container.removeView(item as View)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return instantiateItem(container, position, position % getItemCount())
        }

        abstract fun instantiateItem(container: ViewGroup, itemPos: Int, dataPos: Int): View
    }

    /**
     * 设置切换监听
     */
    fun addOnItemChangedListener(listener: (pos: Int) -> Unit) {
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                listener(position % (adapter as InfiniteAdapter).getItemCount())
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopLoop()
    }
}
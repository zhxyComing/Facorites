package com.app.dixon.facorites.core.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import java.util.*

/**
 * 全路径：com.app.dixon.facorites.core.util
 * 类描述：
 * 创建人：xuzheng
 * 创建时间：4/12/22 8:50 PM
 */
class AnimChain {
    private var index = 0
    private val animators: MutableList<Animator> = ArrayList()
    fun addAnimator(vararg animator: Animator): AnimChain {
        animators.addAll(listOf(*animator))
        return this
    }

    fun start() {
        if (animators.size > index) { //size!=下标
            val animator = animators[index++]
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    start()
                }
            })
            animator.start()
        }
    }

    fun cancel() {
        animators.forEach {
            it.cancel()
        }
    }
}
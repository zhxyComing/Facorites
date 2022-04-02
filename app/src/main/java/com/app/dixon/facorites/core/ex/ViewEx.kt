package com.app.dixon.facorites.core.ex

import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * 全路径：com.app.dixon.facorites.core.ex
 * 类描述：View 的扩展方法
 * 创建人：xuzheng
 * 创建时间：4/1/22 5:07 PM
 */
fun View?.hide() {
    this?.visibility = View.GONE
}

fun View?.show() {
    this?.visibility = View.VISIBLE
}

/**
 * 当 condition 为 true 时，执行action
 *
 * @param shouldShow
 * @param action
 */
fun <T : View> T?.showIf(shouldShow: Boolean, action: (T.() -> Unit)? = null) {
    this?.visibility = if (shouldShow) View.VISIBLE else View.GONE
    if (shouldShow) action?.invoke(this!!)
}

fun View?.hideIf(shouldHide: Boolean) {
    showIf(!shouldHide)
}

fun View?.invisible() {
    this?.visibility = View.INVISIBLE
}

fun View?.isVisible() = this?.visibility == View.VISIBLE
fun View?.isGone() = this?.visibility == View.GONE
fun View?.isInvisible() = this?.visibility == View.INVISIBLE

fun TextView?.setTextOrHide(text: CharSequence) {
    this?.text = text
    showIf(text.isNotEmpty())
}

/**
 * 设置View的MarginStart
 *
 * @param marginStart
 */
fun View?.setMarginStart(marginStart: Int) {
    val param = this?.layoutParams ?: return
    if (param is ViewGroup.MarginLayoutParams) {
        param.marginStart = marginStart
    }
    this.layoutParams = param
}

/**
 * 设置View的MarginEnd
 *
 * @param marginEnd
 */
fun View?.setMarginEnd(marginEnd: Int) {
    val param = this?.layoutParams ?: return
    if (param is ViewGroup.MarginLayoutParams) {
        param.marginEnd = marginEnd
    }
    this.layoutParams = param
}

/**
 * view 拓展方法
 *
 * @param left padding值
 */
fun View?.setPaddingLeft(left: Int) {
    this?.setPadding(left, this.paddingTop, this.paddingRight, this.paddingBottom)
}

/**
 * view 拓展方法
 *
 * @param top padding值
 */
fun View?.setPaddingTop(top: Int) {
    this?.setPadding(this.paddingLeft, top, this.paddingRight, this.paddingBottom)
}

/**
 * view 拓展方法
 *
 * @param right padding值
 */
fun View?.setPaddingRight(right: Int) {
    this?.setPadding(this.paddingLeft, this.paddingTop, right, this.paddingBottom)
}

/**
 * view 拓展方法
 *
 * @param bottom padding值
 */
fun View?.setPaddingBottom(bottom: Int) {
    this?.setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, bottom)
}

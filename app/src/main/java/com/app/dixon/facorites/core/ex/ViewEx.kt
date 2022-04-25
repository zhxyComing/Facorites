package com.app.dixon.facorites.core.ex

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.TextView
import com.app.dixon.facorites.base.BaseApplication
import com.facebook.drawee.view.SimpleDraweeView
import java.io.File

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

/**
 * view 抖动动画 表示数据错误
 */
fun View.shakeTip() {
    val translateAnimation = TranslateAnimation(0f, 10f, 0f, 0f)
    translateAnimation.interpolator = CycleInterpolator(6f)
    translateAnimation.duration = 500
    startAnimation(translateAnimation)
}

/**
 * TextView 空数据提示
 */
fun TextView.shakeTipIfEmpty() {
    if (text.toString().isEmpty()) {
        shakeTip()
    }
}

/**
 * 安全的使用SimpleDraweeView，防止内存泄漏
 * SimpleDraweeView.setImageURI(Uri uri, @Nullable Object callerContext) context如果是Activity，会导致内存泄漏...
 */
fun SimpleDraweeView.setImageByPath(path: String?) {
    if (path.isNullOrEmpty()) return
    if (path.startsWith("file://")) {
        setImageURI(path)
        return
    }
    if (path.startsWith("http")) {
        setImageURI(Uri.parse(path), BaseApplication.application)
        return
    }
    setImageURI(Uri.fromFile(File(path)), BaseApplication.application)
}

/**
 * 安全的使用SimpleDraweeView，防止内存泄漏
 * SimpleDraweeView.setImageURI(Uri uri, @Nullable Object callerContext) context如果是Activity，会导致内存泄漏...
 */
fun SimpleDraweeView.setImageByUri(uri: Uri?) {
    if (uri == null) return
    setImageURI(uri, BaseApplication.application)
}
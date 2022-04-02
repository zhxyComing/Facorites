package com.app.dixon.facorites.base

import androidx.fragment.app.Fragment
import com.app.dixon.facorites.core.ex.backUi

/**
 * VisibleExtensionFragment
 */
open class VisibleExtensionFragment : Fragment() {

    // Fragment 是否可见
    private var isFVisible = false

    // Fragment 是否是第一次可见
    private var isFVisibleFirst = true

    private fun visibleLogic() {
        // isHidden 的返回值有时候会错误 疑似 Android FrameWork Bug
        if (isResumed && !isHidden && !isFVisible) {
            isFVisible = true
            onVisible()
            if (isFVisibleFirst) {
                isFVisibleFirst = false
                onVisibleFirst()
            } else {
                onVisibleExceptFirst()
            }
        }
    }

    private fun invisibleLogic() {
        if (isFVisible) {
            isFVisible = false
            onInVisible()
        }
    }

    override fun onResume() {
        super.onResume()
        backUi { visibleLogic() }
    }

    override fun onPause() {
        super.onPause()
        invisibleLogic()
    }

    protected open fun onVisibleFirst() {}

    protected open fun onVisibleExceptFirst() {}

    protected open fun onVisible() {}

    protected open fun onInVisible() {}
}
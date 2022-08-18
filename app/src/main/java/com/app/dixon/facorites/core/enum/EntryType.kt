package com.app.dixon.facorites.core.enum

import androidx.annotation.IntDef

@Target(AnnotationTarget.VALUE_PARAMETER)
    @MustBeDocumented
    @IntDef(EntryType.LINK, EntryType.IMAGE, EntryType.WORD, EntryType.GALLERY)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation
    class EntryType {
        companion object {
            const val LINK = 0x0
            const val IMAGE = 0x1
            const val WORD = 0x2
            const val GALLERY = 0x3
        }
    }
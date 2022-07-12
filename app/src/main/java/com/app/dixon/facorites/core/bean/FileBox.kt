package com.app.dixon.facorites.core.bean

import androidx.documentfile.provider.DocumentFile
import java.io.File

data class FileBox(val file: File? = null, val documentFile: DocumentFile? = null) {

    fun process(fileAction: (File) -> Unit, documentFileAction: (DocumentFile) -> Unit) {
        file?.let(fileAction) ?: documentFile?.let(documentFileAction)
    }
}

package com.narkolep.skkimmer.keyboard

import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection

/**
 * AndroidのInputConnectionを分離したclass
 **/
class InputCommitter(
    private val connectionProvider: () -> InputConnection?
) {
    fun selectAll() {
        connectionProvider()?.performContextMenuAction(
            android.R.id.selectAll
        )
    }

    fun cut() {
        connectionProvider()?.performContextMenuAction(
            android.R.id.cut
        )
    }

    fun copy() {
        connectionProvider()?.performContextMenuAction(
            android.R.id.copy
        )
    }

    fun paste() {
        connectionProvider()?.performContextMenuAction(
            android.R.id.paste
        )
    }

    fun commit(text: String) {
        connectionProvider()?.commitText(text, 1)
    }

    fun setComposingText(text: String) {
        connectionProvider()?.setComposingText(text, 1)
    }

    fun isSelected(): Boolean {
        return !connectionProvider()?.getSelectedText(0).isNullOrEmpty()
    }

    fun delete() {
        connectionProvider()?.deleteSurroundingText(1, 0)
    }

    fun performEditorAction(action: Int) {
        connectionProvider()?.performEditorAction(action)
    }

    fun getExtractedText(): ExtractedText? {
        return connectionProvider()?.getExtractedText(ExtractedTextRequest(), 0)
    }

    fun setSelection(start: Int, end: Int) {
        connectionProvider()?.setSelection(start, end)
    }

    fun getText(position: Int): CharSequence? {
        return connectionProvider()?.getTextBeforeCursor(position, 0)?.toString()
    }
}
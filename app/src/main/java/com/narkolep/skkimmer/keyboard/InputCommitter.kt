package com.narkolep.skkimmer.keyboard

import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import kotlin.math.abs
import android.icu.text.BreakIterator

/**
 * AndroidのInputConnectionを分離したclass
 */
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

    fun commit(text: String) {
        connectionProvider()?.commitText(text, 1)
    }

    fun setComposingText(text: String) {
        connectionProvider()?.setComposingText(text, 1)
    }

    fun isSelected(): Boolean {
        return !connectionProvider()?.getSelectedText(0).isNullOrEmpty()
    }

    fun delete(count: Int = 1) {
        val textBefore = getText(16 * count) ?: return
        if (textBefore.isEmpty()) return

        var boundary = textBefore.length
        repeat(count) {
            boundary = boundaryBefore(textBefore, boundary)
        }

        connectionProvider()?.deleteSurroundingText(textBefore.length - boundary, 0)
    }

    fun moveCursor(offset: Int) {
        if (offset == 0) return

        val extracted = connectionProvider()?.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val text = extracted.text ?: return

        var newPos = extracted.selectionStart
        val direction = if (offset > 0) 1 else -1

        repeat(abs(offset)) {
            newPos = if (direction > 0) {
                boundaryAfter(text, newPos)
            } else {
                boundaryBefore(text, newPos)
            }
        }

        connectionProvider()?.setSelection(newPos, newPos)
    }

    /**
     *  posの直後にある1文字(書記素クラスタ)分の終端位置を返す
     */
    private fun boundaryAfter(text: CharSequence, pos: Int): Int {
        if (pos >= text.length) return text.length
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(text.toString())
        val next = iterator.following(pos)
        return if (next == BreakIterator.DONE) text.length else next
    }

    /**
     *  posの直前にある1文字(書記素クラスタ)分の開始位置を返す
     */
    private fun boundaryBefore(text: CharSequence, pos: Int): Int {
        if (pos <= 0) return 0
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(text.toString())
        val prev = iterator.preceding(pos)
        return if (prev == BreakIterator.DONE) 0 else prev
    }

    fun performEditorAction(action: Int) {
        connectionProvider()?.performEditorAction(action)
    }

    fun getText(position: Int): CharSequence? {
        return connectionProvider()?.getTextBeforeCursor(position, 0)?.toString()
    }

    fun beginBatch() {
        connectionProvider()?.beginBatchEdit()
    }

    fun endBatch() {
        connectionProvider()?.endBatchEdit()
    }
}
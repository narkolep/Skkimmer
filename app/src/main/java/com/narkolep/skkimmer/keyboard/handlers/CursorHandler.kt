package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputCommitter

class CursorHandler(
    private val inputCommitter: InputCommitter,
) {
    fun handleLeft() {
        moveCursor(-1)
    }

    fun handleRight() {
        moveCursor(1)
    }

    private fun moveCursor(offset: Int) {
        val extracted = inputCommitter.getExtractedText() ?: return
        val textLength = extracted.text?.length ?: 0
        val current = extracted.selectionStart
        val newPos = (current + offset).coerceIn(0, textLength)

        inputCommitter.setSelection(newPos, newPos)
    }
}
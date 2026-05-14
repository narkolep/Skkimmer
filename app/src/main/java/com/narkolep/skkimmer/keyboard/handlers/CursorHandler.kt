package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.SkkUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class CursorHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val inputCommitter: InputCommitter,
) {
    fun handleLeft() {
        stateFlow.update {
            it.copy(
                firstChar = "",
                secondChar = ""
            )
        }
        moveCursor(-1)
    }

    fun handleRight() {
        stateFlow.update {
            it.copy(
                firstChar = "",
                secondChar = ""
            )
        }
        moveCursor(1)
    }

    private fun moveCursor(offset: Int) {
        val extracted = inputCommitter.getExtractedText() ?: return
        val textLength = extracted.text?.length ?: 0
        val current = extracted.selectionStart
        val newPos = (current + offset).coerceIn(0, textLength)

        inputCommitter.setSelection(newPos)
    }
}
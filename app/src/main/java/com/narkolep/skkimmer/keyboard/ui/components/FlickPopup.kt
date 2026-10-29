package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.narkolep.skkimmer.keyboard.ui.layouts.FlickKanaMap
import com.narkolep.skkimmer.keyboard.ui.theme.LocalKeyboardColors

@Composable
fun FlickPopup(
    flickText: FlickKanaMap.Position,
    currentDir: FlickDirection,
    paddingDp: Dp,
    keyWidth: Dp,
    keyHeight: Dp
) {
    val colors = LocalKeyboardColors.current
    
    val activeBgColor = colors.flickKeyBackground.copy(alpha = 1.0f)
    val inactiveBgColor = colors.keyBackground.copy(alpha = 1.0f)
    
    // 文字ブロックを描画する関数
    @Composable
    fun PopupChar(text: String, isActive: Boolean) {
        if (text.isEmpty()) {
            Spacer(
                modifier = Modifier
                    .size(width = keyWidth, height = keyHeight)
                    .padding(paddingDp/3)
            )
            return
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = keyWidth, height = keyHeight)
                .padding(paddingDp/3)
                .background(
                    color = if (isActive) activeBgColor else inactiveBgColor,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Text(
                text = text,
                color = colors.keyText,
                fontSize = 26.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    // レイアウト
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(paddingDp)
    ) {
        // 上段
        Row(horizontalArrangement = Arrangement.spacedBy(paddingDp)) {
            if (flickText.up.isNotEmpty()) {
                if (flickText.left.isNotEmpty()) PopupChar("", false)
                PopupChar(
                    text = flickText.up,
                    isActive = currentDir == FlickDirection.UP
                )
                if (flickText.right.isNotEmpty()) PopupChar("", false)
            }
        }
        // 中段
        Row(horizontalArrangement = Arrangement.spacedBy(paddingDp)) {
            if (flickText.left.isNotEmpty()) PopupChar(text = flickText.left, isActive = currentDir == FlickDirection.LEFT)
            PopupChar(
                text = flickText.center,
                isActive = currentDir == FlickDirection.CENTER
            )
            if (flickText.right.isNotEmpty()) PopupChar(text = flickText.right, isActive = currentDir == FlickDirection.RIGHT)
        }
        // 下段
        Row(horizontalArrangement = Arrangement.spacedBy(paddingDp*2)) {
            if (flickText.down.isNotEmpty()) {
                if (flickText.left.isNotEmpty()) PopupChar("", false)
                PopupChar(
                    text = flickText.down,
                    isActive = currentDir == FlickDirection.DOWN
                )
                if (flickText.right.isNotEmpty()) PopupChar("", false)
            }
        }
    }
}
package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.narkolep.skkimmer.keyboard.ui.layouts.FlickKanaMap
import com.narkolep.skkimmer.keyboard.ui.theme.LocalKeyboardColors

@Composable
fun FlickPopup(
    flickText: FlickKanaMap.Position,
    currentDir: FlickDirection
) {
    val colors = LocalKeyboardColors.current

    // ガイドチップ
    Box(
        modifier = Modifier
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(colors.keyBackground.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
            .border(1.dp, colors.keyText.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 上
            GuideBadge(flickText.up, isActive = currentDir == FlickDirection.UP)
            // 中段（左、中央、右）
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                GuideBadge(flickText.left, isActive = currentDir == FlickDirection.LEFT)
                GuideBadge(flickText.center, isActive = currentDir == FlickDirection.CENTER)
                GuideBadge(flickText.right, isActive = currentDir == FlickDirection.RIGHT)
            }
            // 下
            GuideBadge(flickText.down, isActive = currentDir == FlickDirection.DOWN)
        }
    }
}

@Composable
private fun GuideBadge(text: String, isActive: Boolean) {
    val colors = LocalKeyboardColors.current
    if (text.isEmpty()) {
        Spacer(modifier = Modifier.size(40.dp))
        return
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (isActive) colors.flickKeyBackground else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isActive) colors.keyText else colors.keyText.copy(alpha = 0.7f),
            fontSize = 22.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}
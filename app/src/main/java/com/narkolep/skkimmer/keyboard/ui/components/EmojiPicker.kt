package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.narkolep.skkimmer.data.EmojiManager
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import com.narkolep.skkimmer.ui.theme.AppFontFamily

@Composable
fun EmojiPicker(
    backgroundColor: Color = Color.Black,
    textColor: Color = Color.White,
    actionColor: Color = Color.DarkGray,
    actionTextColor: Color = Color.White,
    height: Float = 300f,
    categories: List<EmojiManager.Category>,
    onBackToKeyboard: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val currentCategory = categories.getOrNull(selectedTabIndex)

    // Paintを準備（再利用のためremember）
    val density = LocalDensity.current

    val paint = remember(density, textColor) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = with(density) { 26.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER

            // Int色に変換して設定
            color = android.graphics.Color.argb(
                (textColor.alpha * 255).toInt(),
                (textColor.red * 255).toInt(),
                (textColor.green * 255).toInt(),
                (textColor.blue * 255).toInt()
            )
        }
    }
    val columns = 8

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 上部カテゴリータブ
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = backgroundColor,
                edgePadding = 4.dp,
                indicator = {},
                divider = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEachIndexed { index, category ->
                    val isSelected = selectedTabIndex == index

                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier
                            .padding(top = 2.dp, start = 0.dp, end = 0.dp, bottom = 2.dp) // タブの上と横の隙間
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = if (isSelected) actionColor else Color.Transparent),
                        selectedContentColor = actionTextColor,
                        unselectedContentColor = textColor
                    ) {
                        Text(
                            text = category.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            fontFamily = AppFontFamily,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp) // タブ内の余白
                        )
                    }
                }
            }

            if (currentCategory != null) {
                // カテゴリー内の全絵文字を一覧表示するリスト
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 60.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    currentCategory.subcategories.forEach { subcategory ->

                        // サブカテゴリーの見出し
                        item(key = "header_${subcategory.name}") {
                            Text(
                                text = subcategory.name,
                                fontSize = 14.sp,
                                fontFamily = AppFontFamily,
                                color = textColor.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
                            )
                        }

                        val chunkedEmojis = subcategory.emojis.chunked(columns)

                        // 行ごとにCanvasを描画
                        items(chunkedEmojis) { rowEmojis ->
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .pointerInput(rowEmojis) {
                                        detectTapGestures { offset ->
                                            val widthPerItem = size.width / columns
                                            val index = (offset.x / widthPerItem).toInt()
                                            if (index < rowEmojis.size) {
                                                onEmojiSelected(rowEmojis[index].emoji)
                                            }
                                        }
                                    }
                            ) {
                                val widthPerItem = size.width / columns
                                val centerY = size.height / 2
                                val fontMetrics = paint.fontMetrics
                                val baselineY = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2

                                drawContext.canvas.nativeCanvas.apply {
                                    rowEmojis.forEachIndexed { index, emojiObj ->
                                        val centerX = widthPerItem * index + widthPerItem / 2
                                        drawText(
                                            emojiObj.emoji,
                                            centerX,
                                            baselineY,
                                            paint
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // 戻るボタン
        Button(
            onClick = onBackToKeyboard,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = actionColor)
        ) {
            Text(
                text = "ABC",
                fontFamily = AppFontFamily,
                color = actionTextColor
            )
        }
    }
}
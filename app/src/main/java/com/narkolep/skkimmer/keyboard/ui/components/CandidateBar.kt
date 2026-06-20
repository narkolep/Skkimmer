package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.narkolep.skkimmer.ui.theme.AppFontFamily

@Composable
fun CandidateBar(
    backgroundColor: Color = Color.Black,
    selectedBackgroundColor: Color = Color.DarkGray,
    selectedTextColor: Color = Color.LightGray,
    candidates: List<String>,
    selectedIndex: Int? = null,
    onCandidateClick: (Int) -> Unit
) {
    // スクロール状態を管理するStateを作成
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex) {
        selectedIndex?.let { index ->
            listState.animateScrollToItem(index)
        }
    }

    LazyRow(
        state = listState, // 作成したStateを紐付け
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(backgroundColor) // バー全体の背景色
            .padding(horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 候補を描画
        itemsIndexed(candidates) { index, candidate ->
            val isSelected = index == selectedIndex
            val textColor = if (isSelected) selectedTextColor else selectedTextColor.copy(alpha = 0.6f)

            // 注釈のパース処理
            val annotatedText = buildAnnotatedString {
                val parts = candidate.split(";", limit = 2)
                val mainText = parts[0]
                val comment = if (parts.size > 1) parts[1] else null

                // 候補本体
                append(mainText)

                // 注釈
                if (comment != null) {
                    withStyle(
                        style = SpanStyle(
                            color = textColor.copy(alpha = 0.4f), // 色を少し薄くする
                            fontSize = 14.sp, // サイズを小さく
                        )
                    ) {
                        append(" $comment") // セミコロンの代わりにスペースを入れて追加
                    }
                }
            }

            Text(
                text = annotatedText,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 20.sp,
                fontFamily = AppFontFamily,
                color = textColor,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .background(
                        color = if (isSelected) selectedBackgroundColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onCandidateClick(index) }
                    .padding(horizontal = 10.dp)
                    .wrapContentHeight(Alignment.CenterVertically)
            )
        }
    }
}
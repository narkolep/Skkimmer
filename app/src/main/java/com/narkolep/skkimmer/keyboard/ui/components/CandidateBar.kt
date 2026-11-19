package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import com.narkolep.skkimmer.data.Candidate

@Composable
fun CandidateBar(
    selectedBackgroundColor: Color = Color.DarkGray,
    selectedTextColor: Color = Color.LightGray,
    candidates: List<Candidate>,
    selectedIndex: Int? = null,
    keyHeight: Float,
    onCandidateClick: (Int) -> Unit
) {
    // スクロール状態を管理するStateを作成
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex, candidates.size) {
        if (selectedIndex != null && selectedIndex in candidates.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 候補を描画
        itemsIndexed(candidates) { index, candidate ->
            val isSelected = index == selectedIndex
            val textColor = if (isSelected) selectedTextColor else selectedTextColor.copy(alpha = 0.6f)

            val annotatedText = buildAnnotatedString {

                // 候補本体
                append(candidate.text.replace("\n", " "))

                // 注釈
                if (candidate.annotation != null) {
                    withStyle(
                        style = SpanStyle(
                            color = textColor.copy(alpha = 0.4f),
                            fontSize = (keyHeight * 0.25f).sp,
                        )
                    ) {
                        append(" ${candidate.annotation}")
                    }
                }
            }

            Text(
                text = annotatedText,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = (keyHeight * 0.35f).sp,
                color = textColor,
                modifier = Modifier
                    .fillMaxHeight()
                    .background(
                        color = if (isSelected) selectedBackgroundColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onCandidateClick(index) }
                    .padding(horizontal = (keyHeight * 0.2).dp)
                    .wrapContentHeight(Alignment.CenterVertically)
            )
        }
    }
}
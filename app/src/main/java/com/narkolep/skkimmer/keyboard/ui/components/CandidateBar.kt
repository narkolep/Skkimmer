package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.composables.icons.lucide.R
import com.narkolep.skkimmer.data.Candidate

@Composable
fun CandidateBar(
    selectedBackgroundColor: Color,
    selectedTextColor: Color,
    candidates: List<Candidate>,
    selectedIndex: Int? = null,
    keyHeight: Float,
    onCandidateClick: (Int) -> Unit,
    onToggleClick: () -> Unit,
) {
    // スクロール状態を管理するStateを作成
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex, candidates.size) {
        if (selectedIndex != null && selectedIndex in candidates.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .weight(9f)
                .fillMaxSize()
                .padding(vertical = 3.dp),
        ) {
            // 候補を描画
            itemsIndexed(candidates) { index, candidate ->
                val isSelected = index == selectedIndex
                val textColor =
                    if (isSelected) selectedTextColor else selectedTextColor.copy(alpha = 0.6f)

                val annotatedText = buildAnnotatedString {

                    // 候補本体
                    append(candidate.text.replace("\n", "\u21B5"))

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

        VerticalDivider(
            modifier = Modifier.padding(vertical = (keyHeight * 0.2).dp),
            color = selectedTextColor.copy(alpha = 0.4f),
        )

        IconButton(
            onClick = { onToggleClick() },
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_chevron_down),
                contentDescription = "Toggle candidate screen",
                tint = selectedTextColor,
                modifier = Modifier.size((keyHeight * 0.3f).dp)
            )
        }
    }
}
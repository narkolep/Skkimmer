package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.narkolep.skkimmer.data.Candidate

@Composable
fun CandidateScreen(
    backgroundColor: Color,
    textColor: Color,
    candidates: List<Candidate>,
    keyHeight: Float,
    onCandidateClick: (Int) -> Unit,
    onToggleClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // 候補を描画
                candidates.forEachIndexed { index, candidate ->
                    val annotatedText = buildAnnotatedString {

                        // 候補本体
                        append(candidate.text)

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
                        fontWeight = FontWeight.Normal,
                        fontSize = (keyHeight * 0.35f).sp,
                        color = textColor,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { onCandidateClick(index) }
                            .padding(
                                horizontal = (keyHeight * 0.2).dp,
                                vertical = (keyHeight * 0.1).dp,
                            )
                            .wrapContentHeight(Alignment.CenterVertically),
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((keyHeight * 0.6).dp)
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 8.dp)
                .background(
                    color = backgroundColor,
                    shape = CircleShape,
                ),
            onClick = { onToggleClick() },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_chevron_up),
                contentDescription = "Toggle candidate screen",
                tint = textColor,
            )
        }
    }
}
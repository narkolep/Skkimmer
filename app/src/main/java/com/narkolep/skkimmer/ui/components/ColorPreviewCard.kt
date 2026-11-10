package com.narkolep.skkimmer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.narkolep.skkimmer.keyboard.ui.theme.KeyboardColors
import kotlin.math.roundToInt

/**
 * キーボード外観のプレビューカード
 */
@Composable
fun ColorPreviewCard(
    colors: KeyboardColors,
    isBlurEnabled: Boolean,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "キーボード外観プレビュー",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (isBlurEnabled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "ぼかし有効 (${(alpha * 100).roundToInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                // 透明度を視覚化するための市松模様背景
                CheckerboardPattern(modifier = Modifier.matchParentSize())

                // キーボード背景
                val effectiveBg = colors.background.copy(alpha = if (isBlurEnabled) alpha else 1f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(effectiveBg)
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "背景",
                            color = colors.keyText.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )

                        // キーの配置
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 一般キー
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = colors.keyBackground
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "一般キー",
                                            color = colors.keyText,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            text = "A",
                                            color = colors.keyText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // ポップアップ
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = colors.flickKeyBackground
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "ポップアップ",
                                            color = colors.keyText,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            text = "あ",
                                            color = colors.keyText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // アクションキー
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = colors.specialKeyBackground
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "アクション",
                                            color = colors.specialKeyText,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            text = "Enter",
                                            color = colors.specialKeyText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 透明度を視覚化するための市松模様パターン
 */
@Composable
private fun CheckerboardPattern(modifier: Modifier = Modifier) {
    val light = MaterialTheme.colorScheme.surface
    val dark = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier) {
        val cellSize = 12.dp.toPx()
        val cols = kotlin.math.ceil(size.width / cellSize).toInt()
        val rows = kotlin.math.ceil(size.height / cellSize).toInt()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val topLeft = Offset(c * cellSize, r * cellSize)
                drawRect(
                    color = if ((r + c) % 2 == 0) light else dark,
                    topLeft = topLeft,
                    size = Size(
                        width = minOf(cellSize, size.width - topLeft.x),
                        height = minOf(cellSize, size.height - topLeft.y)
                    )
                )
            }
        }
    }
}

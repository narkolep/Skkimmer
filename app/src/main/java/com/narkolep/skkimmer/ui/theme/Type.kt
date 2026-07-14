package com.narkolep.skkimmer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

private val DefaultTypography = Typography()
val Typography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(
        fontFamily = AppFontFamily,
        fontSize = 60.sp,
        lineHeight = 62.sp
    ),
    displayMedium = DefaultTypography.displayMedium.copy(
        fontFamily = AppFontFamily,
        fontSize = 48.sp,
        lineHeight = 50.sp
    ),
    displaySmall = DefaultTypography.displaySmall.copy(
        fontFamily = AppFontFamily,
        fontSize = 39.sp,
        lineHeight = 42.sp
    ),
    headlineLarge = DefaultTypography.headlineLarge.copy(
        fontFamily = AppFontFamily,
        fontSize = 36.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = DefaultTypography.headlineMedium.copy(
        fontFamily = AppFontFamily,
        fontSize = 30.sp,
        lineHeight = 34.sp
    ),
    headlineSmall = DefaultTypography.headlineSmall.copy(
        fontFamily = AppFontFamily,
        fontSize = 28.sp,
        lineHeight = 30.sp
    ),
    titleLarge = DefaultTypography.titleLarge.copy(
        fontFamily = AppFontFamily,
        fontSize = 24.sp,
        lineHeight = 26.sp
    ),
    titleMedium = DefaultTypography.titleMedium.copy(
        fontFamily = AppFontFamily,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    titleSmall = DefaultTypography.titleSmall.copy(
        fontFamily = AppFontFamily,
        fontSize = 16.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = DefaultTypography.bodyLarge.copy(
        fontFamily = AppFontFamily,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = DefaultTypography.bodyMedium.copy(
        fontFamily = AppFontFamily,
        fontSize = 16.sp,
        lineHeight = 18.sp
    ),
    bodySmall = DefaultTypography.bodySmall.copy(
        fontFamily = AppFontFamily,
        fontSize = 14.sp,
        lineHeight = 15.sp
    ),
    labelLarge = DefaultTypography.labelLarge.copy(
        fontFamily = AppFontFamily,
        fontSize = 16.sp,
        lineHeight = 18.sp
    ),
    labelMedium = DefaultTypography.labelMedium.copy(
        fontFamily = AppFontFamily,
        fontSize = 14.sp,
        lineHeight = 15.sp
    ),
    labelSmall = DefaultTypography.labelSmall.copy(
        fontFamily = AppFontFamily,
        fontSize = 13.sp,
        lineHeight = 14.sp
    ),
)
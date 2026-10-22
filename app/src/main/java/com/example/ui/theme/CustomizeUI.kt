package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centralized design configuration for application themes, colors, dimensions, and typography.
 */
object CustomizeUI {

    // Action and Button Colors
    val ButtonMarkReturnedGreen: Color = Color(0xFF00BF63)
    val ButtonMarkReturnedText: Color = Color(0xFFFFFFFF)

    val ButtonPrimaryBlue: Color = Color(0xFF1800AC)
    val ButtonPrimaryText: Color = Color(0xFFFFFFFF)

    // Transaction Direction Colors
    val DirectionLentBlue: Color = Color(0xFF3059F5)
    val DirectionBorrowedOrange: Color = Color(0xFFFA6400)

    // Transaction Status Colors
    val StatusActiveGreen: Color = Color(0xFF0E7E45)
    val StatusOverdueRed: Color = Color(0xFFC61A1A)

    // Dialog and Form Dimensions
    val FormDialogMaxHeight: Dp = 710.dp
    val FormDialogCornerRadius: Dp = 14.dp
    val FormButtonHeight: Dp = 48.dp
    val FormHorizontalPadding: Dp = 14.dp
    val FormVerticalPadding: Dp = 28.dp
    val FormDetailTopPadding: Dp = 54.dp

    // Summary Metric Cards
    val MetricColorLent: Color = Color(0xFF3059F5)
    val MetricBgLent: Color = Color(0xFFDCE8FE)
    val MetricBorderLent: Color = Color(0xFF3059F5)

    val MetricColorBorrowed: Color = Color(0xFFFA6400)
    val MetricBgBorrowed: Color = Color(0xFFFFE3D1)
    val MetricBorderBorrowed: Color = Color(0xFFFA6400)

    val MetricColorOverdue: Color = Color(0xFFFF2828)
    val MetricBgOverdue: Color = Color(0xFFFFD2D2)
    val MetricBorderOverdue: Color = Color(0xFFFF2828)

    val MetricColorCompleted: Color = Color(0xFF00BF63)
    val MetricBgCompleted: Color = Color(0xFFCBF6E0)
    val MetricBorderCompleted: Color = Color(0xFF00BF63)

    val MetricCardHeight: Dp = 130.dp
    val MetricCardCornerRadius: Dp = 10.dp
    val TransactionCardCornerRadius: Dp = 10.dp
    val MetricCardIconToTextSpacing: Dp = 18.dp
    val QuickActionButtonHeight: Dp = 40.dp

    // Typography Settings
    const val FixedFontScale: Float = 1.0f
    val ScreenTitleFontSize: TextUnit = 21.sp
    val DialogTitleFontSize: TextUnit = 20.sp
    val ItemTitleFontSize: TextUnit = 16.sp
    val BodyFontSize: TextUnit = 13.sp
    val SmallFontSize: TextUnit = 11.sp
}

package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Pure Monochrome & Neutral Grayscale Palette
val MonoBlack = Color(0xFF000000)
val MonoCharcoal = Color(0xFF18181B)
val MonoDarkGray = Color(0xFF27272A)
val MonoGrayMedium = Color(0xFF71717A)
val MonoGrayLight = Color(0xFFA1A1AA)
val MonoBorder = Color(0xFFE4E4E7)
val MonoSurfaceVariant = Color(0xFFF4F4F5)
val MonoBackgroundLight = Color(0xFFFAFAFA)
val MonoWhite = Color(0xFFFFFFFF)

// Backward compatible aliases mapped to monochrome tokens
val HabitGreenPrimary = MonoBlack
val HabitGreenDark = MonoCharcoal
val HabitGreenLight = MonoDarkGray
val HabitGreenContainer = MonoSurfaceVariant
val HabitOnGreenContainer = MonoBlack

val HabitBackgroundLight = MonoBackgroundLight
val HabitSurfaceLight = MonoWhite
val HabitSurfaceVariant = MonoSurfaceVariant
val HabitOutline = MonoBorder
val HabitOutlineVariant = Color(0xFFEBEBEF)

val HabitTextPrimary = MonoBlack
val HabitTextSecondary = MonoGrayMedium
val HabitTextTertiary = MonoGrayLight

// Monochrome Category Styling
val CatNeutralBg = Color(0xFFF4F4F5)
val CatNeutralText = Color(0xFF18181B)
val CatNeutralBorder = Color(0xFFE4E4E7)

val CatHealthBg = CatNeutralBg
val CatHealthText = CatNeutralText
val CatProductivityBg = CatNeutralBg
val CatProductivityText = CatNeutralText
val CatLearningBg = CatNeutralBg
val CatLearningText = CatNeutralText
val CatLifestyleBg = CatNeutralBg
val CatLifestyleText = CatNeutralText

// Monochromatic Heatmap Levels
val Heatmap0 = Color(0xFFEAEAEA) // Inactive/0
val Heatmap1 = Color(0xFFAFAFAF) // Low
val Heatmap2 = Color(0xFF555555) // Medium
val Heatmap3 = Color(0xFF000000) // High

// Dark Mode Palette
val HabitGreenPrimaryDark = MonoWhite
val HabitBackgroundDark = Color(0xFF09090B)
val HabitSurfaceDark = Color(0xFF141416)
val HabitSurfaceVariantDark = Color(0xFF202024)
val HabitTextPrimaryDark = Color(0xFFFAFAFA)
val HabitTextSecondaryDark = Color(0xFFA1A1AA)

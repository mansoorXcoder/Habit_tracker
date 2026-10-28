package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// High-Contrast Monochrome & Neutral Grayscale Palette (WCAG AA/AAA Compliant)
val MonoBlack = Color(0xFF09090B)
val MonoCharcoal = Color(0xFF18181B)
val MonoDarkGray = Color(0xFF27272A)
val MonoGrayMedium = Color(0xFF52525B)      // Improved contrast for subtitles/secondary text
val MonoGrayLight = Color(0xFF71717A)       // Improved contrast for captions/hints
val MonoBorder = Color(0xFFD4D4D8)          // Crisp, visible 1dp border outline
val MonoBorderStrong = Color(0xFFA1A1AA)    // Stronger outline for interactive controls
val MonoSurfaceVariant = Color(0xFFF4F4F5)
val MonoBackgroundLight = Color(0xFFFAFAFA)
val MonoWhite = Color(0xFFFFFFFF)

// Semantic Accent Colors for Lives, Streak, and Feedback
val AccentSuccess = Color(0xFF16A34A)
val AccentSuccessBg = Color(0xFFDCFCE7)
val AccentError = Color(0xFFDC2626)
val AccentErrorBg = Color(0xFFFEE2E2)
val AccentHeart = Color(0xFFE11D48)
val AccentHeartBg = Color(0xFFFFE4E6)

// Backward compatible aliases mapped to high-contrast monochrome tokens
val HabitGreenPrimary = MonoBlack
val HabitGreenDark = MonoCharcoal
val HabitGreenLight = MonoDarkGray
val HabitGreenContainer = MonoSurfaceVariant
val HabitOnGreenContainer = MonoBlack

val HabitBackgroundLight = MonoBackgroundLight
val HabitSurfaceLight = MonoWhite
val HabitSurfaceVariant = MonoSurfaceVariant
val HabitOutline = MonoBorder
val HabitOutlineVariant = Color(0xFFE4E4E7)

val HabitTextPrimary = MonoBlack
val HabitTextSecondary = MonoGrayMedium
val HabitTextTertiary = MonoGrayLight

// Monochrome Category Styling
val CatNeutralBg = Color(0xFFF4F4F5)
val CatNeutralText = Color(0xFF18181B)
val CatNeutralBorder = Color(0xFFD4D4D8)

val CatHealthBg = CatNeutralBg
val CatHealthText = CatNeutralText
val CatProductivityBg = CatNeutralBg
val CatProductivityText = CatNeutralText
val CatLearningBg = CatNeutralBg
val CatLearningText = CatNeutralText
val CatLifestyleBg = CatNeutralBg
val CatLifestyleText = CatNeutralText

// 5 Consistent Heatmap Intensity Levels (0%, 1-39%, 40-69%, 70-99% Streak, 100% Perfect)
val Heatmap0 = Color(0xFFECECEF) // 0% - None
val Heatmap1 = Color(0xFFC4C4CC) // 1-39% - Low
val Heatmap2 = Color(0xFF8A8A96) // 40-69% - Moderate
val Heatmap3 = Color(0xFF3F3F46) // 70-99% - Streak Qualified
val Heatmap4 = Color(0xFF09090B) // 100% - Perfect Day

// Dark Mode Palette
val HabitGreenPrimaryDark = MonoWhite
val HabitBackgroundDark = Color(0xFF09090B)
val HabitSurfaceDark = Color(0xFF141416)
val HabitSurfaceVariantDark = Color(0xFF202024)
val HabitTextPrimaryDark = Color(0xFFFAFAFA)
val HabitTextSecondaryDark = Color(0xFFA1A1AA)

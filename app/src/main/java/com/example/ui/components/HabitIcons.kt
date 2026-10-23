package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CatHealthBg
import com.example.ui.theme.CatHealthText
import com.example.ui.theme.CatLearningBg
import com.example.ui.theme.CatLearningText
import com.example.ui.theme.CatLifestyleBg
import com.example.ui.theme.CatLifestyleText
import com.example.ui.theme.CatProductivityBg
import com.example.ui.theme.CatProductivityText
import com.example.ui.theme.HabitGreenContainer
import com.example.ui.theme.HabitGreenPrimary

object HabitIcons {

    fun getIconVector(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "code" -> Icons.Default.Code
            "book", "read" -> Icons.Default.MenuBook
            "fitness", "workout" -> Icons.Default.FitnessCenter
            "water", "drink" -> Icons.Default.WaterDrop
            "moon", "sleep" -> Icons.Default.Bedtime
            "run", "walk" -> Icons.Default.DirectionsRun
            "meditation" -> Icons.Default.SelfImprovement
            "music" -> Icons.Default.MusicNote
            else -> Icons.Default.Spa
        }
    }

    fun getCategoryColors(category: String): Pair<Color, Color> {
        return when (category.lowercase()) {
            "health" -> Pair(CatHealthBg, CatHealthText)
            "productivity" -> Pair(CatProductivityBg, CatProductivityText)
            "learning" -> Pair(CatLearningBg, CatLearningText)
            "lifestyle" -> Pair(CatLifestyleBg, CatLifestyleText)
            else -> Pair(HabitGreenContainer, HabitGreenPrimary)
        }
    }
}

@Composable
fun HabitIconBadge(
    iconName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    backgroundColor: Color = HabitGreenContainer,
    iconTint: Color = HabitGreenPrimary,
    isRoundedRect: Boolean = false
) {
    val shape = if (isRoundedRect) RoundedCornerShape(14.dp) else CircleShape

    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = HabitIcons.getIconVector(iconName),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )
    }
}

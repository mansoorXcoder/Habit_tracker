package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CatNeutralBg
import com.example.ui.theme.CatNeutralText
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoSurfaceVariant

data class IconCategoryGroup(
    val categoryName: String,
    val iconKeys: List<String>
)

object HabitIcons {

    val iconGroups = listOf(
        IconCategoryGroup(
            "Fitness",
            listOf("fitness", "run", "walk", "bike", "pool", "sports", "timer", "fire")
        ),
        IconCategoryGroup(
            "Health",
            listOf("water", "heart", "moon", "pharmacy", "restaurant", "spa", "clean", "sun")
        ),
        IconCategoryGroup(
            "Productivity",
            listOf("code", "laptop", "work", "task", "rocket", "lightbulb", "alarm", "flag")
        ),
        IconCategoryGroup(
            "Learning",
            listOf("book", "school", "language", "science", "psychology", "calculate", "edit", "library")
        ),
        IconCategoryGroup(
            "Mindfulness",
            listOf("meditation", "park", "nature", "air", "peace", "music", "draw", "night")
        ),
        IconCategoryGroup(
            "Lifestyle",
            listOf("coffee", "pets", "savings", "flight", "home", "brush", "celebration", "star")
        )
    )

    fun getIconVector(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            // Fitness
            "fitness", "workout" -> Icons.Default.FitnessCenter
            "run" -> Icons.Default.DirectionsRun
            "walk" -> Icons.Default.DirectionsWalk
            "bike" -> Icons.Default.DirectionsBike
            "pool", "swim" -> Icons.Default.Pool
            "sports" -> Icons.Default.SportsGymnastics
            "timer" -> Icons.Default.Timer
            "fire" -> Icons.Default.LocalFireDepartment

            // Health
            "water", "drink" -> Icons.Default.WaterDrop
            "heart" -> Icons.Default.Favorite
            "moon", "sleep" -> Icons.Default.Bedtime
            "pharmacy" -> Icons.Default.LocalPharmacy
            "restaurant", "food" -> Icons.Default.Restaurant
            "spa" -> Icons.Default.Spa
            "clean" -> Icons.Default.CleanHands
            "sun" -> Icons.Default.WbSunny

            // Productivity
            "code" -> Icons.Default.Code
            "laptop", "computer" -> Icons.Default.Computer
            "work" -> Icons.Default.Work
            "task" -> Icons.Default.Checklist
            "rocket" -> Icons.Default.RocketLaunch
            "lightbulb" -> Icons.Default.Lightbulb
            "alarm" -> Icons.Default.AccessTime
            "flag" -> Icons.Default.Flag

            // Learning
            "book", "read" -> Icons.Default.MenuBook
            "school" -> Icons.Default.School
            "language" -> Icons.Default.Language
            "science" -> Icons.Default.Science
            "psychology" -> Icons.Default.Psychology
            "calculate" -> Icons.Default.Calculate
            "edit" -> Icons.Default.Edit
            "library" -> Icons.Default.LocalLibrary

            // Mindfulness
            "meditation" -> Icons.Default.SelfImprovement
            "park" -> Icons.Default.Park
            "nature" -> Icons.Default.NaturePeople
            "air" -> Icons.Default.Air
            "peace" -> Icons.Default.Forest
            "music" -> Icons.Default.MusicNote
            "draw" -> Icons.Default.Palette
            "night" -> Icons.Default.NightsStay

            // Lifestyle
            "coffee" -> Icons.Default.LocalCafe
            "pets" -> Icons.Default.Pets
            "savings" -> Icons.Default.Savings
            "flight" -> Icons.Default.Flight
            "home" -> Icons.Default.Home
            "brush" -> Icons.Default.Brush
            "celebration" -> Icons.Default.Celebration
            "star" -> Icons.Default.Star

            else -> Icons.Default.Checklist
        }
    }

    // Always returns monochrome styling
    fun getCategoryColors(category: String): Pair<Color, Color> {
        return Pair(CatNeutralBg, CatNeutralText)
    }
}

@Composable
fun HabitIconBadge(
    iconName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp,
    backgroundColor: Color = MonoSurfaceVariant,
    iconTint: Color = MonoBlack,
    isRoundedRect: Boolean = false,
    isSelected: Boolean = false
) {
    val shape = if (isRoundedRect) RoundedCornerShape(14.dp) else CircleShape
    val actualBg = if (isSelected) MonoBlack else backgroundColor
    val actualTint = if (isSelected) Color.White else iconTint

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(actualBg)
            .border(BorderStroke(1.dp, MonoBorder), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = HabitIcons.getIconVector(iconName),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = actualTint
        )
    }
}

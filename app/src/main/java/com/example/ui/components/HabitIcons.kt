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
import com.example.ui.theme.MonoWhite

data class IconCategoryGroup(
    val categoryName: String,
    val defaultIconKey: String,
    val iconKeys: List<String>
)

object HabitIcons {

    val iconGroups = listOf(
        IconCategoryGroup(
            categoryName = "Productivity",
            defaultIconKey = "code",
            iconKeys = listOf("code", "laptop", "work", "task", "rocket", "lightbulb", "alarm", "flag")
        ),
        IconCategoryGroup(
            categoryName = "Health",
            defaultIconKey = "water",
            iconKeys = listOf("water", "heart", "moon", "pharmacy", "restaurant", "spa", "clean", "sun")
        ),
        IconCategoryGroup(
            categoryName = "Learning",
            defaultIconKey = "book",
            iconKeys = listOf("book", "school", "language", "science", "psychology", "calculate", "edit", "library")
        ),
        IconCategoryGroup(
            categoryName = "Lifestyle",
            defaultIconKey = "coffee",
            iconKeys = listOf("coffee", "pets", "savings", "flight", "home", "brush", "celebration", "star")
        ),
        IconCategoryGroup(
            categoryName = "Fitness",
            defaultIconKey = "fitness",
            iconKeys = listOf("fitness", "run", "walk", "bike", "pool", "sports", "timer", "fire")
        ),
        IconCategoryGroup(
            categoryName = "Mindfulness",
            defaultIconKey = "meditation",
            iconKeys = listOf("meditation", "park", "nature", "air", "peace", "music", "draw", "night")
        )
    )

    val allIconKeys: List<String> = iconGroups.flatMap { it.iconKeys }.distinct()

    fun getGroupIndexForCategory(categoryName: String): Int {
        val idx = iconGroups.indexOfFirst { it.categoryName.equals(categoryName.trim(), ignoreCase = true) }
        return if (idx >= 0) idx else 0
    }

    fun getDefaultIconForCategory(categoryName: String): String {
        val group = iconGroups.find { it.categoryName.equals(categoryName.trim(), ignoreCase = true) }
        return group?.defaultIconKey ?: "task"
    }

    fun getIconVector(iconName: String): ImageVector {
        return when (iconName.trim().lowercase()) {
            // Fitness
            "fitness", "workout", "gym" -> Icons.Default.FitnessCenter
            "run", "jog" -> Icons.Default.DirectionsRun
            "walk", "steps" -> Icons.Default.DirectionsWalk
            "bike", "cycle" -> Icons.Default.DirectionsBike
            "pool", "swim" -> Icons.Default.Pool
            "sports", "yoga" -> Icons.Default.SportsGymnastics
            "timer", "stopwatch" -> Icons.Default.Timer
            "fire", "streak" -> Icons.Default.LocalFireDepartment

            // Health
            "water", "drink", "hydrate" -> Icons.Default.WaterDrop
            "heart", "health" -> Icons.Default.Favorite
            "moon", "sleep", "bed" -> Icons.Default.Bedtime
            "pharmacy", "vitamins", "medicine" -> Icons.Default.LocalPharmacy
            "restaurant", "food", "diet", "nutrition" -> Icons.Default.Restaurant
            "spa", "relax" -> Icons.Default.Spa
            "clean", "hygiene" -> Icons.Default.CleanHands
            "sun", "morning" -> Icons.Default.WbSunny

            // Productivity
            "code", "programming", "dev" -> Icons.Default.Code
            "laptop", "computer" -> Icons.Default.Computer
            "work", "career", "productivity" -> Icons.Default.Work
            "task", "checklist", "todo" -> Icons.Default.Checklist
            "rocket", "launch", "focus" -> Icons.Default.RocketLaunch
            "lightbulb", "idea" -> Icons.Default.Lightbulb
            "alarm", "time" -> Icons.Default.AccessTime
            "flag", "goal" -> Icons.Default.Flag

            // Learning
            "book", "read", "reading" -> Icons.Default.MenuBook
            "school", "learning", "study" -> Icons.Default.School
            "language", "speak" -> Icons.Default.Language
            "science", "lab" -> Icons.Default.Science
            "psychology", "brain", "mind" -> Icons.Default.Psychology
            "calculate", "math" -> Icons.Default.Calculate
            "edit", "write", "journal" -> Icons.Default.Edit
            "library", "courses" -> Icons.Default.LocalLibrary

            // Mindfulness
            "meditation", "mindfulness", "breathe" -> Icons.Default.SelfImprovement
            "park", "outdoors" -> Icons.Default.Park
            "nature", "walk_nature" -> Icons.Default.NaturePeople
            "air", "breath" -> Icons.Default.Air
            "peace", "forest" -> Icons.Default.Forest
            "music", "instrument" -> Icons.Default.MusicNote
            "draw", "art", "paint" -> Icons.Default.Palette
            "night", "evening" -> Icons.Default.NightsStay

            // Lifestyle
            "coffee", "tea" -> Icons.Default.LocalCafe
            "pets", "dog", "cat" -> Icons.Default.Pets
            "savings", "finance", "money" -> Icons.Default.Savings
            "flight", "travel" -> Icons.Default.Flight
            "home", "chores", "lifestyle" -> Icons.Default.Home
            "brush", "creative" -> Icons.Default.Brush
            "celebration", "social" -> Icons.Default.Celebration
            "star", "favorite" -> Icons.Default.Star

            else -> Icons.Default.Checklist
        }
    }

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
    val actualTint = if (isSelected) MonoWhite else iconTint
    val borderColor = if (isSelected) MonoBlack else MonoBorder

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(actualBg)
            .border(BorderStroke(1.dp, borderColor), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = HabitIcons.getIconVector(iconName),
            contentDescription = iconName,
            modifier = Modifier.size(iconSize),
            tint = actualTint
        )
    }
}

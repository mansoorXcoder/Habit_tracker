package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.ui.components.CustomTimePickerDialog
import com.example.ui.theme.AccentHeart
import com.example.ui.theme.AccentHeartBg
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoGrayLight
import com.example.ui.theme.MonoGrayMedium
import com.example.ui.theme.MonoSurfaceVariant
import com.example.ui.theme.MonoWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    userName: String,
    userProfile: UserProfileEntity = UserProfileEntity(),
    hasNotificationPermission: Boolean = true,
    onRequestNotificationPermission: () -> Unit = {},
    onUpdateUserName: (String) -> Unit,
    onUpdateMorningSettings: (Boolean, String, String) -> Unit = { _, _, _ -> },
    onSendTestNotification: () -> Unit = {},
    onSyncAccount: (String, String) -> Unit = { _, _ -> },
    onExportBackup: ((String) -> Unit) -> Unit = {},
    onImportBackup: (String) -> Unit = {},
    onResetAllData: () -> Unit,
    currentStreak: Int,
    completionRate: Int,
    totalHabits: Int,
    livesCount: Int = userProfile.livesCount,
    perfectDaysCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember(userName) { mutableStateOf(userName) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Morning settings states
    var morningEnabled by remember(userProfile.morningReminderEnabled) {
        mutableStateOf(userProfile.morningReminderEnabled)
    }
    var morningTime by remember(userProfile.morningReminderTime) {
        mutableStateOf(userProfile.morningReminderTime)
    }
    var morningMessage by remember(userProfile.customMorningMessage) {
        mutableStateOf(userProfile.customMorningMessage)
    }
    var showMorningTimePicker by remember { mutableStateOf(false) }

    // Cloud Sync & Backup states
    var accountEmail by remember(userProfile.userEmail) { mutableStateOf(userProfile.userEmail) }
    var syncKey by remember(userProfile.syncKey) { mutableStateOf(userProfile.syncKey) }
    var exportedJsonText by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWideScreen = maxWidth >= 680.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = if (isWideScreen) 840.dp else 600.dp)
                .padding(horizontal = if (isWideScreen) 28.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Profile & Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonoBlack
                )
            }

            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MonoBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MonoWhite,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MonoBlack
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = MonoGrayMedium,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            tempName = userName
                                            showEditNameDialog = true
                                        }
                                        .testTag("edit_profile_name_button")
                                )
                            }
                            Text(
                                text = if (userProfile.userEmail.isNotBlank()) {
                                    "Synced · ${userProfile.userEmail}"
                                } else {
                                    "Daily Habit Builder"
                                },
                                fontSize = 12.sp,
                                color = MonoGrayMedium
                            )
                        }

                        // Recovery Lives Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(AccentHeartBg)
                                .border(BorderStroke(1.dp, AccentHeart.copy(alpha = 0.35f)), RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("profile_lives_badge"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = AccentHeart,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$livesCount Lives",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonoBlack
                            )
                        }
                    }
                }
            }

            // Highlights Card (Dynamic stats)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(value = "$currentStreak d", label = "70% Streak", modifier = Modifier.weight(1f))
                    StatCard(value = "$perfectDaysCount", label = "100% Days", modifier = Modifier.weight(1f))
                    StatCard(value = "$completionRate%", label = "Completion", modifier = Modifier.weight(1f))
                    StatCard(value = "$totalHabits", label = "Routines", modifier = Modifier.weight(1f))
                }
            }

            // Morning Messages & Daily Notification Settings (§7)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("morning_settings_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MonoBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Morning Motivation & Reminder",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MonoBlack
                                    )
                                    Text(
                                        text = "Personalized daily morning notification",
                                        fontSize = 11.sp,
                                        color = MonoGrayMedium
                                    )
                                }
                            }

                            Switch(
                                checked = morningEnabled,
                                onCheckedChange = { checked ->
                                    morningEnabled = checked
                                    onUpdateMorningSettings(checked, morningTime, morningMessage)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MonoWhite,
                                    checkedTrackColor = MonoBlack,
                                    uncheckedThumbColor = MonoGrayLight,
                                    uncheckedTrackColor = MonoBorder
                                ),
                                modifier = Modifier.testTag("morning_reminder_switch")
                            )
                        }

                        if (!hasNotificationPermission) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MonoSurfaceVariant)
                                    .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "System notifications are turned off. In-app morning banner remains active.",
                                    fontSize = 11.sp,
                                    color = MonoGrayMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = onRequestNotificationPermission) {
                                    Text("Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MonoBlack)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Configurable Morning Reminder Time
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MonoSurfaceVariant)
                                .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(12.dp))
                                .clickable { showMorningTimePicker = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("morning_time_selector"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MonoBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Morning Notification Time",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MonoBlack
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = morningTime,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MonoBlack
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Change time",
                                    tint = MonoGrayMedium,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = morningMessage,
                            onValueChange = { morningMessage = it },
                            label = { Text("Custom Morning Message", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("morning_message_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MonoBlack,
                                unfocusedBorderColor = MonoBorder,
                                focusedTextColor = MonoBlack,
                                unfocusedTextColor = MonoBlack
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onUpdateMorningSettings(morningEnabled, morningTime, morningMessage)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_morning_settings_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
                            ) {
                                Text("Save Reminder", color = MonoWhite, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onSendTestNotification,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_morning_notification_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MonoBorder)
                            ) {
                                Text("Test Alert", color = MonoBlack, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Data Storage, Cross-Device Account Sync & Backup Recovery Card (§1)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cloud_backup_recovery_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = MonoBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Account Sync & Data Recovery",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MonoBlack
                                )
                                Text(
                                    text = "Keep habits, streaks & history safe across reinstalls and devices",
                                    fontSize = 11.sp,
                                    color = MonoGrayMedium
                                )
                            }
                        }

                        if (userProfile.lastSyncedAt > 0L) {
                            val syncFormatted = SimpleDateFormat("MMM d, h:mm a", Locale.US)
                                .format(Date(userProfile.lastSyncedAt))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Last synced: $syncFormatted",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MonoBlack
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = accountEmail,
                            onValueChange = { accountEmail = it },
                            label = { Text("Account Email", fontSize = 11.sp) },
                            placeholder = { Text("you@example.com", fontSize = 12.sp, color = MonoGrayLight) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sync_email_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MonoBlack,
                                unfocusedBorderColor = MonoBorder,
                                focusedTextColor = MonoBlack,
                                unfocusedTextColor = MonoBlack
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = syncKey,
                            onValueChange = { syncKey = it },
                            label = { Text("Recovery Passkey (optional)", fontSize = 11.sp) },
                            placeholder = { Text("Enter secret sync key", fontSize = 12.sp, color = MonoGrayLight) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sync_key_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MonoBlack,
                                unfocusedBorderColor = MonoBorder,
                                focusedTextColor = MonoBlack,
                                unfocusedTextColor = MonoBlack
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onSyncAccount(accountEmail, syncKey) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sync_account_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
                        ) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync & Recover Account Data", color = MonoWhite, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onExportBackup { json ->
                                        exportedJsonText = json
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_backup_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MonoBorder)
                            ) {
                                Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, tint = MonoBlack, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export Backup", color = MonoBlack, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    importJsonInput = ""
                                    showImportDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("import_backup_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MonoBorder)
                            ) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = MonoBlack, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore Snapshot", color = MonoBlack, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Reset Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Data Management",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MonoBlack
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MonoSurfaceVariant)
                                .border(BorderStroke(1.dp, MonoBorder), RoundedCornerShape(14.dp))
                                .clickable { showResetConfirmDialog = true }
                                .padding(16.dp)
                                .testTag("reset_all_data_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Reset All Data",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Clear all habits, streaks, and history to a clean slate",
                                        fontSize = 11.sp,
                                        color = MonoGrayMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // About Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoWhite),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MonoBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HabitFlow",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MonoBlack
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Small Habits · Big Changes\n70% daily threshold builds your streak · 7 consecutive days awards +1 recovery life.",
                            fontSize = 12.sp,
                            color = MonoGrayMedium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showMorningTimePicker) {
        CustomTimePickerDialog(
            initialTime = morningTime,
            onDismiss = { showMorningTimePicker = false },
            onTimeSelected = { newTime ->
                morningTime = newTime
                showMorningTimePicker = false
                onUpdateMorningSettings(morningEnabled, newTime, morningMessage)
            }
        )
    }

    // Exported Backup Snapshot Dialog
    if (exportedJsonText != null) {
        AlertDialog(
            onDismissRequest = { exportedJsonText = null },
            title = { Text("Backup Snapshot Ready", fontWeight = FontWeight.Bold, color = MonoBlack) },
            text = {
                Column {
                    Text(
                        "Copy this recovery snapshot to restore your habits, streaks, and settings on any device:",
                        fontSize = 12.sp,
                        color = MonoGrayMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText ?: "",
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { exportedJsonText = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
                ) {
                    Text("Done", color = MonoWhite)
                }
            }
        )
    }

    // Restore Backup Snapshot Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restore Backup Snapshot", fontWeight = FontWeight.Bold, color = MonoBlack) },
            text = {
                Column {
                    Text(
                        "Paste your HabitFlow backup JSON below to restore your habits, history, streaks, and settings:",
                        fontSize = 12.sp,
                        color = MonoGrayMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        placeholder = { Text("{\"version\":3,...}") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonInput.isNotBlank()) {
                            onImportBackup(importJsonInput.trim())
                            showImportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
                ) {
                    Text("Restore Now", color = MonoWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = MonoBlack)
                }
            }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Display Name", fontWeight = FontWeight.Bold, color = MonoBlack) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    placeholder = { Text("Enter your name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonoBlack,
                        unfocusedBorderColor = MonoBorder,
                        focusedTextColor = MonoBlack,
                        unfocusedTextColor = MonoBlack
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            onUpdateUserName(tempName.trim())
                        }
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
                ) {
                    Text("Save", color = MonoWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = MonoBlack)
                }
            }
        )
    }

    // Reset All Data Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset All Data?", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
            text = {
                Text(
                    "This action will permanently delete all created habits, completion history, streaks, and custom categories. The app will return to a clean blank slate with 10 recovery lives and default categories.\n\nThis cannot be undone.",
                    fontSize = 13.sp,
                    color = MonoBlack
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reset Everything", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = MonoBlack)
                }
            }
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MonoWhite),
        border = BorderStroke(1.dp, MonoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MonoBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MonoGrayMedium
            )
        }
    }
}

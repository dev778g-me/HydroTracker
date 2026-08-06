package com.dev.hydrotracker.presentation.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.hydrotracker.data.models.UserProfile
import com.dev.hydrotracker.notifications.NotificationPermissionManager
import com.dev.hydrotracker.notifications.HydroNotificationScheduler
import kotlinx.coroutines.launch

/**
 * Notification Settings Section for the Settings Screen
 */
@Composable
fun NotificationSettingsSection(
    userProfile: UserProfile?,
    onRequestPermission: () -> Unit,
    isVisible: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(NotificationPermissionManager.hasNotificationPermission(context))
    }

    var hasExactAlarmPermission by remember {
        mutableStateOf(NotificationPermissionManager.hasExactAlarmPermission(context))
    }

    var isNotificationsEnabled by remember {
        mutableStateOf(
            hasPermission && hasExactAlarmPermission && userProfile?.isOnboardingCompleted == true
        )
    }

    var refreshTrigger by remember { mutableIntStateOf(0) }

    val allPermissionsGranted = hasPermission && hasExactAlarmPermission

    // Function to refresh permission status
    val refreshPermissions = {
        hasPermission = NotificationPermissionManager.hasNotificationPermission(context)
        hasExactAlarmPermission = NotificationPermissionManager.hasExactAlarmPermission(context)
        isNotificationsEnabled =
            hasPermission && hasExactAlarmPermission && userProfile?.isOnboardingCompleted == true
    }

    // Update states when userProfile changes or refresh is triggered
    LaunchedEffect(userProfile, refreshTrigger) {
        refreshPermissions()
    }

    // Listen for when app regains focus to refresh permissions
    androidx.compose.runtime.DisposableEffect(Unit) {
        val activity = context as? androidx.activity.ComponentActivity
        val listener = object : android.app.Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: android.app.Activity) {
                if (activity == context) {
                    refreshTrigger++
                }
            }
            override fun onActivityPaused(activity: android.app.Activity) {}
            override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {}
            override fun onActivityStarted(activity: android.app.Activity) {}
            override fun onActivityStopped(activity: android.app.Activity) {}
            override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
            override fun onActivityDestroyed(activity: android.app.Activity) {}
        }

        activity?.application?.registerActivityLifecycleCallbacks(listener)
        onDispose {
            activity?.application?.unregisterActivityLifecycleCallbacks(listener)
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 2 }
        ) + fadeIn(animationSpec = tween(600, delayMillis = 300))
    ) {
        SettingsSectionCard {
            // Header


            // Main toggle
            val toggleReminders: (Boolean) -> Unit = { enabled ->
                isNotificationsEnabled = enabled
                if (enabled) {
                    if (!allPermissionsGranted) {
                        onRequestPermission()
                        NotificationPermissionManager.requestExactAlarmPermission(context)
                    }
                    if (userProfile != null) {
                        coroutineScope.launch {
                            HydroNotificationScheduler.startNotifications(context, userProfile)
                        }
                    }
                } else {
                    coroutineScope.launch {
                        HydroNotificationScheduler.stopNotifications(context)
                    }
                }
            }

            SettingsJoinedBlock(
                isFirst = true,
                isLast = !isNotificationsEnabled,
                checked = isNotificationsEnabled,
                enabled = userProfile?.isOnboardingCompleted == true,
                onCheckedChange = toggleReminders
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hydration Reminders",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (userProfile != null) {
                                "Get reminded every ${userProfile.reminderInterval} minutes"
                            } else {
                                "Complete onboarding to enable reminders"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isNotificationsEnabled,
                        enabled = userProfile?.isOnboardingCompleted == true,
                        onCheckedChange = toggleReminders,
                        thumbContent = if (isNotificationsEnabled) {
                            {
                                Icon(
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }

            if (isNotificationsEnabled) {
                SettingsJoinedBlock(isFirst = false, isLast = true) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (allPermissionsGranted && userProfile != null) {
                            NotificationDetailRow(
                                icon = Icons.Default.Schedule,
                                label = "Reminder Frequency",
                                value = "Every ${userProfile.reminderInterval} minutes"
                            )

                            NotificationDetailRow(
                                icon = Icons.Default.WbSunny,
                                label = "Active Hours",
                                value = "${userProfile.wakeUpTime} - ${userProfile.sleepTime}"
                            )

                            NotificationDetailRow(
                                icon = Icons.Default.Style,
                                label = "Reminder Style",
                                value = userProfile.reminderStyle.getDisplayName()
                            )

                            // Next notification info
                            val nextNotificationTime = remember(userProfile, isNotificationsEnabled) {
                                if (isNotificationsEnabled) {
                                    HydroNotificationScheduler.getNextScheduledTime(context, userProfile)
                                } else {
                                    null
                                }
                            }

                            if (nextNotificationTime != null) {
                                NotificationDetailRow(
                                    icon = Icons.Default.Schedule,
                                    label = "Next Reminder",
                                    value = nextNotificationTime
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Grant notification permission to enable reminders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

package com.dev.hydrotracker.presentation.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.dev.hydrotracker.data.models.ThemePreferences
import com.dev.hydrotracker.data.models.DarkModePreference
import com.dev.hydrotracker.data.models.ColorSource
import com.dev.hydrotracker.data.models.WeekStartDay
import com.dev.hydrotracker.data.models.AppFont
import com.dev.hydrotracker.data.models.UserProfile
import com.dev.hydrotracker.data.models.HydrationStandard
import com.dev.hydrotracker.data.repository.UserRepository
import com.dev.hydrotracker.data.database.repository.WaterIntakeRepository
import com.dev.hydrotracker.data.database.repository.ContainerPresetRepository
import com.dev.hydrotracker.presentation.common.HydroSnackbarHost
import com.dev.hydrotracker.presentation.common.showSuccessSnackbar
import com.dev.hydrotracker.ui.theme.HydroTrackerTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.content.Intent
import androidx.compose.ui.res.painterResource
import com.dev.hydrotracker.R
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import com.dev.hydrotracker.BuildConfig
import com.dev.hydrotracker.health.HealthConnectManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    themePreferences: ThemePreferences = ThemePreferences(),
    userProfile: UserProfile? = null,
    userRepository: UserRepository? = null,
    waterIntakeRepository: WaterIntakeRepository? = null,
    containerPresetRepository: ContainerPresetRepository? = null,
    onDarkModeChange: (DarkModePreference) -> Unit = {},
    onColorSourceChange: (ColorSource) -> Unit = {},
    onPureBlackChange: (Boolean) -> Unit = {},
    onWeekStartDayChange: (WeekStartDay) -> Unit = {},
    onAppFontChange: (AppFont) -> Unit = {},
    onHydrationStandardChange: (HydrationStandard) -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {},
    healthConnectPermissionLauncher: ActivityResultLauncher<Set<String>>? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToBeverageTypes: () -> Unit = {},
    onNavigateToWidgetCustomization: () -> Unit = {},
    onNavigateToHealthConnectData: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    isDynamicColorAvailable: Boolean = true
) {
    // Animation states
    var isVisible by remember { mutableStateOf(false) }

    // Developer options state
    var developerOptionsEnabled by remember {
        mutableStateOf(
            userRepository?.loadDeveloperOptionsEnabled() ?: false
        )
    }
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    // Snackbar state for Material 3 Expressive feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { HydroSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCategoryHeader(title = "Appearance")

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialOffsetY = { -it / 3 }
                    ) + fadeIn(animationSpec = tween(600))
                ) {
                    ThemeSection(
                        themePreferences = themePreferences,
                        onColorSourceChange = onColorSourceChange,
                        onDarkModeChange = onDarkModeChange,
                        onPureBlackChange = onPureBlackChange,
                        isDynamicColorAvailable = isDynamicColorAvailable,
                        onWeekStartDayChange = onWeekStartDayChange,
                        onAppFontChange = onAppFontChange
                    )
                }
            }


            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCategoryHeader(title = "Intake & Widgets")

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 250))
                ) {
                    HydrationSection(
                        userProfile = userProfile,
                        onHydrationStandardChange = onHydrationStandardChange
                    )
                }

                if (containerPresetRepository != null) {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            initialOffsetY = { it / 2 }
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 260))
                    ) {
                        ContainerPresetsSection(
                            containerPresetRepository = containerPresetRepository,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }

                if (userRepository != null) {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            initialOffsetY = { it / 2 }
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 265))
                    ) {
                        SettingsSectionCard {
                            SettingsJoinedBlock(
                                isFirst = true,
                                isLast = false,
                                onClick = { onNavigateToBeverageTypes() }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Beverage Types",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Reorder and hide beverages",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            SettingsJoinedBlock(
                                isFirst = false,
                                isLast = true,
                                onClick = { onNavigateToWidgetCustomization() }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Widget Customization",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Customize quick-add amounts on the home widget",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }


            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCategoryHeader(title = "Health")

                if (HealthConnectManager.isVersionSupported()) {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            initialOffsetY = { it / 2 }
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 275))
                    ) {
                        HealthConnectSection(
                            healthConnectPermissionLauncher = healthConnectPermissionLauncher,
                            userProfile = userProfile,
                            onHealthConnectSyncChange = { enabled ->
                                userProfile?.let { profile ->
                                    val updatedProfile = profile.copy(healthConnectSyncEnabled = enabled)
                                    userRepository?.saveUserProfile(updatedProfile)
                                }
                            },
                            onNavigateToHealthConnectData = onNavigateToHealthConnectData
                        )
                    }
                }
            }


            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCategoryHeader(title = "Notifications")

                NotificationSettingsSection(
                    userProfile = userProfile,
                    onRequestPermission = onRequestNotificationPermission,
                    isVisible = isVisible
                )
            }

            if (developerOptionsEnabled && userRepository != null && waterIntakeRepository != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsCategoryHeader(title = "Developer Options")

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            initialOffsetY = { it / 2 }
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
                    ) {
                        DeveloperOptionsSection(
                            userRepository = userRepository,
                            waterIntakeRepository = waterIntakeRepository,
                            snackbarHostState = snackbarHostState,
                            onNavigateToOnboarding = onNavigateToOnboarding,
                            onDisableDeveloperOptions = {
                                developerOptionsEnabled = false
                                userRepository.saveDeveloperOptionsEnabled(false)
                            },
                            userProfile = userProfile
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCategoryHeader(title = "About")

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 480))
                ) {
                    SettingsSectionCard {
                        SettingsJoinedBlock(
                            isFirst = true,
                            isLast = true,
                            onClick = { onNavigateToAbout() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "About HydroTracker",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Sources, privacy policy and license",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                FooterSection(
                    onVersionTap = {
                        val currentTime = System.currentTimeMillis()

                        // Reset counter if more than 3 seconds have passed
                        if (currentTime - lastTapTime > 3000) {
                            tapCount = 1
                        } else {
                            tapCount++
                        }

                        lastTapTime = currentTime

                        // Activate developer options after 10 taps
                        if (tapCount >= 10 && !developerOptionsEnabled) {
                            developerOptionsEnabled = true
                            userRepository?.saveDeveloperOptionsEnabled(true)

                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Developer options activated",
                                    duration = SnackbarDuration.Short
                                )
                            }

                            tapCount = 0
                        }
                    },
                    isVisible = isVisible
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp)
    )
}

internal fun JoinedBlockShape(isFirst: Boolean, isLast: Boolean): AbsoluteRoundedCornerShape =
    AbsoluteRoundedCornerShape(
        topLeft = if (isFirst) 16.dp else 4.dp,
        topRight = if (isFirst) 16.dp else 4.dp,
        bottomLeft = if (isLast) 16.dp else 4.dp,
        bottomRight = if (isLast) 16.dp else 4.dp
    )

@Composable
internal fun SettingsJoinedBlock(
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = JoinedBlockShape(isFirst, isLast)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = shape
                )
                .then(
                    when {
                        onClick != null -> Modifier.clickable(
                            enabled = enabled,
                            onClick = onClick
                        )
                        checked != null && onCheckedChange != null -> Modifier.toggleable(
                            value = checked,
                            enabled = enabled,
                            onValueChange = onCheckedChange
                        )
                        else -> Modifier
                    }
                )
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
internal fun SettingsSectionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeSection(
    themePreferences: ThemePreferences,
    onColorSourceChange: (ColorSource) -> Unit,
    onDarkModeChange: (DarkModePreference) -> Unit,
    onPureBlackChange: (Boolean) -> Unit,
    onAppFontChange: (AppFont) -> Unit,
    onWeekStartDayChange: (WeekStartDay) -> Unit,
    isDynamicColorAvailable: Boolean,
) {
    SettingsSectionCard {
        val haptics = LocalHapticFeedback.current

        // Theme Mode
        SettingsJoinedBlock(isFirst = true, isLast = false) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Theme Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Connected Button Groups for Theme Mode
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DarkModePreference.entries.forEach { preference ->
                        val isSelected = themePreferences.darkMode == preference

                        ToggleButton(
                            shapes = ToggleButtonDefaults.shapes(
                                shape = MaterialTheme.shapes.extraLarge,
                                pressedShape = MaterialTheme.shapes.small,
                                checkedShape = MaterialTheme.shapes.large
                            ),
                            checked = isSelected,
                            onCheckedChange = {
                                onDarkModeChange(preference)
                                haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            },
                           // modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (preference) {
                                        DarkModePreference.SYSTEM -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
                                        DarkModePreference.LIGHT -> if (isSelected) Icons.Filled.LightMode else Icons.Outlined.LightMode
                                        DarkModePreference.DARK -> if (isSelected) Icons.Filled.DarkMode else Icons.Outlined.DarkMode
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = when (preference) {
                                        DarkModePreference.SYSTEM -> "System"
                                        DarkModePreference.LIGHT -> "Light"
                                        DarkModePreference.DARK -> "Dark"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        val isDynamicColor = themePreferences.colorSource == ColorSource.DYNAMIC_COLOR

        // Dynamic Colors Toggle - only show if available
        if (isDynamicColorAvailable) {
            SettingsJoinedBlock(
                isFirst = false,
                isLast = false,
                checked = isDynamicColor,
                onCheckedChange = { enabled ->
                    onColorSourceChange(
                        if (enabled) ColorSource.DYNAMIC_COLOR else ColorSource.HYDRO_THEME
                    )
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Dynamic Colors",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Colors from your wallpaper",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDynamicColor,
                        onCheckedChange = { enabled ->
                            onColorSourceChange(
                                if (enabled) ColorSource.DYNAMIC_COLOR else ColorSource.HYDRO_THEME
                            )
                        },
                        thumbContent = if (isDynamicColor) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    tint = MaterialTheme.colorScheme.primary,
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
        }

        // Pure Black Toggle
        val isPureBlack = themePreferences.usePureBlack
        SettingsJoinedBlock(
            isFirst = false,
            isLast = false,
            checked = isPureBlack,
            onCheckedChange = { enabled ->
                onPureBlackChange(enabled)
                haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "AMOLED Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "True black backgrounds in dark mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isPureBlack,
                    onCheckedChange = { enabled ->
                        onPureBlackChange(enabled)
                        haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    },
                    thumbContent = if (isPureBlack) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                tint = MaterialTheme.colorScheme.primary,
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

        // Application Font
        SettingsJoinedBlock(isFirst = false, isLast = false) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FontDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Application Font",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Grid or Column of font choices
                val haptics = LocalHapticFeedback.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppFont.entries.forEach { font ->
                        val isSelected = themePreferences.appFont == font
                        ToggleButton(
                            shapes = ToggleButtonDefaults.shapes(
                                shape = MaterialTheme.shapes.extraLarge,
                                pressedShape = MaterialTheme.shapes.small,
                                checkedShape = MaterialTheme.shapes.large
                            ),
                            modifier = Modifier.animateContentSize(),
                            checked = isSelected,
                            onCheckedChange = {
                                onAppFontChange(font)
                                haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            }
                        ) {
                            Text(
                                text = font.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Week Start
        SettingsJoinedBlock(isFirst = false, isLast = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Week Start",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Connected Button Groups for Week Start Day
                val haptics = LocalHapticFeedback.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WeekStartDay.entries.forEach { weekStartDay ->
                        val isSelected = themePreferences.weekStartDay == weekStartDay

                        ToggleButton(
                            shapes = ToggleButtonDefaults.shapes(
                                shape = MaterialTheme.shapes.extraLarge,
                                pressedShape = MaterialTheme.shapes.small,
                                checkedShape = MaterialTheme.shapes.large
                            ),
                            checked = isSelected,
                            onCheckedChange = {
                                onWeekStartDayChange(weekStartDay)
                                haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (weekStartDay) {
                                        WeekStartDay.SUNDAY -> if (isSelected) Icons.Filled.Weekend else Icons.Outlined.Weekend
                                        WeekStartDay.MONDAY -> if (isSelected) Icons.Filled.Today else Icons.Outlined.Today
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = when (weekStartDay) {
                                        WeekStartDay.SUNDAY -> "Sunday"
                                        WeekStartDay.MONDAY -> "Monday"
                                    },
                                    style = MaterialTheme.typography.labelLarge
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
private fun AsyncDebugActionButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    snackbarHostState: SnackbarHostState,
    onClick: suspend () -> Unit,
    confirmationMessage: String
) {
    var isPressed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "debug_button_press"
    )

    Card(
        onClick = {
            if (!isLoading) {
                isPressed = true
                isLoading = true
                coroutineScope.launch {
                    try {
                        onClick()
                        snackbarHostState.showSnackbar(
                            message = confirmationMessage,
                            duration = SnackbarDuration.Long
                        )
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            message = "Error: ${e.message}",
                            duration = SnackbarDuration.Long
                        )
                    } finally {
                        isLoading = false
                        kotlinx.coroutines.delay(150)
                        isPressed = false
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                ContainedLoadingIndicator(

                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FooterSection(
    onVersionTap: () -> Unit,
    isVisible: Boolean
) {
    val context = LocalContext.current
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 2 }
        ) + fadeIn(animationSpec = tween(600, delayMillis = 500))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "HydroTracker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { onVersionTap() }
                )
                
                Text(
                    text = "Developed by Ali Cem Çakmak",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Fork maintained by dev778g-me",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/dev778g-me/".toUri())
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeveloperOptionsSection(
    userRepository: UserRepository,
    waterIntakeRepository: WaterIntakeRepository,
    snackbarHostState: SnackbarHostState,
    onNavigateToOnboarding: () -> Unit,
    onDisableDeveloperOptions: () -> Unit,
    userProfile: UserProfile?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeveloperMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Developer Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Text(
                text = "These options are for development and testing purposes only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            
            // Disable Developer Options Toggle
            Card(
                onClick = onDisableDeveloperOptions,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Disable Developer Options",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Hide developer options from settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f)
            )

            // Reset Onboarding Button
            ResetOnboardingButton(
                snackbarHostState = snackbarHostState,
                onClick = {
                    userRepository.resetOnboarding()
                    onNavigateToOnboarding()
                }
            )

            // Clear All Data Button
            AsyncDebugActionButton(
                title = "Clear All Data",
                description = "Remove all stored user preferences and water data",
                icon = Icons.Default.DeleteForever,
                snackbarHostState = snackbarHostState,
                onClick = {
                    userRepository.clearUserProfile()
                    waterIntakeRepository.clearAllData()
                },
                confirmationMessage = "All data cleared!"
            )

            AsyncDebugActionButton(
                title = "Inject 30-Day Data",
                description = "Add realistic water intake data for past 30 days",
                icon = Icons.Default.DataObject,
                snackbarHostState = snackbarHostState,
                onClick = {
                    waterIntakeRepository.injectDebugData()
                },
                confirmationMessage = "30 days of realistic data injected! Check History screen."
            )

            // Health Connect Debug Section - only show if Health Connect is supported and enabled
            if (HealthConnectManager.isVersionSupported() && userProfile?.healthConnectSyncEnabled == true) {
                val context = LocalContext.current // Capture context in Composable scope

                Text(
                    text = "Health Connect Testing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Test Health Connect Write
                AsyncDebugActionButton(
                    title = "Test Health Connect Write",
                    description = "Write a 250ml test entry to Health Connect",
                    icon = Icons.Default.CloudUpload,
                    snackbarHostState = snackbarHostState,
                    onClick = {
                        try {
                            val healthConnectManager = HealthConnectManager
                            val testEntry = com.dev.hydrotracker.data.database.entities.WaterIntakeEntry(
                                amount = 250.0,
                                timestamp = System.currentTimeMillis(),
                                date = java.time.LocalDate.now().toString(),
                                containerType = "Debug Test",
                                containerVolume = 250.0,
                                note = "Health Connect Debug Test Entry"
                            )
                            val result = healthConnectManager.writeHydrationRecord(context,testEntry)
                            Log.i("HealthConnectDebug", "Test write result: ${result.getOrNull()}")
                        } catch (e: Exception) {
                            Log.e("HealthConnectDebug", "Test write failed", e)
                        }
                    },
                    confirmationMessage = "Test entry sent to Health Connect! Check logs for results."
                )

                // Test Health Connect Read
                AsyncDebugActionButton(
                    title = "Test Health Connect Read",
                    description = "Read recent hydration records from Health Connect",
                    icon = Icons.Default.CloudDownload,
                    snackbarHostState = snackbarHostState,
                    onClick = {
                        try {
                            val healthConnectManager = HealthConnectManager
                            val yesterday = java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS)
                            val result = healthConnectManager.readHydrationRecords(context,yesterday)
                            Log.i("HealthConnectDebug", "Found ${result.getOrNull()?.size ?: 0} records since yesterday")
                            result.getOrNull()?.forEach { record ->
                                Log.d("HealthConnectDebug", "Record: ${record.volume.inMilliliters}ml at ${record.startTime}")
                            }
                        } catch (e: Exception) {
                            Log.e("HealthConnectDebug", "Test read failed", e)
                        }
                    },
                    confirmationMessage = "Health Connect read test completed! Check logs for results."
                )

                // Test Health Connect Import (External Data)
                AsyncDebugActionButton(
                    title = "Test Health Connect Import",
                    description = "Import external hydration data from Health Connect",
                    icon = Icons.Default.Download,
                    snackbarHostState = snackbarHostState,
                    onClick = {
                        try {
                            val healthConnectSyncManager = com.dev.hydrotracker.health.HealthConnectSyncManager
                            val since = java.time.Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS)
                            Log.i("HealthConnectDebug", "🔄 Starting import test for last 7 days...")
                            healthConnectSyncManager.importExternalHydrationData(context, userRepository, waterIntakeRepository, since) { imported, errors ->
                                Log.i("HealthConnectDebug", "📊 Import test result: $imported entries imported, $errors errors")
                            }
                        } catch (e: Exception) {
                            Log.e("HealthConnectDebug", "Import test failed", e)
                        }
                    },
                    confirmationMessage = "Health Connect import test started! Check logs for detailed results."
                )

                // Health Connect Status Check
                AsyncDebugActionButton(
                    title = "Check Health Connect Status",
                    description = "Verify Health Connect availability and permissions",
                    icon = Icons.Default.HealthAndSafety,
                    snackbarHostState = snackbarHostState,
                    onClick = {
                        try {
                            val healthConnectManager = HealthConnectManager
                            Log.i("HealthConnectDebug", "=== Health Connect Status Check ===")
                            Log.i("HealthConnectDebug", "Available: ${healthConnectManager.isAvailable(context)}")
                            Log.i("HealthConnectDebug", "Has Permissions: ${healthConnectManager.hasPermissions(context)}")
                            Log.i("HealthConnectDebug", "Status: ${healthConnectManager.getStatusMessage(context)}")
                            Log.i("HealthConnectDebug", "Sync Enabled: ${userProfile.healthConnectSyncEnabled}")
                            HealthConnectManager.debugPermissions()
                        } catch (e: Exception) {
                            Log.e("HealthConnectDebug", "Status check failed", e)
                        }
                    },
                    confirmationMessage = "Health Connect status logged! Check: adb logcat | grep HealthConnectDebug"
                )

                Text(
                    text = "💡 View logs with: adb logcat | grep -E \"(HealthConnect|HealthConnectDebug|HealthConnectTest)\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Show Current Status
            val isOnboardingCompleted by userRepository.isOnboardingCompleted.collectAsState()
            val currentUserProfile by userRepository.userProfile.collectAsState()

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Current Status",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Onboarding Completed: $isOnboardingCompleted",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "User Profile Exists: ${currentUserProfile != null}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (currentUserProfile != null) {
                        Text(
                            text = "Daily Goal: ${currentUserProfile!!.dailyWaterGoal.toInt()} ml",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Debug Notification Section
            DebugNotificationSection(
                userProfile = userProfile,
                waterIntakeRepository = waterIntakeRepository,
                snackbarHostState = snackbarHostState,
                isVisible = true
            )
        }
    }
}

@Composable
private fun ResetOnboardingButton(
    snackbarHostState: SnackbarHostState,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "debug_button_press"
    )

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reset Onboarding",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Clear user data and restart onboarding",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Show confirmation snackbar
    LaunchedEffect(isPressed) {
        if (isPressed) {
            snackbarHostState.showSnackbar(
                message = "Onboarding reset! Redirecting...",
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun HydrationSection(
    userProfile: UserProfile?,
    onHydrationStandardChange: (HydrationStandard) -> Unit
) {
    val hasProfile = userProfile != null
    SettingsSectionCard {
        SettingsJoinedBlock(isFirst = true, isLast = !hasProfile) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Calculation Standard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Hydration Standard Toggle
                val haptics = LocalHapticFeedback.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HydrationStandard.entries.forEach { standard ->
                        val isSelected = userProfile?.hydrationStandard == standard

                        ToggleButton(
                            shapes = ToggleButtonDefaults.shapes(
                                shape = MaterialTheme.shapes.extraLarge,
                                pressedShape = MaterialTheme.shapes.small,
                                checkedShape = MaterialTheme.shapes.large
                            ),
                            checked = isSelected,
                            onCheckedChange = {
                                onHydrationStandardChange(standard)
                                haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = standard.getDisplayName(),
                                    style = if (isSelected) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge,
                                )
                                Text(
                                    text = when (standard) {
                                        HydrationStandard.EFSA -> "Conservative"
                                        HydrationStandard.IOM -> "Higher intake"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Current values display
        userProfile?.let { profile ->
            SettingsJoinedBlock(isFirst = false, isLast = true) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Current Standards:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Male baseline:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${profile.hydrationStandard.getMaleIntake().toInt() / 1000.0} L",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Female baseline:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${profile.hydrationStandard.getFemaleIntake().toInt() / 1000.0} L",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContainerPresetsSection(
    containerPresetRepository: ContainerPresetRepository,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    var showResetConfirmation by remember { mutableStateOf(false) }

    SettingsSectionCard {
        SettingsJoinedBlock(isFirst = true, isLast = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalDrink,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Container Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Customize the quick select containers on the home screen. Long-press any container to edit or delete it. Use the button to restore to the default state",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Reset to defaults button
            OutlinedButton(
                onClick = { showResetConfirmation = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset to Defaults")
            }
            }
        }
    }

    // Reset confirmation dialog
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Reset Container Presets?") },
            text = {
                Text("This will remove all custom containers and restore the 7 default presets. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmation = false
                        coroutineScope.launch {
                            containerPresetRepository.resetToDefaults()
                            snackbarHostState.showSuccessSnackbar(
                                message = "Container presets reset to defaults"
                            )
                        }
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HealthConnectSection(
    healthConnectPermissionLauncher: ActivityResultLauncher<Set<String>>? = null,
    userProfile: UserProfile? = null,
    onHealthConnectSyncChange: (Boolean) -> Unit = {},
    onNavigateToHealthConnectData: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isHealthConnectEnabled by remember { mutableStateOf(false) }
    var healthConnectStatus by remember { mutableStateOf("Checking...") }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val manager = HealthConnectManager

    // Check Health Connect status on component mount and when refresh is triggered
    LaunchedEffect(refreshTrigger) {
        try {

            val status = manager.getStatusMessage(context)
            healthConnectStatus = status
            val newIsEnabled = status == "Health Connect is ready"

            isHealthConnectEnabled = newIsEnabled
        } catch (e: Exception) {
            healthConnectStatus = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Listen for when app regains focus to refresh permissions
    androidx.compose.runtime.DisposableEffect(Unit) {
        val activity = context as? ComponentActivity
        val listener = object : android.app.Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: android.app.Activity) {
                if (activity == context) {
                    // Refresh permissions when returning to this screen
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

    val showHealthData = userProfile?.healthConnectSyncEnabled == true && isHealthConnectEnabled

    SettingsSectionCard {
        // Main sync toggle
        val toggleSync: (Boolean) -> Unit = { enabled ->
            onHealthConnectSyncChange(enabled)
            if (enabled && !isHealthConnectEnabled) {
                if (healthConnectPermissionLauncher != null) {
                    coroutineScope.launch {
                        manager.checkPermissionsAndRun(context, healthConnectPermissionLauncher) {
                            healthConnectStatus = "Health Connect is ready"
                            isHealthConnectEnabled = true
                        }
                    }
                } else {
                    Log.w("HealthConnect", "Permission launcher not available")
                    healthConnectStatus = "Error: Permission launcher not available"
                }
            }
        }

        SettingsJoinedBlock(
            isFirst = true,
            isLast = !showHealthData,
            checked = userProfile?.healthConnectSyncEnabled == true,
            enabled = !isLoading,
            onCheckedChange = toggleSync
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sync with Health Connect",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            isLoading -> "Checking..."
                            isHealthConnectEnabled -> "Health Connect is ready"
                            healthConnectStatus.contains("Missing") ||
                                healthConnectStatus.contains("Permissions") -> "Grant permission to enable syncing"
                            else -> healthConnectStatus
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = userProfile?.healthConnectSyncEnabled == true,
                    enabled = !isLoading,
                    onCheckedChange = toggleSync,
                    thumbContent = {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                )
            }
        }

        // Health Connect Data (only when enabled & ready)
        if (showHealthData) {
            SettingsJoinedBlock(
                isFirst = false,
                isLast = true,
                onClick = { onNavigateToHealthConnectData() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Health Connect Data",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "View and manage Health Connect data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    HydroTrackerTheme {
        SettingsScreen()
    }
}
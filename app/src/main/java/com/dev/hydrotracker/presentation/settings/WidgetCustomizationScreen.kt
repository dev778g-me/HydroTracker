package com.dev.hydrotracker.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dev.hydrotracker.data.models.WidgetPreferences
import com.dev.hydrotracker.data.repository.UserRepository
import com.dev.hydrotracker.presentation.common.HydroSnackbarHost
import com.dev.hydrotracker.widgets.WidgetUpdateHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetCustomizationScreen(
    userRepository: UserRepository,
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val loaded = remember(userRepository) { userRepository.loadWidgetPreferences() }
    val defaultAmounts = remember { WidgetPreferences.DEFAULT_AMOUNTS }

    var amounts by remember {
        mutableStateOf(loaded.amounts.map { it.toString() }.toList())
    }

    val currentParsed = amounts.map { it.toIntOrNull() }
    val isValid = amounts.size == defaultAmounts.size &&
        amounts.all { it.isNotBlank() && (it.toIntOrNull() ?: 0) > 0 }
    val hasChanges = isValid && currentParsed != loaded.amounts

    fun update(index: Int, value: String) {
        if (value.length > 5) return
        if (value.isEmpty() || value.all { it.isDigit() }) {
            amounts = amounts.toMutableList().also { it[index] = value }
        }
    }

    fun save() {
        if (!isValid) return
        userRepository.saveWidgetPreferences(
            WidgetPreferences(currentParsed.map { it!! })
        )
        coroutineScope.launch {
            WidgetUpdateHelper.updateAllWidgets(context)
            snackbarHostState.showSnackbar(
                message = "Widget preferences saved",
                duration = SnackbarDuration.Short
            )
        }
    }

    fun reset() {
        amounts = defaultAmounts.map { it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Widget Customization",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Customize the quick-add amounts on the large Home Screen widget. Defaults are used until you change them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Quick Add Buttons",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        amounts.forEachIndexed { index, value ->
                            OutlinedTextField(
                                shape = MaterialTheme.shapes.medium,
                                value = value,
                                onValueChange = { update(index, it) },
                                label = { Text("Amount ${index + 1}") },
                                suffix = { Text("ml") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                supportingText = {
                                    Text("Default: ${defaultAmounts[index]}ml")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }



            Button(
                onClick = ::save,
                enabled = hasChanges,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shapes = ButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Preferences",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            OutlinedButton(
                onClick = ::reset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shapes = ButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset to Defaults",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "Changes are applied to your widgets after you tap Save Preferences.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


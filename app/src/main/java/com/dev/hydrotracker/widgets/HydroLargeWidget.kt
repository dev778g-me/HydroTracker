package com.dev.hydrotracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.dev.hydrotracker.MainActivity
import com.dev.hydrotracker.R
import com.dev.hydrotracker.data.repository.UserRepository
import com.dev.hydrotracker.data.database.DatabaseInitializer
import com.dev.hydrotracker.utils.WaterCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

/**
 * HydroTracker Large Widget (4x2)
 * Shows progress with quick add buttons
 */
class HydroLargeWidget : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val ACTION_QUICK_ADD = "com.dev.hydrotracker.QUICK_ADD"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_CONTAINER = "container"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_QUICK_ADD) {
            val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
            val container = intent.getStringExtra(EXTRA_CONTAINER) ?: "Glass"

            // Use goAsync() to keep the BroadcastReceiver alive during async operations
            val pendingResult = goAsync()

            widgetScope.launch {
                try {
                    val userRepository = UserRepository(context)
                    val waterRepository = DatabaseInitializer.getWaterIntakeRepository(context, userRepository)

                    // Quick add water
                    waterRepository.addWaterIntake(
                        amount = amount,
                        containerPreset = com.dev.hydrotracker.data.models.ContainerPreset(
                            name = container,
                            volume = amount
                        )
                    )

                    // Update all widgets
                    WidgetUpdateHelper.updateAllWidgets(context)

                } catch (_: Exception) {
                    // Handle error silently for widgets
                } finally {
                    // Finish the async operation
                    pendingResult.finish()
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateService.scheduleUpdates(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateService.cancelUpdates(context)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        widgetScope.launch {
            try {
                val userRepository = UserRepository(context)
                val waterRepository = DatabaseInitializer.getWaterIntakeRepository(context, userRepository)
                
                val progress = waterRepository.getTodayProgress().first()
                val userProfile = userRepository.userProfile.first()
                
                val views = RemoteViews(context.packageName, R.layout.widget_hydro_large)
                
                // Update progress text
                val currentText = WaterCalculator.formatWaterAmount(progress.currentIntake)
                val goalText = userProfile?.let { WaterCalculator.formatWaterAmount(it.dailyWaterGoal) } ?: "2700ml"
                val progressText = "$currentText / $goalText"
                
                views.setTextViewText(R.id.widget_progress_text, progressText)
                
                // Update progress percentage
                val progressPercent = (progress.progress * 100).toInt()
                views.setTextViewText(R.id.widget_progress_percent, "$progressPercent%")
                
                // Update last updated time
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val lastUpdated = "Updated ${timeFormat.format(Date())}"
                views.setTextViewText(R.id.widget_last_updated, lastUpdated)
                
                // Set progress bar
                views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)
                
                // Set up quick add buttons
                setupQuickAddButtons(context, views, userRepository.loadWidgetPreferences().amounts)
                
                // Set main click intent
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                // Apply Material 3 theme
                WidgetTheme.applyTheme(
                    context = context,
                    views = views,
                    containerId = R.id.widget_container,
                    progressBarId = R.id.widget_progress_bar,
                    titleTextViewIds = listOf(R.id.widget_title),
                    textViewIds = listOf(R.id.widget_progress_text),
                    accentTextViewIds = listOf(R.id.widget_progress_percent),
                    variantTextViewIds = listOf(R.id.widget_last_updated),
                    buttonTextViewIds = listOf(
                        R.id.widget_btn_250_text,
                        R.id.widget_btn_300_text,
                        R.id.widget_btn_500_text,
                        R.id.widget_btn_1l_text
                    )
                )

                appWidgetManager.updateAppWidget(appWidgetId, views)
                
            } catch (_: Exception) {
                updateWidgetWithDefaults(context, appWidgetManager, appWidgetId)
            }
        }
    }
    
    private fun setupQuickAddButtons(
        context: Context,
        views: RemoteViews,
        amounts: List<Int> = com.dev.hydrotracker.data.models.WidgetPreferences.DEFAULT_AMOUNTS
    ) {
        val buttonConfigs = listOf(
            Triple(R.id.widget_btn_250, R.id.widget_btn_250_text, "Glass"),
            Triple(R.id.widget_btn_300, R.id.widget_btn_300_text, "Glass"),
            Triple(R.id.widget_btn_500, R.id.widget_btn_500_text, "Bottle"),
            Triple(R.id.widget_btn_1l, R.id.widget_btn_1l_text, "Large Bottle")
        )

        buttonConfigs.forEachIndexed { index, config ->
            val (buttonId, textId, container) = config
            val amount = amounts.getOrElse(index) { com.dev.hydrotracker.data.models.WidgetPreferences.DEFAULT_AMOUNTS[index] }.toDouble()

            views.setTextViewText(textId, formatWidgetLabel(amount))

            val clickIntent = Intent(context, HydroLargeWidget::class.java).apply {
                action = ACTION_QUICK_ADD
                putExtra(EXTRA_AMOUNT, amount)
                putExtra(EXTRA_CONTAINER, container)
            }
            val pending = PendingIntent.getBroadcast(
                context, 1001 + index, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(buttonId, pending)
        }
    }

    private fun formatWidgetLabel(amount: Double): String {
        return if (amount >= 1000 && amount % 1000 == 0.0) {
            "${(amount / 1000).toInt()}L"
        } else {
            "${amount.toInt()}ml"
        }
    }
    
    private fun updateWidgetWithDefaults(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_hydro_large)
        
        views.setTextViewText(R.id.widget_progress_text, "0ml / 2700ml")
        views.setTextViewText(R.id.widget_progress_percent, "0%")
        views.setTextViewText(R.id.widget_last_updated, "Tap to open HydroTracker")
        views.setProgressBar(R.id.widget_progress_bar, 100, 0, false)
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        setupQuickAddButtons(context, views)

        // Apply Material 3 theme
        WidgetTheme.applyTheme(
            context = context,
            views = views,
            containerId = R.id.widget_container,
            progressBarId = R.id.widget_progress_bar,
            titleTextViewIds = listOf(R.id.widget_title),
            textViewIds = listOf(R.id.widget_progress_text),
            accentTextViewIds = listOf(R.id.widget_progress_percent),
            variantTextViewIds = listOf(R.id.widget_last_updated),
            buttonTextViewIds = listOf(
                R.id.widget_btn_250_text,
                R.id.widget_btn_300_text,
                R.id.widget_btn_500_text,
                R.id.widget_btn_1l_text
            )
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
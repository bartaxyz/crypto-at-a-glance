package com.ondrejbarta.cryptoataglance

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.widget.RemoteViews
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [ClockWidgetConfigureActivity]
 */
class ClockWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateClockAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateClockAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // val widgetText = loadTitlePref(context, appWidgetId)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.clock_widget)
    val currentTime = Calendar.getInstance().time
    val text =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime)

    Log.i("CLOCK_APP", text)

    views.setTextViewText(R.id.clock_text_view, text)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
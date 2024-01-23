package com.ondrejbarta.cryptoataglance

import DailyQuoteWidgetQuotesData
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONArray
import java.util.*

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [DailyQuoteWidgetConfigureActivity]
 */
class DailyQuoteWidget : CompleteWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            Log.i("DAILY_QUOTE_WIDGET", appWidgetId.toString());

            updateDailyQuoteAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteCategoryPref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateDailyQuoteAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val category = loadCategoryPref(context, appWidgetId)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.daily_quote_widget)

    val jsonArray = JSONArray(DailyQuoteWidgetQuotesData)

    val index = (0..(jsonArray.length() - 1)).random()

    val quoteAuthor = jsonArray.getJSONObject(index).getString("quoteAuthor")
    val quoteText = jsonArray.getJSONObject(index).getString("quoteText")


    views.setTextViewText(R.id.daily_quote_label, quoteAuthor)
    views.setTextViewText(R.id.daily_quote, quoteText)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
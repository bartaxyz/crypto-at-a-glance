package com.ondrejbarta.cryptoataglance

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.view.View
import android.widget.RemoteViews

open class CompleteWidgetProvider: AppWidgetProvider() {

}

internal fun showError(
    errorMessage: String,
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    val errorView = RemoteViews(context.packageName, R.layout.widget_error_message)
    errorView.setTextViewText(R.id.error_message_text_view, errorMessage)
    appWidgetManager.updateAppWidget(appWidgetId, RemoteViews(errorView))
}

internal fun showError(
    errorMessage: String,
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    secondErrorMessage: String
) {
    val errorView = RemoteViews(context.packageName, R.layout.widget_error_message)
    errorView.setTextViewText(R.id.error_message_text_view, errorMessage)

    if (secondErrorMessage != null) {
        errorView.setViewVisibility(R.id.error_message_second_line_text_view, View.VISIBLE)
        errorView.setTextViewText(R.id.error_message_second_line_text_view, secondErrorMessage)
    } else {
        errorView.setViewVisibility(R.id.error_message_second_line_text_view, View.GONE)
    }

    appWidgetManager.updateAppWidget(appWidgetId, RemoteViews(errorView))
}
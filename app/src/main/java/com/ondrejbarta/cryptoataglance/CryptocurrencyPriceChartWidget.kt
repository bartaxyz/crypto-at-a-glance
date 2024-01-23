package com.ondrejbarta.cryptoataglance

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.*
import android.icu.text.NumberFormat
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import android.os.PowerManager
import java.util.*
import android.app.PendingIntent

import android.content.Intent
import android.content.ComponentName
import android.util.Log


class MarketChart(
    val prices: List<List<String>>
    /*
    @SerialName("market_caps")
    val marketCaps: List<List<String>>,
    @SerialName("total_volumes")
    val totalVolumes: List<List<String>>
     */
)

/**
 * Implementation of App Widget functionality.
 */
class CryptocurrencyPriceChartWidget : CompleteWidgetProvider() {
    lateinit var appWidgetManager: AppWidgetManager;

    override fun onUpdate(
        context: Context,
        _appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetManager = _appWidgetManager
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateCryptocurrencyPriceChartAppWidget(context, _appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteBaseCurrencyPref(context, appWidgetId)
            deleteCryptocurrencyPref(context, appWidgetId)
            deleteCryptocurrencyFirstRunPref(context, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        super.onReceive(context, intent)
        if (ACTION_ERROR_TAP == intent.action) {
            // if the user clicked error message

            val appWidgetManager = AppWidgetManager.getInstance(context)

            val thisAppWidgetComponentName = ComponentName(context!!.packageName, javaClass.name)
            val appWidgetIds: IntArray =
                appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName)
            onUpdate(context!!, appWidgetManager, appWidgetIds)
        }
    }
}

private fun getPendingSelfIntent(context: Context, action: String): PendingIntent? {

    Log.i("LOGGGGGIIIING", "LOGGGGGIIIING")
    val intent = Intent(
        context,
        CryptocurrencyPriceChartWidget::class.java
    ) // An intent directed at the current class (the "self").
    intent.action = action
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

private const val ACTION_ERROR_TAP = "action.ACTION_ERROR_TAP"

@SuppressLint("RestrictedApi")
internal fun updateCryptocurrencyPriceChartAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val smallView =
        RemoteViews(context.packageName, R.layout.cryptocurrency_price_chart_widget_small)
    val views = RemoteViews(context.packageName, R.layout.cryptocurrency_price_chart_widget)
    val viewMapping: Map<SizeF, RemoteViews> = mapOf(
        SizeF(100f, 100f) to smallView,
        SizeF(215f, 100f) to views
    )

    val baseCurrency = loadBaseCurrencyPref(context, appWidgetId)
    val cryptocurrency = loadCryptocurrencyPref(context, appWidgetId)
    val firstRun = loadCryptocurrencyFirstRunPref(context, appWidgetId)

    val queue = Volley.newRequestQueue(context);
    val days = 1;
    val url = "https://api.coingecko.com/api/v3/coins/" +
            cryptocurrency.lowercase(Locale.getDefault()) +
            "/market_chart?vs_currency=" +
            baseCurrency.lowercase(Locale.getDefault()) +
            "&days=" + days.toString()

    /**
     * If the battery saver mode is enabled, we can't use network.
     * In a case some data is already rendered, we won't re-render anything.
     */
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    if (powerManager.isPowerSaveMode) {
        if (firstRun) {
            return showError(
                "Error loading data, battery saver mode enabled",
                context, appWidgetManager, appWidgetId,
                "Turn off battery saver and edit this widget to try again"
            )
        } else {
            return
        }
    }

    val coinGeckoRequest = JsonObjectRequest(url,
        { response ->

            /**
             * Since the request was successful, we can now set "firstRun" to false
             * to indicate we're rendering some real data.
             */
            saveCryptocurrencyFirstRunPref(context, appWidgetId, false)

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val gistJsonAdapter = moshi.adapter<MarketChart>(MarketChart::class.java)
            val marketChart = gistJsonAdapter.fromJson(response.toString())
            val prices = marketChart!!.prices

            val currentPrice = prices[prices.lastIndex][1].toFloat()
            val oldestPrice = prices[0][1].toFloat();
            val deltaPrice = currentPrice - oldestPrice;

            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            format.setMaximumFractionDigits(2)
            format.setCurrency(
                android.icu.util.Currency.getInstance(baseCurrency)
            )

            val title = cryptocurrency + " - " + baseCurrency

            views.setTextViewText(R.id.title_text_view, title)
            smallView.setTextViewText(R.id.title_text_view, title)
            views.setTextViewText(
                R.id.price_text_view,
                format.format(currentPrice)
            )
            format.setMaximumFractionDigits(0)
            smallView.setTextViewText(
                R.id.price_text_view,
                format.format(currentPrice)
            )

            var deltaPriceSign = "";
            var trendingColor = 0;
            if (deltaPrice > 0) {
                deltaPriceSign = "+"
                trendingColor = 0xFF43D65A.toInt()
                views.setViewVisibility(R.id.ic_baseline_trending_up, View.VISIBLE)
                views.setViewVisibility(R.id.ic_baseline_trending_down, View.GONE)

                smallView.setViewVisibility(R.id.ic_baseline_trending_up, View.VISIBLE)
                smallView.setViewVisibility(R.id.ic_baseline_trending_down, View.GONE)
            } else {
                trendingColor = 0xFFE53028.toInt()
                views.setViewVisibility(R.id.ic_baseline_trending_up, View.GONE)
                views.setViewVisibility(R.id.ic_baseline_trending_down, View.VISIBLE)

                smallView.setViewVisibility(R.id.ic_baseline_trending_up, View.GONE)
                smallView.setViewVisibility(R.id.ic_baseline_trending_down, View.VISIBLE)
            };
            views.setTextColor(
                R.id.price_delta_text_view,
                trendingColor
            )
            smallView.setTextColor(
                R.id.price_delta_text_view,
                trendingColor
            )
            views.setTextViewText(
                R.id.price_delta_text_view,
                deltaPriceSign + format.format(deltaPrice)
            )
            smallView.setTextViewText(
                R.id.price_delta_text_view,
                deltaPriceSign + format.format(deltaPrice)
            )

            val chartWidth: Int = (256f * 2f * 2.5f).toInt() * 2;
            val chartHeight = 256 * 2 * 2;
            val bmp = Bitmap.createBitmap(chartWidth, chartHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            val cryptocurrencyPriceWidgetChartService =
                CryptocurrencyPriceChartWidgetService(context.resources, context.theme)

            val data: MutableList<Float> = mutableListOf();

            for (price in prices) {
                data.add(price[1].toFloat())
            }

            cryptocurrencyPriceWidgetChartService.prepare(
                canvas,
                Rect(0, 0, chartWidth, chartHeight)
            )
            cryptocurrencyPriceWidgetChartService.setData(data)
            cryptocurrencyPriceWidgetChartService.draw()

            views.setImageViewBitmap(R.id.chartCanvasImageView, bmp);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, RemoteViews(viewMapping))
        },
        {
            /**
             * Display error message only if there's no data already rendered
             */
            if (firstRun) {
                showError(
                    "Error loading data",
                    context, appWidgetManager, appWidgetId,
                    "To try again, edit this widget or re-add it"
                )
            }
        }
    )

    queue.add(coinGeckoRequest)

    /**
     * In a case this is the first time our update function runs and no data is loaded
     * yet. We instead render loading indicator layout.
     */
    if (firstRun) {
        appWidgetManager.updateAppWidget(
            appWidgetId,
            RemoteViews(context.packageName, R.layout.widget_loading_message)
        )
    }
}


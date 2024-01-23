package com.ondrejbarta.cryptoataglance

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.ondrejbarta.cryptoataglance.databinding.CryptocurrencyPriceChartWidgetConfigureBinding

class CryptocurrencyPriceChartWidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var baseCurrencyAdapter: ArrayAdapter<String>
    private lateinit var baseCurrencySelect: AutoCompleteTextView
    private lateinit var cryptocurrencyAdapter: ArrayAdapter<String>
    private lateinit var cryptocurrencySelect: AutoCompleteTextView

    private var onClickListener = View.OnClickListener {
        val context = this

        saveBaseCurrencyPref(context, appWidgetId, baseCurrencySelect.text.toString())
        saveCryptocurrencyPref(context, appWidgetId, cryptocurrencySelect.text.toString())
        saveCryptocurrencyFirstRunPref(context, appWidgetId, true)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateCryptocurrencyPriceChartAppWidget(context, appWidgetManager, appWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
    private lateinit var binding: CryptocurrencyPriceChartWidgetConfigureBinding

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        val context = this

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        binding = CryptocurrencyPriceChartWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addButton.setOnClickListener(onClickListener)

        baseCurrencyAdapter = ArrayAdapter(
            context,
            R.layout.list_item,
            listOf("USD", "EUR", "CZK")
        )
        baseCurrencySelect = binding.baseCurrencySelect.editText as AutoCompleteTextView
        baseCurrencySelect?.setAdapter(
            baseCurrencyAdapter
        )
        baseCurrencySelect?.setText("USD", false)

        cryptocurrencyAdapter = ArrayAdapter(
            context,
            R.layout.list_item,
            listOf("Bitcoin", "Ethereum", "Dogecoin")
        )
        cryptocurrencySelect = binding.cryptocurrencySelect.editText as AutoCompleteTextView
        cryptocurrencySelect?.setAdapter(
            cryptocurrencyAdapter
        )
        cryptocurrencySelect?.setText("Bitcoin", false)

        // Find the widget id from the intent.
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }
}


private val PREFS_NAME = "com.ondrejbarta.youwidgets.CryptocurrencyPriceChartWidget"
private val BASE_CURRENCY_PREF_PREFIX_KEY = "appwidget_base_currency"
private val CRYPTOCURRENCY_PREF_PREFIX_KEY = "appwidget_cryptocurrency"
private val CRYPTOCURRENCY_FIRST_RUN_PREF_PREFIX_KEY = "appwidget_cryptocurrency_first_run"

// Write the prefix to the SharedPreferences object for this widget
internal fun saveBaseCurrencyPref(context: Context, appWidgetId: Int, baseCurrency: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(BASE_CURRENCY_PREF_PREFIX_KEY + appWidgetId, baseCurrency)
    prefs.apply()
}

internal fun loadBaseCurrencyPref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(BASE_CURRENCY_PREF_PREFIX_KEY + appWidgetId, null)
    return titleValue ?: "USD"
}

internal fun deleteBaseCurrencyPref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(BASE_CURRENCY_PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}

// Write the prefix to the SharedPreferences object for this widget
internal fun saveCryptocurrencyPref(
    context: Context,
    appWidgetId: Int,
    cryptocurrency: String
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(CRYPTOCURRENCY_PREF_PREFIX_KEY + appWidgetId, cryptocurrency)
    prefs.apply()
}

internal fun loadCryptocurrencyPref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(CRYPTOCURRENCY_PREF_PREFIX_KEY + appWidgetId, null)
    return titleValue ?: "Bitcoin"
}

internal fun deleteCryptocurrencyPref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(CRYPTOCURRENCY_PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}


// Write the prefix to the SharedPreferences object for this widget
internal fun saveCryptocurrencyFirstRunPref(
    context: Context,
    appWidgetId: Int,
    firstRun: Boolean
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putBoolean(CRYPTOCURRENCY_FIRST_RUN_PREF_PREFIX_KEY + appWidgetId, firstRun)
    prefs.apply()
}

internal fun loadCryptocurrencyFirstRunPref(context: Context, appWidgetId: Int): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val firstRun = prefs.getBoolean(CRYPTOCURRENCY_FIRST_RUN_PREF_PREFIX_KEY + appWidgetId, true)
    return firstRun
}

internal fun deleteCryptocurrencyFirstRunPref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(CRYPTOCURRENCY_FIRST_RUN_PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}


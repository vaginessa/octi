package eu.darken.octi.modules.power.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.octi.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.octi.common.debug.logging.log
import eu.darken.octi.common.debug.logging.logTag
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BatteryWidgetManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val widgetManager by lazy { AppWidgetManager.getInstance(context) }

    private val currentWidgetIds: IntArray
        get() = widgetManager.getAppWidgetIds(ComponentName(context, PROVIDER_CLASS))

    suspend fun refreshWidgets() {
        log(TAG) { "updateWidgets()" }

        log(TAG, VERBOSE) { "Notifying these widget IDs: ${currentWidgetIds.toList()}" }

        val intent = Intent(context, PROVIDER_CLASS).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, currentWidgetIds)
        }
        context.sendBroadcast(intent)
    }

    companion object {
        val PROVIDER_CLASS = BatteryWidgetProvider::class.java
        val TAG = logTag("Module", "Power", "Widget", "Manager")
    }
}
package com.tanilink.cekkabar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.RemoteViews
import com.tanilink.cekkabar.CekKabarApp
import com.tanilink.cekkabar.MainActivity
import com.tanilink.cekkabar.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CekKabarWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_FILTER_WIDGET) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            val filter = intent.getStringExtra(EXTRA_FILTER) ?: FILTER_ALL
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                saveFilter(context, appWidgetId, filter)
                val manager = AppWidgetManager.getInstance(context)
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        updateAppWidget(context, manager, appWidgetId)
                        manager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_history_list)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_history_list)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_FILTER_WIDGET = "com.tanilink.cekkabar.ACTION_FILTER_WIDGET"
        const val EXTRA_FILTER = "extra_filter"
        const val EXTRA_START_TAB = "extra_start_tab"
        const val EXTRA_AUTO_PASTE = "extra_auto_paste"
        const val EXTRA_HISTORY_ID = "extra_history_id"

        const val FILTER_ALL = "all"
        const val FILTER_HOAKS = "hoaks"
        const val FILTER_FAKTA = "fakta"

        private const val PREFS_NAME = "cekkabar_widget_prefs"

        fun getFilter(context: Context, appWidgetId: Int): String {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("filter_$appWidgetId", FILTER_ALL) ?: FILTER_ALL
        }

        private fun saveFilter(context: Context, appWidgetId: Int, filter: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("filter_$appWidgetId", filter)
                .apply()
        }

        fun refreshAllWidgets(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext)
            val ids = manager.getAppWidgetIds(
                ComponentName(appContext, CekKabarWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return

            CoroutineScope(Dispatchers.IO).launch {
                for (id in ids) {
                    updateAppWidget(appContext, manager, id)
                }
                manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_history_list)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_cekkabar)
            val activeFilter = getFilter(context, appWidgetId)

            val snapshot = runCatching {
                val app = context.applicationContext as? CekKabarApp
                app?.container?.repository?.getLocalSnapshot(FILTER_ALL)
            }.getOrNull()

            if (snapshot != null) {
                views.setTextViewText(
                    R.id.widget_stats_summary,
                    "${snapshot.counts.all} cek • ${snapshot.counts.hoaks} hoaks • ${snapshot.counts.fakta} fakta"
                )
            }

            // Style filter chips
            styleFilterChip(views, R.id.widget_filter_all, activeFilter == FILTER_ALL)
            styleFilterChip(views, R.id.widget_filter_hoaks, activeFilter == FILTER_HOAKS)
            styleFilterChip(views, R.id.widget_filter_fakta, activeFilter == FILTER_FAKTA)

            views.setOnClickPendingIntent(
                R.id.widget_filter_all,
                createFilterPendingIntent(context, appWidgetId, FILTER_ALL, 200 + appWidgetId * 10 + 1)
            )
            views.setOnClickPendingIntent(
                R.id.widget_filter_hoaks,
                createFilterPendingIntent(context, appWidgetId, FILTER_HOAKS, 200 + appWidgetId * 10 + 2)
            )
            views.setOnClickPendingIntent(
                R.id.widget_filter_fakta,
                createFilterPendingIntent(context, appWidgetId, FILTER_FAKTA, 200 + appWidgetId * 10 + 3)
            )

            // Scrollable ListView adapter (max 10 items)
            val serviceIntent = Intent(context, CekKabarWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_history_list, serviceIntent)
            views.setEmptyView(R.id.widget_history_list, R.id.widget_empty_view)

            // List item click template -> open history detail
            val itemClickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val itemPendingTemplate = PendingIntent.getActivity(
                context,
                300 + appWidgetId,
                itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_history_list, itemPendingTemplate)

            // Chat bar tap -> open Deteksi tab
            val chatIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_START_TAB, "deteksi")
            }
            views.setOnClickPendingIntent(
                R.id.widget_chat_bar,
                PendingIntent.getActivity(
                    context,
                    100,
                    chatIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // "Tempel & Cek" button -> open Deteksi tab + auto-paste clipboard & detect
            val pasteIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_START_TAB, "deteksi")
                putExtra(EXTRA_AUTO_PASTE, true)
            }
            views.setOnClickPendingIntent(
                R.id.widget_btn_paste_check,
                PendingIntent.getActivity(
                    context,
                    101,
                    pasteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // "Semua Riwayat" button -> open Riwayat tab
            val historyIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_START_TAB, "riwayat")
            }
            views.setOnClickPendingIntent(
                R.id.widget_btn_open_history,
                PendingIntent.getActivity(
                    context,
                    102,
                    historyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun styleFilterChip(views: RemoteViews, viewId: Int, isActive: Boolean) {
            views.setInt(
                viewId,
                "setBackgroundResource",
                if (isActive) R.drawable.widget_filter_active else R.drawable.widget_filter_inactive
            )
            views.setTextColor(
                viewId,
                Color.parseColor(if (isActive) "#FF3F3DBF" else "#FFFFFFFF")
            )
        }

        private fun createFilterPendingIntent(
            context: Context,
            appWidgetId: Int,
            filter: String,
            requestCode: Int
        ): PendingIntent {
            val intent = Intent(context, CekKabarWidgetProvider::class.java).apply {
                action = ACTION_FILTER_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(EXTRA_FILTER, filter)
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

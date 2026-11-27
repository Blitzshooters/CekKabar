package com.tanilink.cekkabar.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.appwidget.AppWidgetManager
import com.tanilink.cekkabar.CekKabarApp
import com.tanilink.cekkabar.R
import com.tanilink.cekkabar.data.model.HistoryItem

class CekKabarWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        return CekKabarWidgetFactory(applicationContext, appWidgetId)
    }
}

private class CekKabarWidgetFactory(
    private val context: Context,
    private val appWidgetId: Int
) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<HistoryItem> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        val filter = CekKabarWidgetProvider.getFilter(context, appWidgetId)
        val snapshot = runCatching {
            val app = context.applicationContext as? CekKabarApp
            app?.container?.repository?.getLocalSnapshot(filter)
        }.getOrNull()
        items = snapshot?.items?.take(10) ?: emptyList()
    }

    override fun onDestroy() {
        items = emptyList()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position !in items.indices) {
            return RemoteViews(context.packageName, R.layout.widget_history_item)
        }
        val item = items[position]
        val isHoax = item.verdict.equals("HOAKS", ignoreCase = true)
        val prob = if (isHoax) item.hoaxProbability else item.faktaProbability

        return RemoteViews(context.packageName, R.layout.widget_history_item).apply {
            setTextViewText(R.id.widget_item_badge, if (isHoax) "HOAKS" else "FAKTA")
            setInt(
                R.id.widget_item_badge,
                "setBackgroundResource",
                if (isHoax) R.drawable.widget_badge_hoaks else R.drawable.widget_badge_fakta
            )
            setTextColor(
                R.id.widget_item_badge,
                Color.parseColor(if (isHoax) "#FFE5484D" else "#FF2E9E5B")
            )
            setTextViewText(R.id.widget_item_prob, "Keyakinan ${"%.1f".format(prob)}%")
            setTextViewText(R.id.widget_item_time, item.formattedTime)
            setTextViewText(R.id.widget_item_text, item.text)

            val fillInIntent = Intent().apply {
                putExtra(CekKabarWidgetProvider.EXTRA_HISTORY_ID, item.id)
            }
            setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = items.getOrNull(position)?.id ?: position.toLong()
    override fun hasStableIds(): Boolean = true
}


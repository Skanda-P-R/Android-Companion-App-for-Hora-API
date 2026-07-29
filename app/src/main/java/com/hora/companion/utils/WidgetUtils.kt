package com.hora.companion.utils

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.hora.companion.widgets.HoraWidget

object WidgetUtils {
    suspend fun updateAllWidgets(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(HoraWidget::class.java).forEach { id ->
            HoraWidget().update(context, id)
        }
    }
}

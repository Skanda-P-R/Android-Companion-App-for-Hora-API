package com.hora.jnana.utils

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.hora.jnana.widgets.HoraWidget

object WidgetUtils {
    suspend fun updateAllWidgets(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(HoraWidget::class.java).forEach { id ->
            HoraWidget().update(context, id)
        }
    }
}

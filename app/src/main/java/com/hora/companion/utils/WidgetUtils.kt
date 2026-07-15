package com.hora.companion.utils

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.hora.companion.widgets.HoraWidget
import com.hora.companion.widgets.KundaliWidget

object WidgetUtils {
    suspend fun updateAllWidgets(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(HoraWidget::class.java).forEach { id ->
            HoraWidget().update(context, id)
        }
        manager.getGlanceIds(KundaliWidget::class.java).forEach { id ->
            KundaliWidget().update(context, id)
        }
    }
}

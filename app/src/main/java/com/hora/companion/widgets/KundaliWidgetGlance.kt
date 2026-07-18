package com.hora.companion.widgets

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.Text
import com.hora.companion.CacheManager
import com.hora.companion.DataStoreManager
import com.hora.companion.api.HoraApiService
import com.hora.companion.repository.HoraRepository
import kotlinx.coroutines.flow.first

class KundaliWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val cache = CacheManager(context)
        val dataStore = DataStoreManager(context)
        
        val lang = dataStore.langFlow.first()
        
        val bytes = cache.readBytes("kundali.png")
        var error: String? = null
        val bitmap = bytes?.let { 
            try {
                BitmapFactory.decodeByteArray(it, 0, it.size) 
            } catch (e: Exception) {
                error = "Decode failed: ${e.message}"
                null
            }
        } ?: run {
            error = "No cache"
            null
        }

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (bitmap != null) {
                        Image(
                            provider = ImageProvider(bitmap),
                            contentDescription = "Kundali",
                            modifier = GlanceModifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (lang == "kn") "ಕುಂಡಲಿ ಇಲ್ಲ" else "No Kundali",
                                style = androidx.glance.text.TextStyle(color = GlanceTheme.colors.onSurface)
                            )
                            error?.let {
                                Text(
                                    text = it,
                                    style = androidx.glance.text.TextStyle(
                                        color = GlanceTheme.colors.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class KundaliWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KundaliWidget()
}

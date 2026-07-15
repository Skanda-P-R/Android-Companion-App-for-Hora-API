package com.hora.companion.widgets

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
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
        val location = dataStore.locationFlow.first() ?: (12.9716 to 77.5946)
        
        // Optionally fetch new image in background if needed, but for now use cache
        val bytes = cache.readBytes("kundali.png")
        val bitmap = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background)
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
                        Text("No Kundali")
                    }
                }
            }
        }
    }
}

class KundaliWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KundaliWidget()
}

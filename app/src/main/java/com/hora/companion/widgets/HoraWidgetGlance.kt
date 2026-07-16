package com.hora.companion.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import com.hora.companion.CacheManager
import com.hora.companion.DataStoreManager
import com.hora.companion.repository.HoraRepository
import com.hora.companion.api.HoraApiService
import com.hora.companion.utils.TranslationUtils
import kotlinx.coroutines.flow.first

class HoraWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val cache = CacheManager(context)
        val dataStore = DataStoreManager(context)
        val lang = dataStore.langFlow.first()
        
        val json = cache.readJson("all.json")
        val api = HoraApiService.create()
        val repo = HoraRepository(api, context)
        val state = if (json != null) repo.parsePanchangaFromJson(json) else null

        provideContent {
            GlanceTheme {
                HoraWidgetContent(state, lang)
            }
        }
    }

    @Composable
    private fun HoraWidgetContent(state: com.hora.companion.ui.screens.PanchangaState?, lang: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(12.dp)
        ) {
            if (state == null) {
                Text("Unable to Load", style = TextStyle(color = GlanceTheme.colors.onSurface))
            } else {
                // Header
                Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${state.horaSymbol} ${state.hora}",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface)
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = state.remaining,
                        style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant)
                    )
                }

                Spacer(modifier = GlanceModifier.height(2.dp))

                // Sub-header
                val endsLabel = TranslationUtils.translate("Ends", lang)
                val nextLabel = TranslationUtils.translate("Next", lang)
                Text(
                    text = "$endsLabel ${state.horaEnds}   •   $nextLabel ${state.horaNext}",
                    style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant)
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Three Columns Grid
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    // Column 1
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        WidgetInfoItem(TranslationUtils.translate("Ayana", lang), state.ayana)
                        Spacer(modifier = GlanceModifier.height(5.dp))
                        WidgetInfoItem(TranslationUtils.translate("Rutu", lang), state.rutu)
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        WidgetInfoItem(TranslationUtils.translate("Masa", lang), state.masa)
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // Column 2
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        WidgetInfoItem(TranslationUtils.translate("Tithi", lang), state.tithi)
                        Spacer(modifier = GlanceModifier.height(5.dp))
                        WidgetInfoItem(TranslationUtils.translate("Nakshatra", lang), state.nakshatra)
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        WidgetInfoItem(TranslationUtils.translate("Sunrise", lang), "${state.sunrise} - ${state.sunset}")
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // Column 3
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        WidgetInfoItem(TranslationUtils.translate("Rahu Kalam", lang), state.rahuKalam)
                        Spacer(modifier = GlanceModifier.height(5.dp))
                        WidgetInfoItem(TranslationUtils.translate("Yamaganda", lang), state.yamaganda)
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        WidgetInfoItem(TranslationUtils.translate("Abhijit", lang), state.abhijit)
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Footer
                Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌙 ${state.moonRasi}",
                        style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "☀️ ${state.sunRasi}",
                        style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
                    )
                }
            }
        }
    }

    @Composable
    private fun WidgetInfoItem(label: String, value: String) {
        Column {
            Text(
                text = label.uppercase(), 
                style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.secondary)
            )
            Text(
                text = value, 
                style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurface),
                maxLines = 1
            )
        }
    }
}

class HoraWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HoraWidget()
}

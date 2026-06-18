package cn.waijade.nexuswid.widget

import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.state.GlanceStateDefinition

object ContributionAppWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*>? = null

    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) {
    }
}

class ContributionWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ContributionAppWidget
}

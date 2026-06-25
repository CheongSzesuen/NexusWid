@file:OptIn(ExperimentalMaterial3Api::class)

package cn.waijade.nexuswid.ui.settingsScreen.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.waijade.nexuswid.data.HeatmapColorMode
import cn.waijade.nexuswid.data.afdian.AfdianDailyStat
import cn.waijade.nexuswid.data.afdian.AfdianMonthlyIncome
import cn.waijade.nexuswid.data.afdian.AfdianProductSummary
import cn.waijade.nexuswid.data.afdian.AfdianRandomCreator
import cn.waijade.nexuswid.data.afdian.AfdianTopSponsor
import kotlin.math.abs

// =============================================================================
// Formatting helpers (matching Glance widget logic)
// =============================================================================

private fun formatAmount(amount: Double): String {
    return when {
        amount >= 1_000_000 -> {
            val v = amount / 1_000_000
            if (v >= 10) "${v.toInt()}M" else String.format("%.1fM", v)
        }
        amount >= 10_000 -> {
            val v = amount / 1_000
            if (v >= 10) "${v.toInt()}k" else String.format("%.1fk", v)
        }
        else -> String.format("%.2f", amount)
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 10_000 -> "${count / 10_000}w"
        count >= 1_000 -> "${count / 1_000}k"
        else -> count.toString()
    }
}

// =============================================================================
// Shared color helpers
// =============================================================================

private data class AfdianCardColors(
    val bgColor: Color,
    val textPrimary: Color,
    val grayText: Color,
    val accentColor: Color
)

@Composable
private fun afdianCardColors(
    colorMode: HeatmapColorMode,
    accentColor: Color = Color(0xFF946CE6)
): AfdianCardColors {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (colorMode) {
        HeatmapColorMode.SYSTEM -> systemDark
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
    }
    return AfdianCardColors(
        bgColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF6F8FA),
        textPrimary = if (isDark) Color.White else Color(0xFF1F2328),
        grayText = if (isDark) Color(0xFF8B949E) else Color(0xFF656D76),
        accentColor = accentColor
    )
}

@Composable
private fun afdianIcon(isDark: Boolean) = painterResource(
    if (isDark) cn.waijade.nexuswid.R.drawable.ic_afdian_dark
    else cn.waijade.nexuswid.R.drawable.ic_afdian
)

// =============================================================================
// 1. Afdian Total Earnings Preview Card
// =============================================================================

@Composable
fun AfdianTotalEarningsPreviewCard(
    colorMode: HeatmapColorMode,
    totalEarnings: Double? = null,
    modifier: Modifier = Modifier
) {
    val colors = afdianCardColors(colorMode)
    val isDark = when (colorMode) {
        HeatmapColorMode.SYSTEM -> isSystemInDarkTheme()
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.bgColor
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val contentEdge =
                maxHeight.coerceAtMost(maxWidth) - 14.dp * 2
            val iconSize = (contentEdge * 0.13f).coerceIn(12.dp, 22.dp)
            val amountText = totalEarnings?.let { formatAmount(it) } ?: "-"
            val maxByHeight = contentEdge * 0.42f
            val maxByWidth = contentEdge / (amountText.length * 0.55f + 0.5f)
            val amountSize =
                maxByHeight.coerceAtMost(maxByWidth).coerceIn(10.dp, 48.dp)
            val labelSize = (contentEdge * 0.12f).coerceIn(8.dp, 14.dp)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Top: icon + label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = afdianIcon(isDark),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "爱发电",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Center: big amount
                Text(
                    text = if (totalEarnings != null) "¥$amountText" else "-",
                    color = colors.accentColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = amountSize.value.sp,
                    maxLines = 1
                )

                // Bottom: label
                Text(
                    text = "总收益",
                    color = colors.grayText,
                    fontSize = labelSize.value.sp
                )
            }
        }
    }
}

// =============================================================================
// 2. Afdian Monthly Earnings Preview Card
// =============================================================================

@Composable
fun AfdianMonthlyEarningsPreviewCard(
    colorMode: HeatmapColorMode,
    monthlyEarnings: Double? = null,
    modifier: Modifier = Modifier
) {
    val colors = afdianCardColors(colorMode)
    val isDark = when (colorMode) {
        HeatmapColorMode.SYSTEM -> isSystemInDarkTheme()
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.bgColor
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val contentEdge =
                maxHeight.coerceAtMost(maxWidth) - 14.dp * 2
            val iconSize = (contentEdge * 0.13f).coerceIn(12.dp, 22.dp)
            val amountText = monthlyEarnings?.let { formatAmount(it) } ?: "-"
            val maxByHeight = contentEdge * 0.42f
            val maxByWidth = contentEdge / (amountText.length * 0.55f + 0.5f)
            val amountSize =
                maxByHeight.coerceAtMost(maxByWidth).coerceIn(10.dp, 48.dp)
            val labelSize = (contentEdge * 0.12f).coerceIn(8.dp, 14.dp)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = afdianIcon(isDark),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "爱发电",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = if (monthlyEarnings != null) "¥$amountText" else "-",
                    color = colors.accentColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = amountSize.value.sp,
                    maxLines = 1
                )

                Text(
                    text = "本月收益",
                    color = colors.grayText,
                    fontSize = labelSize.value.sp
                )
            }
        }
    }
}

// =============================================================================
// 3. Afdian Unread Preview Card
// =============================================================================

@Composable
fun AfdianUnreadPreviewCard(
    colorMode: HeatmapColorMode,
    unreadCount: Int? = null,
    modifier: Modifier = Modifier
) {
    val accentRed = Color(0xFFE74C3C)
    val colors = afdianCardColors(colorMode, accentRed)
    val isDark = when (colorMode) {
        HeatmapColorMode.SYSTEM -> isSystemInDarkTheme()
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.bgColor
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val contentEdge =
                maxHeight.coerceAtMost(maxWidth) - 14.dp * 2
            val iconSize = (contentEdge * 0.13f).coerceIn(12.dp, 22.dp)
            val countText = unreadCount?.let { formatCount(it) } ?: "-"
            val maxByHeight = contentEdge * 0.42f
            val maxByWidth = contentEdge / (countText.length * 0.55f + 0.5f)
            val countSize =
                maxByHeight.coerceAtMost(maxByWidth).coerceIn(10.dp, 48.dp)
            val labelSize = (contentEdge * 0.12f).coerceIn(8.dp, 14.dp)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = afdianIcon(isDark),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "消息",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = if (unreadCount != null) countText else "-",
                    color = accentRed,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = countSize.value.sp,
                    maxLines = 1
                )

                Text(
                    text = "未读数",
                    color = colors.grayText,
                    fontSize = labelSize.value.sp
                )
            }
        }
    }
}

// =============================================================================
// 4. Afdian Complaint Preview Card
// =============================================================================

@Composable
fun AfdianComplaintPreviewCard(
    colorMode: HeatmapColorMode,
    complaintCount: Int? = null,
    modifier: Modifier = Modifier
) {
    val accentCyan = Color(0xFF03D3EE)
    val colors = afdianCardColors(colorMode, accentCyan)
    val isDark = when (colorMode) {
        HeatmapColorMode.SYSTEM -> isSystemInDarkTheme()
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.bgColor
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val contentEdge =
                maxHeight.coerceAtMost(maxWidth) - 14.dp * 2
            val iconSize = (contentEdge * 0.13f).coerceIn(12.dp, 22.dp)
            val countText = complaintCount?.let { formatCount(it) } ?: "-"
            val maxByHeight = contentEdge * 0.42f
            val maxByWidth = contentEdge / (countText.length * 0.55f + 0.5f)
            val countSize =
                maxByHeight.coerceAtMost(maxByWidth).coerceIn(10.dp, 48.dp)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = afdianIcon(isDark),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "未读投诉",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = if (complaintCount != null) countText else "-",
                    color = accentCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = countSize.value.sp,
                    maxLines = 1
                )
            }
        }
    }
}

// =============================================================================
// 5. Afdian Product Preview Card
// =============================================================================

@Composable
fun AfdianProductPreviewCard(
    colorMode: HeatmapColorMode,
    productData: AfdianProductSummary? = null,
    modifier: Modifier = Modifier
) {
    val colors = afdianCardColors(colorMode)
    val isDark = when (colorMode) {
        HeatmapColorMode.SYSTEM -> isSystemInDarkTheme()
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
    }
    val dividerColor =
        if (isDark) Color(0xFF30363D) else Color(0xFFD0D7DE)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.bgColor
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val isCompact = maxWidth < 200.dp
            Column(modifier = Modifier.fillMaxSize()) {
                // Top row: icon + "商品收益"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = afdianIcon(isDark),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "商品收益",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (productData != null) {
                    // Product name + price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = productData.name,
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "¥${productData.price}",
                                color = colors.accentColor,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(dividerColor)
                    )

                    Spacer(Modifier.height(8.dp))

                    // Stats row: 收入 | 销量 | 利润
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val profit =
                            productData.totalAmount * 0.94 // minus 6% platform fee
                        AfdianStatItem(
                            label = "收入",
                            value = "¥${formatAmount(productData.totalAmount)}",
                            accentColor = colors.accentColor,
                            grayText = colors.grayText
                        )
                        AfdianStatItem(
                            label = "销量",
                            value = "${productData.sponsorCount}",
                            accentColor = colors.accentColor,
                            grayText = colors.grayText
                        )
                        AfdianStatItem(
                            label = "利润",
                            value = "¥${formatAmount(profit)}",
                            accentColor = colors.accentColor,
                            grayText = colors.grayText
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "选择商品",
                            color = colors.grayText,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AfdianStatItem(
    label: String,
    value: String,
    accentColor: Color,
    grayText: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = grayText,
            fontSize = 10.sp
        )
    }
}

// =============================================================================
// 6. Afdian Daily Earnings Chart Preview Card
// =============================================================================

@Composable
fun AfdianDailyEarningsChartPreviewCard(
    colorMode: HeatmapColorMode,
    dailyStats: List<AfdianDailyStat> = emptyList(),
    modifier: Modifier = Modifier
) {
    val colors = afdianCardColors(colorMode)
    val isDark = when (colorMode) {
        HeatmapColorMode.SYSTEM -> isSystemInDarkTheme()
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
    }
    val chartLineColor = Color(0xFF946CE6)
    val chartGridColor =
        if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
    val chartLabelColor =
        if (isDark) Color(0xFF8B949E) else Color(0xFF656D76)
    val density = LocalDensity.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.bgColor
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "本月收入",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    if (dailyStats.isNotEmpty()) {
                        val total = dailyStats.sumOf {
                            it.paid_order_real_amount.toDoubleOrNull() ?: 0.0
                        }
                        Text(
                            text = "¥${formatAmount(total)}",
                            color = colors.accentColor,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Chart area
                if (dailyStats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无数据",
                            color = colors.grayText,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val chartWidth = size.width
                        val chartHeight = size.height
                        val labelTextSize = with(density) { 7.sp.toPx() }

                        val amounts = dailyStats.map {
                            it.paid_order_real_amount.toDoubleOrNull() ?: 0.0
                        }
                        val maxAmount = amounts.maxOrNull()?.let {
                            if (it <= 0) 1.0 else it * 1.15
                        } ?: 1.0
                        val minAmount = 0.0

                        val leftPadding = 32f
                        val bottomPadding = 18f
                        val topPadding = 4f
                        val plotWidth = chartWidth - leftPadding - 4f
                        val plotHeight = chartHeight - bottomPadding - topPadding

                        // Y-axis grid lines + labels (4 lines)
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val fraction = i.toFloat() / gridLines
                            val y = topPadding + plotHeight * (1f - fraction)
                            val value = minAmount + (maxAmount - minAmount) * fraction

                            // Grid line
                            drawLine(
                                color = chartGridColor,
                                start = Offset(leftPadding, y),
                                end = Offset(chartWidth - 4f, y),
                                strokeWidth = 1f
                            )

                            // Y label
                            drawContext.canvas.nativeCanvas.drawText(
                                formatAmount(value),
                                leftPadding - 2f,
                                y + labelTextSize / 3f,
                                android.graphics.Paint().apply {
                                    color = chartLabelColor.hashCode()
                                    textSize = labelTextSize
                                    textAlign =
                                        android.graphics.Paint.Align.RIGHT
                                    isAntiAlias = true
                                }
                            )
                        }

                        // X-axis labels
                        val step = when {
                            dailyStats.size <= 7 -> 1
                            dailyStats.size <= 15 -> 2
                            else -> 3
                        }
                        for (i in dailyStats.indices step step) {
                            val x = leftPadding + plotWidth * i / (dailyStats.size - 1).coerceAtLeast(1)
                            val dateStr = dailyStats[i].date_str.toString()
                            val label = if (dateStr.length >= 4) {
                                "${dateStr.substring(dateStr.length - 4, dateStr.length - 2)}/${dateStr.substring(dateStr.length - 2)}"
                            } else dateStr

                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                x,
                                chartHeight - 2f,
                                android.graphics.Paint().apply {
                                    color = chartLabelColor.hashCode()
                                    textSize = labelTextSize
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                }
                            )
                        }

                        // Line chart
                        if (amounts.size >= 2) {
                            val points = amounts.mapIndexed { index, amount ->
                                val x =
                                    leftPadding + plotWidth * index / (amounts.size - 1).coerceAtLeast(1)
                                val fraction =
                                    ((amount - minAmount) / (maxAmount - minAmount)).toFloat()
                                        .coerceIn(0f, 1f)
                                val y = topPadding + plotHeight * (1f - fraction)
                                Offset(x, y)
                            }

                            // Gradient fill
                            val fillPath = Path().apply {
                                moveTo(points.first().x, topPadding + plotHeight)
                                points.forEach { lineTo(it.x, it.y) }
                                lineTo(points.last().x, topPadding + plotHeight)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        chartLineColor.copy(alpha = 0.3f),
                                        chartLineColor.copy(alpha = 0.0f)
                                    ),
                                    startY = topPadding,
                                    endY = topPadding + plotHeight
                                )
                            )

                            // Smooth line
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val cp1x = p0.x + (p1.x - p0.x) / 3f
                                    val cp2x = p0.x + (p1.x - p0.x) * 2f / 3f
                                    cubicTo(cp1x, p0.y, cp2x, p1.y, p1.x, p1.y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = chartLineColor,
                                style = Stroke(width = 2f)
                            )

                            // Data points
                            val pointRadius = 2.5f
                            points.forEach { point ->
                                drawCircle(
                                    color = chartLineColor,
                                    radius = pointRadius,
                                    center = point
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 7. Afdian Monthly Earnings Chart Preview Card
// =============================================================================

@Composable
fun AfdianMonthlyEarningsChartPreviewCard(
    colorMode: HeatmapColorMode,
    monthlyIncomes: List<AfdianMonthlyIncome> = emptyList(),
    modifier: Modifier = Modifier
) {
    val colors = afdianCardColors(colorMode)
    val isDark = when (colorMode) {
        HeatmapColorMode.SYSTEM -> isSystemInDarkTheme()
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
    }
    val chartLineColor = Color(0xFF946CE6)
    val chartGridColor =
        if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
    val chartLabelColor =
        if (isDark) Color(0xFF8B949E) else Color(0xFF656D76)
    val density = LocalDensity.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.bgColor
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "每月汇总",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    if (monthlyIncomes.isNotEmpty()) {
                        val total = monthlyIncomes.sumOf { it.creatorAmount }
                        Text(
                            text = "¥${formatAmount(total)}",
                            color = colors.accentColor,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                if (monthlyIncomes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无数据",
                            color = colors.grayText,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val chartWidth = size.width
                        val chartHeight = size.height
                        val labelTextSize = with(density) { 7.sp.toPx() }

                        val amounts = monthlyIncomes.map { it.creatorAmount }
                        val maxAmount = amounts.maxOrNull()?.let {
                            if (it <= 0) 1.0 else it * 1.15
                        } ?: 1.0

                        val leftPadding = 32f
                        val bottomPadding = 18f
                        val topPadding = 4f
                        val plotWidth = chartWidth - leftPadding - 4f
                        val plotHeight = chartHeight - bottomPadding - topPadding

                        // Y-axis grid lines + labels
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val fraction = i.toFloat() / gridLines
                            val y = topPadding + plotHeight * (1f - fraction)
                            val value = maxAmount * fraction

                            drawLine(
                                color = chartGridColor,
                                start = Offset(leftPadding, y),
                                end = Offset(chartWidth - 4f, y),
                                strokeWidth = 1f
                            )

                            drawContext.canvas.nativeCanvas.drawText(
                                formatAmount(value),
                                leftPadding - 2f,
                                y + labelTextSize / 3f,
                                android.graphics.Paint().apply {
                                    color = chartLabelColor.hashCode()
                                    textSize = labelTextSize
                                    textAlign =
                                        android.graphics.Paint.Align.RIGHT
                                    isAntiAlias = true
                                }
                            )
                        }

                        // X-axis labels
                        val step = when {
                            monthlyIncomes.size <= 6 -> 1
                            monthlyIncomes.size <= 12 -> 2
                            else -> 3
                        }
                        for (i in monthlyIncomes.indices step step) {
                            val x = leftPadding + plotWidth * i / (monthlyIncomes.size - 1).coerceAtLeast(1)
                            val label = "${monthlyIncomes[i].month}月"

                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                x,
                                chartHeight - 2f,
                                android.graphics.Paint().apply {
                                    color = chartLabelColor.hashCode()
                                    textSize = labelTextSize
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                }
                            )
                        }

                        // Line chart
                        if (amounts.size >= 2) {
                            val points = amounts.mapIndexed { index, amount ->
                                val x =
                                    leftPadding + plotWidth * index / (amounts.size - 1).coerceAtLeast(1)
                                val fraction =
                                    (amount / maxAmount).toFloat().coerceIn(0f, 1f)
                                val y = topPadding + plotHeight * (1f - fraction)
                                Offset(x, y)
                            }

                            // Gradient fill
                            val fillPath = Path().apply {
                                moveTo(points.first().x, topPadding + plotHeight)
                                points.forEach { lineTo(it.x, it.y) }
                                lineTo(points.last().x, topPadding + plotHeight)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        chartLineColor.copy(alpha = 0.3f),
                                        chartLineColor.copy(alpha = 0.0f)
                                    ),
                                    startY = topPadding,
                                    endY = topPadding + plotHeight
                                )
                            )

                            // Smooth line
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val cp1x = p0.x + (p1.x - p0.x) / 3f
                                    val cp2x = p0.x + (p1.x - p0.x) * 2f / 3f
                                    cubicTo(cp1x, p0.y, cp2x, p1.y, p1.x, p1.y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = chartLineColor,
                                style = Stroke(width = 2f)
                            )

                            // Data points
                            val pointRadius = 2.5f
                            points.forEach { point ->
                                drawCircle(
                                    color = chartLineColor,
                                    radius = pointRadius,
                                    center = point
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 8. Afdian Random Creator Preview Card
// =============================================================================

@Composable
fun AfdianRandomCreatorPreviewCard(
    colorMode: HeatmapColorMode,
    randomCreator: AfdianRandomCreator? = null,
    modifier: Modifier = Modifier
) {
    val colors = afdianCardColors(colorMode)
    val isDark = when (colorMode) {
        HeatmapColorMode.SYSTEM -> isSystemInDarkTheme()
        HeatmapColorMode.LIGHT -> false
        HeatmapColorMode.DARK -> true
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.bgColor
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val isCompact = maxWidth < 200.dp
            Column(modifier = Modifier.fillMaxSize()) {
                if (randomCreator != null) {
                    val avatarSize = if (isCompact) 40.dp else 48.dp
                    val nameSize = if (isCompact) 14.sp else 15.sp
                    val descSize = if (isCompact) 10.sp else 11.sp

                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "发现创作者",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "@${randomCreator.urlSlug}",
                            color = colors.accentColor,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Avatar placeholder + name
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Placeholder avatar circle
                        Box(
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(colors.accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = randomCreator.name.take(1),
                                color = colors.accentColor,
                                fontSize = nameSize,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = randomCreator.name,
                                    color = colors.textPrimary,
                                    fontSize = nameSize,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (randomCreator.isVerified) {
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "✓",
                                        color = colors.accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = randomCreator.categoryName,
                                color = colors.grayText,
                                fontSize = descSize,
                                maxLines = 1
                            )
                            if (randomCreator.doing.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = randomCreator.doing,
                                    color = colors.textPrimary,
                                    fontSize = descSize,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Bottom hint
                    Text(
                        text = "点击刷新",
                        color = colors.grayText.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                } else {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "加载中",
                            color = colors.grayText,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 9. Afdian Top Sponsors Preview Card
// =============================================================================

@Composable
fun AfdianTopSponsorsPreviewCard(
    colorMode: HeatmapColorMode,
    topSponsors: List<AfdianTopSponsor> = emptyList(),
    modifier: Modifier = Modifier
) {
    val colors = afdianCardColors(colorMode)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.bgColor
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val isCompact = maxWidth < 200.dp
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Text(
                    text = "赞助月榜",
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))

                if (topSponsors.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无赞助者",
                            color = colors.grayText,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    val avatarSize = if (isCompact) 32.dp else 40.dp
                    val nameSize = if (isCompact) 9.sp else 10.sp

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        topSponsors.take(3).forEachIndexed { index, sponsor ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Placeholder avatar
                                Box(
                                    modifier = Modifier
                                        .size(avatarSize)
                                        .clip(RoundedCornerShape(avatarSize * 0.5f))
                                        .background(colors.accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sponsor.name.take(1),
                                        color = colors.accentColor,
                                        fontSize = nameSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${index + 1} ${sponsor.name}",
                                    color = colors.textPrimary,
                                    fontSize = nameSize,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.tanilink.cekkabar.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanilink.cekkabar.data.model.InfluentialWord
import com.tanilink.cekkabar.data.model.TokenItem
import com.tanilink.cekkabar.ui.theme.FactGreen
import com.tanilink.cekkabar.ui.theme.FactGreenContainer
import com.tanilink.cekkabar.ui.theme.HoaxRed
import com.tanilink.cekkabar.ui.theme.HoaxRedContainer
import com.tanilink.cekkabar.ui.theme.IndigoPrimary
import com.tanilink.cekkabar.ui.theme.NeutralChipBg
import com.tanilink.cekkabar.ui.theme.OnFactGreenContainer
import com.tanilink.cekkabar.ui.theme.OnHoaxRedContainer
import com.tanilink.cekkabar.ui.theme.OnSurfaceDark
import com.tanilink.cekkabar.ui.theme.OnSurfaceVariant
import com.tanilink.cekkabar.ui.theme.OutlineBorder
import com.tanilink.cekkabar.ui.theme.PrimaryContainer
import com.tanilink.cekkabar.ui.theme.SurfaceContainerHigh
import com.tanilink.cekkabar.ui.theme.SurfaceWhite
import kotlin.math.abs

@Composable
fun CekKabarTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IndigoPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = "CekKabar Logo",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "CekKabar",
                    style = MaterialTheme.typography.titleLarge,
                    color = IndigoPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Deteksi Hoaks Berbasis AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
        Surface(
            color = PrimaryContainer,
            shape = RoundedCornerShape(999.dp)
        ) {
            Text(
                text = "NB + TF-IDF",
                style = MaterialTheme.typography.labelSmall,
                color = IndigoPrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
fun EditorialCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    containerColor: Color = SurfaceWhite,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, OutlineBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
fun VerdictBadge(
    verdict: String,
    probability: Double? = null,
    modifier: Modifier = Modifier
) {
    val isHoax = verdict.uppercase() == "HOAKS"
    val bgColor = if (isHoax) HoaxRedContainer else FactGreenContainer
    val textColor = if (isHoax) OnHoaxRedContainer else OnFactGreenContainer
    val iconColor = if (isHoax) HoaxRed else FactGreen
    val label = if (probability != null) "$verdict ${probability.toInt()}%" else verdict

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isHoax) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SemiCircularGauge(
    percentage: Double,
    verdict: String,
    modifier: Modifier = Modifier
) {
    val isHoax = verdict.uppercase() == "HOAKS"
    val activeColor = if (isHoax) HoaxRed else FactGreen
    val animatedSweep by animateFloatAsState(
        targetValue = (percentage.toFloat().coerceIn(0f, 100f) / 100f) * 180f,
        animationSpec = tween(durationMillis = 800),
        label = "gauge_sweep"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(155.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(
            modifier = Modifier
                .width(230.dp)
                .height(130.dp)
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        ) {
            val strokeWidth = 16.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, (size.width - strokeWidth))
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Track
            drawArc(
                color = SurfaceContainerHigh,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Arc
            drawArc(
                color = activeColor,
                startAngle = 180f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.displayLarge,
                color = activeColor
            )
            Text(
                text = verdict.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = activeColor,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HighlightedTokensBox(
    tokens: List<TokenItem>,
    selectedToken: TokenItem?,
    onTokenClick: (TokenItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundCardTint)
            .padding(14.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tokens.forEach { token ->
                val isSelected = selectedToken?.clean == token.clean && token.isWord
                val (bg, fg) = when (token.sentiment) {
                    "hoaks" -> HoaxRedContainer to OnHoaxRedContainer
                    "fakta" -> FactGreenContainer to OnFactGreenContainer
                    else -> if (token.isWord && abs(token.score) > 0.01) {
                        PrimaryContainer to IndigoPrimary
                    } else {
                        Color.Transparent to OnSurfaceDark
                    }
                }

                if (bg == Color.Transparent) {
                    Text(
                        text = token.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = fg,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .then(
                                if (isSelected) {
                                    Modifier.border(1.5.dp, IndigoPrimary, RoundedCornerShape(8.dp))
                                } else Modifier
                            )
                            .clickable(enabled = token.isWord) { onTokenClick(token) }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = token.text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = fg
                        )
                    }
                }
            }
        }
    }
}

private val BackgroundCardTint = Color(0xFFF6F8FD)

@Composable
fun DualDirectionBarChart(
    words: List<InfluentialWord>,
    modifier: Modifier = Modifier
) {
    if (words.isEmpty()) return
    val maxAbs = words.maxOf { abs(it.score) }.coerceAtLeast(1.0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "← Menuju Fakta (-)",
                style = MaterialTheme.typography.labelSmall,
                color = FactGreen
            )
            Text(
                text = "Menuju Hoaks (+) →",
                style = MaterialTheme.typography.labelSmall,
                color = HoaxRed
            )
        }

        words.forEach { item ->
            val ratio = (abs(item.score) / maxAbs).toFloat().coerceIn(0.15f, 1f)
            val isHoax = item.score >= 0
            val barColor = if (isHoax) HoaxRed else FactGreen
            val scoreText = if (isHoax) "+${item.score}" else "${item.score}"

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side (Fakta)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (!isHoax) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .clip(RoundedCornerShape(6.dp))
                                .background(barColor)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "$scoreText ${item.word}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Center axis
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .width(2.dp)
                        .height(26.dp)
                        .background(OutlineBorder)
                )

                // Right side (Hoaks)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isHoax) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .clip(RoundedCornerShape(6.dp))
                                .background(barColor)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${item.word} $scoreText",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Sumbu 0.00 mewakili kata berdistribusi netral",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MetricRingCard(
    title: String,
    value: Double,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    EditorialCard(modifier = modifier, cornerRadius = 18.dp) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${value}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurfaceDark,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(42.dp)) {
                        drawArc(
                            color = SurfaceContainerHigh,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = accentColor,
                            startAngle = -90f,
                            sweepAngle = (value.toFloat() / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${value.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = accentColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = SurfaceContainerHigh,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }
    }
}


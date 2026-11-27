package com.tanilink.cekkabar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tanilink.cekkabar.ui.components.CekKabarTopBar
import com.tanilink.cekkabar.ui.components.EditorialCard
import com.tanilink.cekkabar.ui.components.ErrorRetryCard
import com.tanilink.cekkabar.ui.components.MetricRingCard
import com.tanilink.cekkabar.ui.theme.FactGreen
import com.tanilink.cekkabar.ui.theme.FactGreenContainer
import com.tanilink.cekkabar.ui.theme.HoaxRed
import com.tanilink.cekkabar.ui.theme.HoaxRedContainer
import com.tanilink.cekkabar.ui.theme.IndigoPrimary
import com.tanilink.cekkabar.ui.theme.OnFactGreenContainer
import com.tanilink.cekkabar.ui.theme.OnHoaxRedContainer
import com.tanilink.cekkabar.ui.theme.OnSurfaceDark
import com.tanilink.cekkabar.ui.theme.OnSurfaceVariant
import com.tanilink.cekkabar.ui.theme.PrimaryContainer
import com.tanilink.cekkabar.ui.theme.SurfaceContainerHigh
import com.tanilink.cekkabar.ui.theme.SurfaceContainerLow
import com.tanilink.cekkabar.ui.viewmodel.CekKabarUiState

@Composable
fun EvaluasiScreen(
    uiState: CekKabarUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        if (uiState.evaluation == null && !uiState.isLoadingEvaluation) {
            onRetry()
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        CekKabarTopBar()

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Evaluasi Model",
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurfaceDark
                    )
                    Text(
                        text = "Kinerja klasifikasi Naive Bayes pada data uji",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
                Surface(color = FactGreenContainer, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        text = "● Valid",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnFactGreenContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            val ev = uiState.evaluation
            if (uiState.isLoadingEvaluation && ev == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IndigoPrimary)
                }
            } else if (ev != null) {
                Surface(
                    color = SurfaceContainerLow,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TF-IDF + MultinomialNB • Test Split 20% (${ev.nTest} Data Uji)",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                // 2x2 Metric Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricRingCard(
                        title = "Akurasi",
                        value = ev.accuracy,
                        subtitle = "CV F1: ${ev.cvF1Mean}%",
                        accentColor = IndigoPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricRingCard(
                        title = "Presisi",
                        value = ev.precision,
                        subtitle = "Positif Benar",
                        accentColor = FactGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricRingCard(
                        title = "Recall",
                        value = ev.recall,
                        subtitle = "Sensitivitas Kuat",
                        accentColor = IndigoPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricRingCard(
                        title = "F1-Score",
                        value = ev.f1Score,
                        subtitle = "Harmonis Optimal",
                        accentColor = HoaxRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Confusion Matrix Card
                val cm = ev.confusionMatrix
                val total = cm.total.coerceAtLeast(1)
                val correctPct = ((cm.tp + cm.tn) * 100.0 / total)
                val wrongPct = ((cm.fp + cm.fn) * 100.0 / total)

                EditorialCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Matriks Konfusi (Confusion Matrix)",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceDark
                        )
                        Text(
                            text = "Aktual (Baris) vs Prediksi (Kolom)",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Spacer(modifier = Modifier.width(64.dp))
                            Text(
                                text = "Pred. Hoaks",
                                style = MaterialTheme.typography.labelSmall,
                                color = HoaxRed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Pred. Fakta",
                                style = MaterialTheme.typography.labelSmall,
                                color = FactGreen,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 1: Aktual Hoaks
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.width(64.dp)) {
                                Text("AKTUAL", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text("Hoaks", style = MaterialTheme.typography.labelMedium, color = HoaxRed)
                            }
                            MatrixCell(
                                label = "TP (BENAR)",
                                value = cm.tp,
                                pct = cm.tp * 100.0 / total,
                                isCorrect = true,
                                modifier = Modifier.weight(1f)
                            )
                            MatrixCell(
                                label = "FN (MISS)",
                                value = cm.fn,
                                pct = cm.fn * 100.0 / total,
                                isCorrect = false,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 2: Aktual Fakta
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.width(64.dp)) {
                                Text("AKTUAL", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text("Fakta", style = MaterialTheme.typography.labelMedium, color = FactGreen)
                            }
                            MatrixCell(
                                label = "FP (SALAH)",
                                value = cm.fp,
                                pct = cm.fp * 100.0 / total,
                                isCorrect = false,
                                modifier = Modifier.weight(1f)
                            )
                            MatrixCell(
                                label = "TN (BENAR)",
                                value = cm.tn,
                                pct = cm.tn * 100.0 / total,
                                isCorrect = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "● Klasifikasi Benar: ${"%.1f".format(correctPct)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = FactGreen
                            )
                            Text(
                                text = "● Salah Prediksi: ${"%.1f".format(wrongPct)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = HoaxRed
                            )
                        }
                    }
                }

                // Performa per Kelas
                EditorialCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Performa per Kelas",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceDark
                        )
                        Text(
                            text = "Komparasi metrik evaluasi per kategori klaim",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        ClassPerformanceBlock(
                            title = "Kelas Hoaks",
                            f1 = ev.perClass.hoaks.f1,
                            precision = ev.perClass.hoaks.precision,
                            recall = ev.perClass.hoaks.recall,
                            accentColor = HoaxRed,
                            badgeBg = HoaxRedContainer,
                            badgeFg = OnHoaxRedContainer
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        ClassPerformanceBlock(
                            title = "Kelas Fakta",
                            f1 = ev.perClass.fakta.f1,
                            precision = ev.perClass.fakta.precision,
                            recall = ev.perClass.fakta.recall,
                            accentColor = FactGreen,
                            badgeBg = FactGreenContainer,
                            badgeFg = OnFactGreenContainer
                        )
                    }
                }

                // Spesifikasi Korpus
                EditorialCard(cornerRadius = 20.dp, containerColor = SurfaceContainerLow) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Spesifikasi Korpus Data",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurfaceDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Dataset gabungan TurnBackHoax.id (Mafindo), AntaraNews, Kompas, dan Detik sebanyak ${ev.nTrain + ev.nTest} berita seimbang (${ev.nVocab} fitur TF-IDF).",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = PrimaryContainer, shape = RoundedCornerShape(999.dp)) {
                                Text(
                                    text = "5-Fold CV F1: ${ev.cvF1Mean}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IndigoPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Surface(color = FactGreenContainer, shape = RoundedCornerShape(999.dp)) {
                                Text(
                                    text = "Train: ${ev.nTrain}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnFactGreenContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                ErrorRetryCard(
                    title = "Data Evaluasi Belum Tersedia",
                    message = uiState.evaluationError ?: "Pastikan server backend aktif dan terhubung ke jaringan WiFi yang sama.",
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
private fun MatrixCell(
    label: String,
    value: Int,
    pct: Double,
    isCorrect: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isCorrect) FactGreenContainer else HoaxRedContainer
    val fg = if (isCorrect) OnFactGreenContainer else OnHoaxRedContainer

    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = fg)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleLarge,
                color = fg,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "${"%.1f".format(pct)}%",
                style = MaterialTheme.typography.bodySmall,
                color = fg
            )
        }
    }
}

@Composable
private fun ClassPerformanceBlock(
    title: String,
    f1: Double,
    precision: Double,
    recall: Double,
    accentColor: Color,
    badgeBg: Color,
    badgeFg: Color
) {
    Surface(
        color = SurfaceContainerLow,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurfaceDark
                    )
                }
                Surface(color = badgeBg, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        text = "F1: ${f1}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            ProgressMetricRow("Presisi", precision, accentColor)
            Spacer(modifier = Modifier.height(8.dp))
            ProgressMetricRow("Recall", recall, accentColor)
        }
    }
}

@Composable
private fun ProgressMetricRow(label: String, pct: Double, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            Text(text = "$pct%", style = MaterialTheme.typography.labelMedium, color = OnSurfaceDark)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(SurfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((pct.toFloat() / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(color)
            )
        }
    }
}


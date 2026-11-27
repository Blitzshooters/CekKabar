package com.tanilink.cekkabar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tanilink.cekkabar.data.model.WordScoreItem
import com.tanilink.cekkabar.ui.components.CekKabarTopBar
import com.tanilink.cekkabar.ui.components.EditorialCard
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
import com.tanilink.cekkabar.ui.theme.SurfaceWhite
import com.tanilink.cekkabar.ui.viewmodel.CekKabarUiState
import kotlin.math.abs

@Composable
fun AnalisisScreen(
    uiState: CekKabarUiState,
    onToggleMode: (Boolean) -> Unit,
    onGoToDeteksi: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFormula by remember { mutableStateOf(false) }

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
                Surface(color = PrimaryContainer, shape = RoundedCornerShape(999.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "XAI EXPLAINABILITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = IndigoPrimary
                        )
                    }
                }
                Surface(color = FactGreenContainer, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        text = "● Model Online",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnFactGreenContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "Analisis Model",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurfaceDark
                )
                Text(
                    text = "Transparansi probabilitas & bobot kata Naive Bayes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            // Segmented Toggle: Global vs Teks Terakhir
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(SurfaceContainerHigh)
                    .padding(4.dp)
            ) {
                SegmentedTabItem(
                    label = "Global (Korpus)",
                    icon = Icons.Rounded.Public,
                    selected = uiState.analysisModeGlobal,
                    onClick = { onToggleMode(true) },
                    modifier = Modifier.weight(1f)
                )
                SegmentedTabItem(
                    label = "Teks Terakhir",
                    icon = Icons.Rounded.Subject,
                    selected = !uiState.analysisModeGlobal,
                    onClick = { onToggleMode(false) },
                    modifier = Modifier.weight(1f)
                )
            }

            val analysis = uiState.analysis
            if (uiState.isLoadingAnalysis && analysis == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IndigoPrimary)
                }
            } else if (analysis != null) {
                // 3 Stat Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMiniCard(
                        label = "Kosa Kata",
                        value = "${analysis.vocabSize}",
                        sub = "Fitur TF-IDF",
                        bgColor = SurfaceContainerLow,
                        textColor = IndigoPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        label = "Prior Hoaks",
                        value = "${analysis.priorHoax}%",
                        sub = "11.816 data",
                        bgColor = HoaxRedContainer,
                        textColor = OnHoaxRedContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        label = "Prior Fakta",
                        value = "${analysis.priorFakta}%",
                        sub = "11.816 data",
                        bgColor = FactGreenContainer,
                        textColor = OnFactGreenContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                val hoaxList: List<WordScoreItem>
                val faktaList: List<WordScoreItem>
                if (uiState.analysisModeGlobal || uiState.latestResult == null) {
                    hoaxList = analysis.topHoaxWords.take(5)
                    faktaList = analysis.topFaktaWords.take(5)
                } else {
                    val res = uiState.latestResult
                    hoaxList = res.topInfluentialWords
                        .filter { it.score > 0 }
                        .map { WordScoreItem(it.word, it.score) }
                        .ifEmpty { analysis.topHoaxWords.take(5) }
                    faktaList = res.topInfluentialWords
                        .filter { it.score < 0 }
                        .map { WordScoreItem(it.word, abs(it.score)) }
                        .ifEmpty { analysis.topFaktaWords.take(5) }
                }

                // Indikator Hoaks Card
                IndicatorWordCard(
                    title = "Indikator Hoaks",
                    badgeText = "High Risk",
                    badgeBg = HoaxRedContainer,
                    badgeFg = OnHoaxRedContainer,
                    accentColor = HoaxRed,
                    isHoax = true,
                    words = hoaxList
                )

                // Indikator Fakta Card
                IndicatorWordCard(
                    title = "Indikator Fakta",
                    badgeText = "Rujukan Sah",
                    badgeBg = FactGreenContainer,
                    badgeFg = OnFactGreenContainer,
                    accentColor = FactGreen,
                    isHoax = false,
                    words = faktaList
                )
            }

            // Cara Kerja Model Card
            EditorialCard(cornerRadius = 20.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cara Kerja Naive Bayes + TF-IDF",
                                style = MaterialTheme.typography.titleMedium,
                                color = OnSurfaceDark
                            )
                            Text(
                                text = "Prinsip dasar inferensi statistik",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Model memecah teks menjadi kata (tokenisasi), menghitung bobot kepentingan kata (TF-IDF), lalu mengalikan probabilitas kemunculan tiap kata pada berita hoaks vs fakta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = SurfaceContainerLow,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFormula = !showFormula }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Σ  Lihat Formula Matematika",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = IndigoPrimary
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = IndigoPrimary
                                )
                            }
                            AnimatedVisibility(visible = showFormula) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Text(
                                        text = "Kontribusi(w) = log P(w | Hoaks) − log P(w | Fakta)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OnSurfaceDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Nilai positif mendorong prediksi ke HOAKS (merah), nilai negatif mendorong ke FAKTA (hijau) dengan Laplace Smoothing (α = 1.0).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom CTA Banner
            EditorialCard(cornerRadius = 18.dp, containerColor = SurfaceContainerLow) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ingin cek kata per kalimat?",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurfaceDark
                        )
                        Text(
                            text = "Beralih ke tab Deteksi untuk analitik instan",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onGoToDeteksi,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Coba", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedTabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable { onClick() },
        color = if (selected) SurfaceWhite else Color.Transparent,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) IndigoPrimary else OnSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) IndigoPrimary else OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatMiniCard(
    label: String,
    value: String,
    sub: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = OnSurfaceDark,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

@Composable
private fun IndicatorWordCard(
    title: String,
    badgeText: String,
    badgeBg: Color,
    badgeFg: Color,
    accentColor: Color,
    isHoax: Boolean,
    words: List<WordScoreItem>
) {
    val maxScore = words.maxOfOrNull { it.score }?.coerceAtLeast(1.0) ?: 1.0

    EditorialCard(cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isHoax) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceDark
                    )
                }
                Surface(color = badgeBg, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                words.forEachIndexed { idx, item ->
                    val ratio = (item.score / maxScore).toFloat().coerceIn(0.15f, 1f)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${idx + 1}.  \"${item.word}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurfaceDark
                            )
                            Text(
                                text = if (isHoax) "+${item.score}" else "-${item.score}",
                                style = MaterialTheme.typography.labelLarge,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(SurfaceContainerHigh)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(ratio)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(accentColor)
                            )
                        }
                    }
                }
            }
        }
    }
}


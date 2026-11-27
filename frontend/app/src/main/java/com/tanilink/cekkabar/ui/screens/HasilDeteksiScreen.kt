package com.tanilink.cekkabar.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tanilink.cekkabar.data.model.DetectResponse
import com.tanilink.cekkabar.data.model.TokenItem
import com.tanilink.cekkabar.ui.components.DualDirectionBarChart
import com.tanilink.cekkabar.ui.components.EditorialCard
import com.tanilink.cekkabar.ui.components.HighlightedTokensBox
import com.tanilink.cekkabar.ui.components.SemiCircularGauge
import com.tanilink.cekkabar.ui.components.VerdictBadge
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

@Composable
fun HasilDeteksiScreen(
    result: DetectResponse,
    onBackClick: () -> Unit,
    onCheckAgainClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isHoax = result.verdict.uppercase() == "HOAKS"
    val primaryPct = if (isHoax) result.hoaxProbability else result.faktaProbability

    val defaultWord = remember(result) {
        result.tokens.firstOrNull { it.isWord && it.sentiment != "neutral" }
            ?: result.tokens.firstOrNull { it.isWord }
    }
    var selectedToken by remember(result) { mutableStateOf<TokenItem?>(defaultWord) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = OnSurfaceDark
                )
            }
            Column {
                Text(
                    text = "Detail Klaim",
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurfaceDark
                )
                Text(
                    text = "CekKabar Indonesia • Explainable AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Semi-Circular Gauge
            EditorialCard(cornerRadius = 24.dp) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VerdictBadge(verdict = result.confidenceLabel)

                    Spacer(modifier = Modifier.height(16.dp))

                    SemiCircularGauge(
                        percentage = primaryPct,
                        verdict = result.verdict
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHoax) "Model yakin teks ini cenderung hoaks" else "Model yakin teks ini cenderung fakta",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHoax) {
                            "Algoritma mendeteksi pola leksikal manipulatif dan sentimen disinformasi mendesak."
                        } else {
                            "Algoritma mendeteksi gaya penulisan jurnalistik formal dan rujukan faktual."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress comparison bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "● Hoaks ${result.hoaxProbability}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = HoaxRed
                        )
                        Text(
                            text = "Fakta ${result.faktaProbability}% ●",
                            style = MaterialTheme.typography.labelMedium,
                            color = FactGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val hoaxWeight = (result.hoaxProbability.toFloat() / 100f).coerceIn(0.05f, 0.95f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(hoaxWeight)
                                .fillMaxHeight()
                                .background(HoaxRed)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f - hoaxWeight)
                                .fillMaxHeight()
                                .background(FactGreen)
                        )
                    }
                }
            }

            // Card 2: Teks dengan Sorotan Kata
            EditorialCard(cornerRadius = 20.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Teks dengan Sorotan Kata",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sentuh salah satu kata berwarna untuk melihat kontribusi skor probabilitas model:",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    HighlightedTokensBox(
                        tokens = result.tokens,
                        selectedToken = selectedToken,
                        onTokenClick = { selectedToken = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        LegendDot(color = HoaxRed, label = "Dorong ke Hoaks (+)")
                        LegendDot(color = FactGreen, label = "Dorong ke Fakta (-)")
                    }
                }
            }

            // Card 3: Kata Terpilih Detail
            selectedToken?.let { tok ->
                val tokHoax = tok.score > 0
                val scoreColor = if (tokHoax) HoaxRed else if (tok.score < 0) FactGreen else OnSurfaceVariant
                val scoreStr = if (tok.score > 0) "+${tok.score}" else "${tok.score}"

                EditorialCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Flag,
                                    contentDescription = null,
                                    tint = scoreColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "KATA TERPILIH",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                    Text(
                                        text = "\"${tok.text}\"",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = OnSurfaceDark
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Kontribusi Skor",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = scoreStr,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = scoreColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = SurfaceContainerLow,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Log-Odds Ratio Model: log P(w|hoaks) − log P(w|fakta)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when {
                                        tok.score > 0.25 -> "! Dorongan Kuat ke Prediksi Hoaks"
                                        tok.score < -0.25 -> "✓ Dorongan Kuat ke Prediksi Fakta"
                                        else -> "• Kata Netral / Stopword"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = scoreColor
                                )
                            }
                        }
                    }
                }
            }

            // Card 4: Top Kata Berpengaruh (Dual-Direction Bar Chart)
            if (result.topInfluentialWords.isNotEmpty()) {
                EditorialCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.BarChart,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Top ${result.topInfluentialWords.size} Kata Berpengaruh",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnSurfaceDark
                                )
                            }
                            Text(
                                text = "Poin Pengaruh",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        DualDirectionBarChart(words = result.topInfluentialWords)
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onCheckAgainClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = PrimaryContainer,
                        contentColor = IndigoPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Periksa Lagi", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Hasil CekKabar: ${result.verdict} (${primaryPct}%)\n\n\"${result.text}\""
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Hasil CekKabar"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndigoPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
    }
}


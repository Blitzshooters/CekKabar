package com.tanilink.cekkabar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanilink.cekkabar.data.model.WordCloudItem
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
import com.tanilink.cekkabar.ui.theme.OutlineBorder
import com.tanilink.cekkabar.ui.theme.PrimaryContainer
import com.tanilink.cekkabar.ui.theme.SurfaceContainerHigh
import com.tanilink.cekkabar.ui.theme.SurfaceContainerLow
import com.tanilink.cekkabar.ui.theme.SurfaceWhite
import com.tanilink.cekkabar.ui.viewmodel.CekKabarUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordCloudScreen(
    uiState: CekKabarUiState,
    onSwitchCorpus: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isHoaxCorpus = uiState.wordCloudCorpus == "hoaks"
    val accentColor = if (isHoaxCorpus) HoaxRed else FactGreen
    val containerTint = if (isHoaxCorpus) HoaxRedContainer else FactGreenContainer
    val onContainerTint = if (isHoaxCorpus) OnHoaxRedContainer else OnFactGreenContainer
    var tappedWord by remember(uiState.wordCloudCorpus) { mutableStateOf<WordCloudItem?>(null) }

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
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Cloud,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Awan Kata",
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurfaceDark
                    )
                }
                Text(
                    text = "Distribusi frekuensi leksikal dalam korpus berita",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            // Corpus Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(SurfaceContainerHigh)
                    .padding(4.dp)
            ) {
                CorpusTogglePill(
                    label = "Korpus Hoaks",
                    dotColor = HoaxRed,
                    selected = isHoaxCorpus,
                    onClick = { onSwitchCorpus("hoaks") },
                    modifier = Modifier.weight(1f)
                )
                CorpusTogglePill(
                    label = "Korpus Fakta",
                    dotColor = FactGreen,
                    selected = !isHoaxCorpus,
                    onClick = { onSwitchCorpus("fakta") },
                    modifier = Modifier.weight(1f)
                )
            }

            val wc = uiState.wordCloud
            if (uiState.isLoadingWordCloud && wc == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IndigoPrimary)
                }
            } else if (wc != null) {
                // WordCloud Visual Card
                EditorialCard(cornerRadius = 22.dp) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (wc.words.isEmpty()) {
                            Text(
                                text = "Tidak ada kata yang cocok dengan pencarian.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(vertical = 32.dp)
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                wc.words.take(24).forEachIndexed { index, item ->
                                    val fontSize = (13f + item.weight.toFloat() * 14f).sp
                                    val isHighlight = index < 6
                                    val chipColor = if (isHighlight) containerTint else SurfaceContainerLow
                                    val textColor = when {
                                        index < 3 -> accentColor
                                        isHighlight -> onContainerTint
                                        else -> OnSurfaceDark
                                    }

                                    Surface(
                                        color = chipColor,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.clickable { tappedWord = item }
                                    ) {
                                        Text(
                                            text = if (index < 2) item.word.uppercase() else item.word,
                                            fontSize = fontSize,
                                            fontWeight = if (index < 5) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = textColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = SurfaceContainerLow,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TouchApp,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tappedWord?.let {
                                        "Kata \"${it.word}\" memiliki skor log-odds ${it.score} pada korpus ${wc.corpus.uppercase()}."
                                    } ?: "Ketuk salah satu kata untuk melihat skor log-odds kontribusinya",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 3 Stat Tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WcStatTile(
                        title = "Total Dokumen",
                        value = "${wc.totalDocs} teks",
                        modifier = Modifier.weight(1f)
                    )
                    WcStatTile(
                        title = "Kata Unik",
                        value = "${wc.uniqueWords}",
                        modifier = Modifier.weight(1f)
                    )
                    WcStatTile(
                        title = "Skor Maks",
                        value = wc.topWord?.let { "${it.word} (${it.score})" } ?: "-",
                        accent = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Search bar
                OutlinedTextField(
                    value = uiState.wordCloudQuery,
                    onValueChange = onQueryChanged,
                    placeholder = {
                        Text(
                            text = "Cari kata dalam awan kata...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = OnSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(999.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = OutlineBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Top 5 Dominan
                if (wc.words.isNotEmpty()) {
                    val topMax = wc.words.first().score.coerceAtLeast(1.0)
                    EditorialCard(cornerRadius = 20.dp) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "5 Kata Dominan Teratas",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnSurfaceDark
                                )
                                Text(
                                    text = "Skor Log-Odds",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                wc.words.take(5).forEachIndexed { idx, w ->
                                    val ratio = (w.score / topMax).toFloat().coerceIn(0.15f, 1f)
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${idx + 1}. ${w.word}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = OnSurfaceDark
                                            )
                                            Text(
                                                text = "${w.score}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = accentColor
                                            )
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

                // Karakteristik Linguistik Card
                EditorialCard(cornerRadius = 20.dp, containerColor = SurfaceContainerLow) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MenuBook,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Karakteristik Linguistik Korpus",
                                style = MaterialTheme.typography.titleSmall,
                                color = OnSurfaceDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isHoaxCorpus) {
                                    "Korpus hoaks didominasi referensi media sosial tidak terverifikasi (\"facebook\", \"disebarkan\", \"yg\", \"arsip\") serta kata ajakan viral."
                                } else {
                                    "Korpus fakta didominasi diksi jurnalistik baku dan kutipan narasumber resmi (\"ujar\", \"ditemui\", \"keterangan\", \"jakarta\")."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CorpusTogglePill(
    label: String,
    dotColor: Color,
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
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) dotColor else OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun WcStatTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = OnSurfaceDark
) {
    EditorialCard(modifier = modifier, cornerRadius = 14.dp) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}


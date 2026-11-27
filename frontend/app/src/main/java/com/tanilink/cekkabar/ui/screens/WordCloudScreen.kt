package com.tanilink.cekkabar.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanilink.cekkabar.data.model.WordCloudItem
import com.tanilink.cekkabar.ui.components.CekKabarTopBar
import com.tanilink.cekkabar.ui.components.EditorialCard
import com.tanilink.cekkabar.ui.components.ErrorRetryCard
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
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        if (uiState.wordCloud == null && !uiState.isLoadingWordCloud) {
            onRetry()
        }
    }
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
                val topGlow = if (isHoaxCorpus) Color(0xFFFFF3F4) else Color(0xFFF1FBF5)
                val deepAccent = if (isHoaxCorpus) Color(0xFF8C0D1D) else Color(0xFF146C3B)

                EditorialCard(cornerRadius = 24.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(topGlow, SurfaceWhite)
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 18.dp),
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
                            val cloudItems = remember(wc.words) {
                                interleaveCloudWords(wc.words.take(20))
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                cloudItems.forEach { (rank, item) ->
                                    val isSelected = tappedWord?.word == item.word
                                    val isPill = rank in setOf(10, 12, 16)
                                    val displayWord = when {
                                        rank in 0..3 -> item.word.uppercase()
                                        else -> item.word.replaceFirstChar { it.uppercase() }
                                    }
                                    val fontSize = when (rank) {
                                        0 -> 28.sp
                                        1 -> 25.sp
                                        2 -> 23.sp
                                        3 -> 26.sp
                                        in 4..6 -> 18.sp
                                        in 7..9 -> 16.sp
                                        in 10..13 -> 14.sp
                                        else -> 13.sp
                                    }
                                    val fontWeight = when (rank) {
                                        in 0..3 -> FontWeight.Black
                                        in 4..7 -> FontWeight.Bold
                                        in 8..14 -> FontWeight.SemiBold
                                        else -> FontWeight.Medium
                                    }
                                    val textColor = when {
                                        isSelected -> IndigoPrimary
                                        rank in 0..3 -> deepAccent
                                        rank in setOf(4, 7, 11) -> accentColor
                                        rank in setOf(14, 15) -> IndigoPrimary
                                        rank in setOf(5, 6, 8, 9) -> OnSurfaceDark
                                        else -> OnSurfaceVariant
                                    }

                                    if (isPill || isSelected) {
                                        Surface(
                                            color = if (isSelected) PrimaryContainer else Color(0xFFF0F2FA),
                                            shape = RoundedCornerShape(999.dp),
                                            border = if (isSelected) BorderStroke(1.dp, IndigoPrimary) else null,
                                            modifier = Modifier
                                                .align(Alignment.CenterVertically)
                                                .clip(RoundedCornerShape(999.dp))
                                                .clickable { tappedWord = item }
                                        ) {
                                            Text(
                                                text = displayWord,
                                                fontSize = if (isSelected) fontSize else 13.sp,
                                                fontWeight = fontWeight,
                                                color = textColor,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = displayWord,
                                            fontSize = fontSize,
                                            fontWeight = fontWeight,
                                            color = textColor,
                                            modifier = Modifier
                                                .align(Alignment.CenterVertically)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { tappedWord = item }
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = Color(0xFFF3F5FC),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TouchApp,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
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
            } else {
                ErrorRetryCard(
                    title = "Data Awan Kata Belum Tersedia",
                    message = uiState.wordCloudError ?: "Pastikan server backend aktif dan terhubung ke jaringan WiFi yang sama.",
                    onRetry = onRetry
                )
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

private fun interleaveCloudWords(words: List<WordCloudItem>): List<Pair<Int, WordCloudItem>> {
    val order = listOf(0, 6, 1, 8, 12, 2, 7, 14, 10, 3, 9, 5, 13, 4, 15, 17, 11, 18, 16, 19)
    val result = mutableListOf<Pair<Int, WordCloudItem>>()
    val used = mutableSetOf<Int>()
    for (idx in order) {
        if (idx < words.size) {
            result.add(idx to words[idx])
            used.add(idx)
        }
    }
    words.forEachIndexed { idx, item ->
        if (idx !in used) result.add(idx to item)
    }
    return result
}


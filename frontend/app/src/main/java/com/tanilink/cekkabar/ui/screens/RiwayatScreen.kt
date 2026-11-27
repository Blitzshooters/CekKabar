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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tanilink.cekkabar.data.model.HistoryItem
import com.tanilink.cekkabar.ui.components.CekKabarTopBar
import com.tanilink.cekkabar.ui.components.EditorialCard
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
import com.tanilink.cekkabar.ui.theme.OutlineBorder
import com.tanilink.cekkabar.ui.theme.PrimaryContainer
import com.tanilink.cekkabar.ui.theme.SurfaceContainerHigh
import com.tanilink.cekkabar.ui.theme.SurfaceContainerLow
import com.tanilink.cekkabar.ui.theme.SurfaceWhite
import com.tanilink.cekkabar.ui.viewmodel.CekKabarUiState

@Composable
fun RiwayatScreen(
    uiState: CekKabarUiState,
    onFilterSelected: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onHistoryClick: (HistoryItem) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onClearAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        CekKabarTopBar()

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Riwayat Deteksi",
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurfaceDark
                    )
                }
                Surface(color = PrimaryContainer, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        text = "${uiState.historyCounts.all} Tersimpan",
                        style = MaterialTheme.typography.labelSmall,
                        color = IndigoPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Text(
                text = "Catatan periksa berita & artikel sebelumnya",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search + Clear all
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.historyQuery,
                    onValueChange = onQueryChanged,
                    placeholder = {
                        Text(
                            text = "Cari riwayat kata kunci...",
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
                    modifier = Modifier.weight(1f)
                )

                if (uiState.historyCounts.all > 0) {
                    IconButton(
                        onClick = onClearAllClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(HoaxRedContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Hapus Semua",
                            tint = HoaxRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipPill(
                    label = "Semua (${uiState.historyCounts.all})",
                    selected = uiState.historyFilter == "all",
                    onClick = { onFilterSelected("all") }
                )
                FilterChipPill(
                    label = "Hoaks (${uiState.historyCounts.hoaks})",
                    selected = uiState.historyFilter == "hoaks",
                    onClick = { onFilterSelected("hoaks") }
                )
                FilterChipPill(
                    label = "Fakta (${uiState.historyCounts.fakta})",
                    selected = uiState.historyFilter == "fakta",
                    onClick = { onFilterSelected("fakta") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (uiState.isLoadingHistories && uiState.histories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IndigoPrimary)
            }
        } else if (uiState.histories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada riwayat deteksi. Coba periksa berita di tab Deteksi!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.histories, key = { it.id }) { item ->
                    HistoryCardItem(
                        item = item,
                        onClick = { onHistoryClick(item) },
                        onDelete = { onDeleteClick(item.id) }
                    )
                }
                item {
                    EditorialCard(cornerRadius = 18.dp, containerColor = SurfaceContainerLow) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Data Tersimpan Lokal",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = OnSurfaceDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Riwayat verifikasi disimpan di database SQLite perangkat terenkripsi AES-256 (SQLCipher + Android Keystore).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = IndigoPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Enkripsi Penyimpanan Perangkat Aktif",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IndigoPrimary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable { onClick() },
        color = if (selected) IndigoPrimary else SurfaceContainerHigh,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else OnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryCardItem(
    item: HistoryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isHoax = item.verdict.uppercase() == "HOAKS"
    val accent = if (isHoax) HoaxRed else FactGreen
    val tagBg = if (isHoax) HoaxRedContainer else FactGreenContainer
    val tagFg = if (isHoax) OnHoaxRedContainer else OnFactGreenContainer
    val pct = if (isHoax) item.hoaxProbability else item.faktaProbability

    EditorialCard(
        cornerRadius = 18.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(130.dp)
                    .background(accent)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VerdictBadge(verdict = item.verdict, probability = pct)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.formattedTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Hapus",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item.topKeywords.take(3).forEach { kw ->
                            Surface(color = tagBg, shape = RoundedCornerShape(999.dp)) {
                                Text(
                                    text = "#$kw",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tagFg,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Buka Detail",
                        tint = OnSurfaceVariant
                    )
                }
            }
        }
    }
}


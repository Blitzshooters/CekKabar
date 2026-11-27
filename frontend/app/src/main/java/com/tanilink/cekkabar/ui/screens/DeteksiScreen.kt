package com.tanilink.cekkabar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tanilink.cekkabar.data.model.ExampleNewsItem
import com.tanilink.cekkabar.ui.components.CekKabarTopBar
import com.tanilink.cekkabar.ui.components.EditorialCard
import com.tanilink.cekkabar.ui.components.VerdictBadge
import com.tanilink.cekkabar.ui.theme.FactGreen
import com.tanilink.cekkabar.ui.theme.HoaxRed
import com.tanilink.cekkabar.ui.theme.IndigoLightGradient
import com.tanilink.cekkabar.ui.theme.IndigoPrimary
import com.tanilink.cekkabar.ui.theme.OnSurfaceDark
import com.tanilink.cekkabar.ui.theme.OnSurfaceVariant
import com.tanilink.cekkabar.ui.theme.OutlineBorder
import com.tanilink.cekkabar.ui.theme.PrimaryContainer
import com.tanilink.cekkabar.ui.theme.SurfaceContainerHigh
import com.tanilink.cekkabar.ui.theme.SurfaceContainerLow
import com.tanilink.cekkabar.ui.viewmodel.CekKabarUiState

@Composable
fun DeteksiScreen(
    uiState: CekKabarUiState,
    onInputChanged: (String) -> Unit,
    onClearInput: () -> Unit,
    onDetectClick: () -> Unit,
    onExampleClick: (ExampleNewsItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        CekKabarTopBar()

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Pill badge
            Surface(
                color = PrimaryContainer,
                shape = RoundedCornerShape(999.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kecerdasan Buatan Terverifikasi",
                        style = MaterialTheme.typography.labelMedium,
                        color = IndigoPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Halo, Peneliti Fakta!",
                style = MaterialTheme.typography.headlineLarge,
                color = OnSurfaceDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Periksa kebenaran narasi berita & media sosial dengan transparansi kata.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Gradient Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(IndigoPrimary, IndigoLightGradient)
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Deteksi Sentimen & Diksi",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Cek klaim viral dalam hitungan detik secara obyektif",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Berita Card
            EditorialCard(cornerRadius = 20.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Input Berita / Klaim",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurfaceDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = PrimaryContainer,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.clickable {
                                    clipboardManager.getText()?.text?.let { pasted ->
                                        onInputChanged(pasted)
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentPaste,
                                        contentDescription = "Tempel",
                                        tint = IndigoPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tempel",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = IndigoPrimary
                                    )
                                }
                            }
                            if (uiState.inputText.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = onClearInput,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(SurfaceContainerHigh)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Hapus",
                                        tint = OnSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = onInputChanged,
                        placeholder = {
                            Text(
                                text = "Tempel judul atau isi berita di sini...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = OutlineBorder
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceDark)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Minimal 10 karakter untuk analisis",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                        Text(
                            text = "${uiState.inputText.length} / 2.000",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }

                    if (uiState.detectError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.detectError,
                            style = MaterialTheme.typography.bodySmall,
                            color = HoaxRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary CTA Button
            Button(
                onClick = onDetectClick,
                enabled = !uiState.isDetecting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndigoPrimary,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isDetecting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Menganalisis Teks...",
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Text(
                        text = "Periksa Sekarang",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Coba contoh berita
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Coba contoh berita",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceDark
                    )
                    Text(
                        text = "Klik kartu untuk langsung menganalisis",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                Text(
                    text = "Geser →",
                    style = MaterialTheme.typography.labelMedium,
                    color = IndigoPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.examples) { example ->
                val isHoax = example.label == "HOAKS"
                EditorialCard(
                    modifier = Modifier
                        .width(265.dp)
                        .clickable { onExampleClick(example) },
                    cornerRadius = 18.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        VerdictBadge(
                            verdict = "Contoh ${example.label.lowercase().replaceFirstChar { it.uppercase() }}"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"${example.text}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceDark,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = example.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isHoax) HoaxRed else FactGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Metodologi Analisis AI",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurfaceDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "CekKabar menggunakan model Naive Bayes & TF-IDF untuk mendeteksi pola leksikal hoaks serta menampilkan kontribusi tiap kata.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(FactGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Multi-corpus Bahasa Indonesia",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Privasi Terjaga",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}


# CekKabar 🔍

Aplikasi deteksi hoaks berita berbahasa Indonesia berbasis **Naive Bayes + TF-IDF**.

## Arsitektur

```
CekKabar/
├── modelling/      # Training model (Python)
├── backend/        # REST API (Laravel 13 + MySQL)
├── frontend/       # Android app (Kotlin + Jetpack Compose)
└── docs/           # Dokumentasi & simulasi Excel
```

## Stack Teknologi

| Layer | Teknologi |
|---|---|
| ML Model | Python 3, scikit-learn (Naive Bayes + TF-IDF) |
| Backend | Laravel 13, PHP 8.4, MySQL |
| Android | Kotlin, Jetpack Compose, Material3 |
| Keamanan | SQLCipher (AES-256), Android Keystore |

## Akurasi Model

| Metrik | Nilai |
|---|---|
| Akurasi | 96.49% |
| Presisi | 98.5% |
| Recall | 94.4% |
| F1-Score | 96.41% |
| Cross-Validation | 96.68% ± 0.21% |

## Setup

### 1. Modelling (Python)

```bash
cd modelling
pip install -r requirements.txt
python train_model.py
# Outputs: model_nb.pkl, word_contributions.csv, metrics.json
```

> **Catatan:** `model_nb.pkl` tidak di-commit ke repo. Download dari [Releases](../../releases).

### 2. Backend (Laravel)

```bash
cd backend
cp .env.example .env
composer install
php artisan key:generate
# Buat database MySQL: cekkabar
php artisan migrate
php artisan serve --host=0.0.0.0 --port=8000
```

### 3. Frontend (Android)

Buka folder `frontend/` di Android Studio, lalu build & run.

> Pastikan `BuildConfig.BASE_URL` di `build.gradle.kts` mengarah ke IP server yang benar.

## Fitur Aplikasi

- 🔍 **Deteksi** – Input teks berita, dapat hasil HOAKS/FAKTA + probabilitas
- 📊 **Analisis** – Visualisasi distribusi kata & statistik corpus
- ☁️ **Word Cloud** – Kata paling berpengaruh per kategori
- 📈 **Evaluasi** – Metrik akurasi model (confusion matrix, F1, dll.)
- 🕒 **Riwayat** – Histori deteksi tersimpan lokal dengan enkripsi SQLCipher

## Lisensi

MIT

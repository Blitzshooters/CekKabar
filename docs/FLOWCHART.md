# Flowchart Aplikasi CekKabar

> **Versi:** 1.0.0  
> **Tanggal:** 2026-11-27  
> **Stack:** Android (Kotlin + Jetpack Compose) · Laravel 13 (PHP 8.4) · Python 3.12 · MySQL

---

## Daftar Isi

1. [Arsitektur Sistem](#1-arsitektur-sistem)
2. [Alur Training Model ML](#2-alur-training-model-ml)
3. [Alur Utama: Deteksi Berita](#3-alur-utama-deteksi-berita)
4. [Alur Backend Inferensi](#4-alur-backend-inferensi)
5. [Alur Widget Home Screen](#5-alur-widget-home-screen)
6. [Alur Tab Riwayat](#6-alur-tab-riwayat)
7. [Alur Tab WordCloud](#7-alur-tab-wordcloud)
8. [Alur Tab Evaluasi](#8-alur-tab-evaluasi)
9. [Alur Data Repository (Offline-First)](#9-alur-data-repository-offline-first)

---

## 1. Arsitektur Sistem

Gambaran menyeluruh seluruh lapisan sistem CekKabar.

```mermaid
flowchart TD
    subgraph CLIENT["📱 Android Client"]
        UI["Jetpack Compose UI\n(5 Tab + Widget)"]
        VM["CekKabarViewModel\n(StateFlow)"]
        REPO["CekKabarRepository"]
        LOCAL["LocalHistoryDatabase\n(SQLite in-process)"]
        WIDGET["CekKabarWidgetProvider\n+ WidgetService"]
    end

    subgraph BACKEND["🖥️ Laravel 13 Backend"]
        API["HoaxController\n(REST API)"]
        SVC["HoaxInferenceService\n(PHP)"]
        CACHE["Laravel Cache\n(file-based)"]
        DB["MySQL\ndetection_histories"]
    end

    subgraph ML["🐍 Python 3.12 ML Engine"]
        INFER["infer.py\n(subprocess)"]
        MODEL["model_nb.pkl\n(TF-IDF + Naive Bayes)"]
        CSV["word_contributions.csv"]
        METRICS["metrics.json"]
    end

    UI -->|"event"| VM
    VM -->|"coroutine"| REPO
    REPO -->|"HTTP (Retrofit)"| API
    REPO <-->|"read/write"| LOCAL
    LOCAL -.->|"snapshot"| WIDGET

    API --> SVC
    SVC -->|"Cache hit"| CACHE
    SVC -->|"Cache miss"| INFER
    API -->|"persist"| DB
    API -->|"query"| DB

    INFER --> MODEL
    INFER --> CSV
    INFER --> METRICS
```

---

## 2. Alur Training Model ML

Dijalankan sekali offline sebelum deploy. Menghasilkan artefak yang dipakai `infer.py`.

```mermaid
flowchart TD
    START([▶ Jalankan train_model.py])
    LOAD["Load 4 Dataset CSV\n(Antara, Kompas, Detik, TBH)"]
    MERGE["Merge & Bersihkan\n(drop NaN, panjang > 10 char)"]
    BALANCE["Balance Dataset\n(undersample mayoritas)"]
    SPLIT["Train/Test Split\n80% / 20% · stratify=y"]
    TRAIN["Fit Pipeline\nTF-IDF (20k fitur, ngram 1-2)\n+ MultinomialNB (α=1.0)"]
    EVAL["Evaluasi Test Set\nAccuracy, Precision, Recall, F1"]
    CV["5-Fold Cross-Validation\nCV F1 Mean ± Std"]
    CONTRIB["Hitung Word Contributions\nlog P(w|Hoaks) − log P(w|Fakta)"]
    SAVE_PKL["Simpan model_nb.pkl"]
    SAVE_CSV["Simpan word_contributions.csv"]
    SAVE_JSON["Simpan metrics.json"]
    SAVE_TXT["Simpan classification_report.txt"]
    END([✅ Artefak siap dipakai infer.py])

    START --> LOAD --> MERGE --> BALANCE --> SPLIT --> TRAIN
    TRAIN --> EVAL --> CV --> CONTRIB
    CONTRIB --> SAVE_PKL & SAVE_CSV & SAVE_JSON & SAVE_TXT
    SAVE_PKL & SAVE_CSV & SAVE_JSON & SAVE_TXT --> END
```

---

## 3. Alur Utama: Deteksi Berita

Alur lengkap dari input pengguna di Android hingga hasil ditampilkan.

```mermaid
flowchart TD
    A([👤 User membuka aplikasi]) --> B["Tab Deteksi aktif\n(DeteksiScreen)"]
    B --> C{"Sumber teks?"}

    C -->|"Ketik manual"| D["onInputTextChanged\n→ update inputText di UiState"]
    C -->|"Tekan contoh berita"| E["onExampleClick\n→ textOverride = teks contoh"]
    C -->|"Widget: Tempel & Cek"| F["PendingIntent → MainActivity\nEXTRA_AUTO_PASTE = true"]

    F --> G["LaunchedEffect autoPasteRequestId\n→ baca clipboard\n→ onInputTextChanged\n→ detectNews(textOverride)"]

    D --> H["Tombol Deteksi ditekan\nonDetectClick"]
    E --> H
    G --> H

    H --> I{"len text ≥ 10?"}
    I -->|"Tidak"| J["Tampilkan error:\n'minimal 10 karakter'"]
    J --> B

    I -->|"Ya"| K["isDetecting = true\nviewModelScope.launch"]
    K --> L["repository.detectHoax(text)\nHTTP POST /api/v1/detect"]
    L --> M{"Response?"}

    M -->|"Success"| N["localDb.saveDetection(result)\nrefreshAllWidgets()"]
    N --> O["_uiState latestResult = result\nloadHistories()"]
    O --> P["navController.navigate 'hasil'"]
    P --> Q["HasilDeteksiScreen\nTampilkan verdict, probabilitas,\ntop keywords, token XAI,\ninfluential words"]

    M -->|"Error / timeout"| R["isDetecting = false\ndetectError = pesan error"]
    R --> S["Tampilkan error card\ndi DeteksiScreen"]
    S --> B

    Q --> T{"Aksi user?"}
    T -->|"Cek Lagi"| B
    T -->|"Back"| B
```

---

## 4. Alur Backend Inferensi

Detail proses di sisi server saat menerima request `POST /api/v1/detect`.

```mermaid
flowchart TD
    REQ(["📥 POST /api/v1/detect\n{text: '...'}"])
    VALIDATE["DetectHoaxRequest\nvalidasi: required, string, min:10, max:5000"]
    CHECK_VALID{"Valid?"}
    ERR422["HTTP 422\nValidation Error"]

    SVC_PREDICT["HoaxInferenceService::predict(text)"]
    BUILD_CMD["Build subprocess command:\n[python, infer.py, predict]"]
    BUILD_ENV["Inject Windows ENV:\nSystemRoot, PATH, APPDATA,\nLOCALAPPDATA, TEMP, ..."]
    RUN_PY["Jalankan infer.py via\nSymfony Process (timeout 60s)"]

    CHECK_PY{"Sukses?"}
    ERR500["HTTP 500\nML inference failed"]

    DECODE["json_decode output"]
    PERSIST["DetectionHistory::create(...)\nSimpan ke MySQL"]
    RESP["HTTP 201\nJSON: id, verdict, probabilities,\nconfidence_label, tokens, XAI words"]

    REQ --> VALIDATE --> CHECK_VALID
    CHECK_VALID -->|"Tidak"| ERR422
    CHECK_VALID -->|"Ya"| SVC_PREDICT
    SVC_PREDICT --> BUILD_CMD --> BUILD_ENV --> RUN_PY
    RUN_PY --> CHECK_PY
    CHECK_PY -->|"Gagal"| ERR500
    CHECK_PY -->|"Sukses"| DECODE --> PERSIST --> RESP
```

```mermaid
flowchart TD
    PY_START(["🐍 infer.py predict"])
    LOAD_PKL["pickle.load model_nb.pkl\n(TfidfVectorizer + MultinomialNB)"]
    LOAD_CSV["pd.read_csv word_contributions.csv"]
    TOKENIZE["Tokenisasi & Cleaning teks"]
    TFIDF["TF-IDF transform"]
    PREDICT["pipeline.predict(X)\npipeline.predict_proba(X)"]
    VERDICT{"label == 1?"}
    SET_HOAKS["verdict = 'HOAKS'"]
    SET_FAKTA["verdict = 'FAKTA'"]
    CONFIDENCE["Hitung confidence_label\n(Tinggi/Sedang/Rendah)"]
    TOP_KW["Ambil top_keywords\n(TF-IDF score tertinggi)"]
    TOKEN_XAI["Buat token list + sentiment\n(hoaks/fakta/neutral per kata)"]
    INFLUENTIAL["Top influential words\ndari word_contributions.csv"]
    OUTPUT["json.dumps seluruh result\n→ stdout"]
    PY_END(["✅ PHP baca stdout"])

    PY_START --> LOAD_PKL & LOAD_CSV
    LOAD_PKL & LOAD_CSV --> TOKENIZE --> TFIDF --> PREDICT
    PREDICT --> VERDICT
    VERDICT -->|"Ya"| SET_HOAKS
    VERDICT -->|"Tidak"| SET_FAKTA
    SET_HOAKS & SET_FAKTA --> CONFIDENCE --> TOP_KW --> TOKEN_XAI --> INFLUENTIAL --> OUTPUT --> PY_END
```

---

## 5. Alur Widget Home Screen

Interaksi antara Home Screen Widget dan aplikasi utama.

```mermaid
flowchart TD
    INSTALL(["👤 User pasang widget\ndi Home Screen"])
    PROVIDER["CekKabarWidgetProvider\nonUpdate()"]
    LOAD_FILTER["getFilter(appWidgetId)\ndari SharedPreferences"]
    LOAD_DATA["LocalHistoryDatabase.getHistories(filter)\nmaks. 10 item terakhir"]
    SET_REMOTE["Buat RemoteViews\nSet filter chips, stats, ListView"]
    BIND_SVC["WidgetService → CekKabarWidgetService\n(RemoteViewsFactory)"]
    RENDER["Widget tampil di Home Screen"]

    RENDER --> USER_ACT{"Aksi user?"}

    USER_ACT -->|"Tap filter chip\n(Semua/Hoaks/Fakta)"| BROADCAST["Kirim broadcast\nACTION_FILTER_WIDGET\nappWidgetId + filter baru"]
    BROADCAST --> SAVE_FILTER["saveFilter(appWidgetId, filter)"]
    SAVE_FILTER --> PROVIDER

    USER_ACT -->|"Tap item riwayat"| PI_DETAIL["PendingIntent → MainActivity\nEXTRA_HISTORY_ID = id"]
    PI_DETAIL --> MA_DETAIL["handleWidgetIntent\nrequestedHistoryId = id"]
    MA_DETAIL --> OPEN_HASIL["LaunchedEffect\nopenHistoryDetail(id)\nnavController → 'hasil'"]

    USER_ACT -->|"Tap Tempel & Cek"| PI_PASTE["PendingIntent → MainActivity\nEXTRA_AUTO_PASTE = true\nEXTRA_START_TAB = 'deteksi'"]
    PI_PASTE --> MA_PASTE["handleWidgetIntent\nautoPasteRequestId++"]
    MA_PASTE --> DO_PASTE["LaunchedEffect\nbaca clipboard\ndetectNews(textOverride)"]

    INSTALL --> PROVIDER --> LOAD_FILTER --> LOAD_DATA --> SET_REMOTE --> BIND_SVC --> RENDER

    subgraph REFRESH["🔄 Auto-Refresh Widget"]
        DETECT_DONE["detectHoax / deleteHistory /\nclearHistories selesai"]
        REFRESH_ALL["refreshAllWidgets(context)\nnotifyAppWidgetViewDataChanged()"]
        DETECT_DONE --> REFRESH_ALL --> PROVIDER
    end
```

---

## 6. Alur Tab Riwayat

Manajemen riwayat deteksi dengan filter dan pencarian.

```mermaid
flowchart TD
    OPEN(["👤 Buka Tab Riwayat"])
    LOAD["loadHistories(filter='all', query='')"]
    TRY_REMOTE["repository.getHistories(filter, query)\nHTTP GET /api/v1/histories"]
    CHECK_REMOTE{"HTTP sukses &\ndata tidak kosong?"}
    USE_REMOTE["Pakai data remote\nUpdate MySQL"]
    USE_LOCAL["Fallback: LocalHistoryDatabase\n(SQLite snapshot)"]
    RENDER["Tampilkan list riwayat\nHistoryCounts: Semua/Hoaks/Fakta"]

    RENDER --> USER_ACT{"Aksi user?"}

    USER_ACT -->|"Pilih filter\n(Semua/Hoaks/Fakta)"| SET_FILTER["setHistoryFilter(filter)\nloadHistories(filter, query)"]
    SET_FILTER --> TRY_REMOTE

    USER_ACT -->|"Ketik pencarian"| SET_QUERY["onHistoryQueryChanged(query)\nloadHistories(filter, query)"]
    SET_QUERY --> TRY_REMOTE

    USER_ACT -->|"Tap item"| DETAIL["openHistoryDetail(id)\ngetHistoryDetail(id)"]
    DETAIL --> CHECK_LOCAL{"Ada di LocalDb?"}
    CHECK_LOCAL -->|"Ya"| SHOW_DETAIL["Tampilkan HasilDeteksiScreen\n(data lokal)"]
    CHECK_LOCAL -->|"Tidak"| FETCH_DETAIL["GET /api/v1/histories/{id}\nlocalDb.saveDetection(result)"]
    FETCH_DETAIL --> SHOW_DETAIL

    USER_ACT -->|"Hapus satu item"| DELETE["deleteHistory(id)\nlocalDb + API + refreshWidget"]
    DELETE --> LOAD

    USER_ACT -->|"Hapus semua"| CLEAR["clearHistories()\nlocalDb + API + refreshWidget"]
    CLEAR --> LOAD

    OPEN --> LOAD --> TRY_REMOTE --> CHECK_REMOTE
    CHECK_REMOTE -->|"Ya"| USE_REMOTE --> RENDER
    CHECK_REMOTE -->|"Tidak"| USE_LOCAL --> RENDER
```

---

## 7. Alur Tab WordCloud

Eksplorasi distribusi kata penting per korpus dengan cache backend.

```mermaid
flowchart TD
    OPEN(["👤 Buka Tab WordCloud"])
    AUTO["LaunchedEffect(Unit)\nloadWordCloud(corpus='hoaks', query='')"]
    CHECK_CACHE{"Cache hit?\ncekkabar_wc_v2_{corpus}_{md5(q)}"}
    FROM_CACHE["Kembalikan data dari\nLaravel Cache (TTL 30 menit)"]
    RUN_PY["runPython wordcloud corpus query\ninfer.py wordcloud hoaks/fakta"]
    FILTER_CSV["Filter word_contributions.csv\nhoaks: kontribusi > 0\nfakta: kontribusi < 0"]
    SORT_LIMIT["Sort descending abs(score)\nAmbil top 60 kata"]
    RESPONSE["JSON: corpus, total_docs,\nunique_words, top_word, words[]"]
    RENDER_WC["Render awan kata tipografi\n(ukuran/warna/bobot proporsional)"]

    RENDER_WC --> USER_ACT{"Aksi user?"}

    USER_ACT -->|"Ganti korpus\n(Hoaks↔Fakta)"| SWITCH["switchWordCloudCorpus\nupdate wordCloudCorpus\nloadWordCloud(corpus baru)"]
    SWITCH --> CHECK_CACHE

    USER_ACT -->|"Ketik pencarian kata"| QUERY["onWordCloudQueryChanged(q)\nloadWordCloud(corpus, q)"]
    QUERY --> CHECK_CACHE

    USER_ACT -->|"Tap kata"| HIGHLIGHT["Highlight kata terpilih\n(efek visual)"]

    USER_ACT -->|"Retry (error)"| AUTO

    OPEN --> AUTO --> CHECK_CACHE
    CHECK_CACHE -->|"Hit"| FROM_CACHE --> RENDER_WC
    CHECK_CACHE -->|"Miss"| RUN_PY --> FILTER_CSV --> SORT_LIMIT --> RESPONSE --> RENDER_WC
```

---

## 8. Alur Tab Evaluasi

Memuat dan menampilkan metrik performa model.

```mermaid
flowchart TD
    OPEN(["👤 Buka Tab Evaluasi"])
    AUTO["LaunchedEffect(Unit)\nloadEvaluation()"]
    CHECK_CACHE{"Cache hit?\ncekkabar_evaluation_v1\n(TTL 1 jam)"}
    FROM_CACHE["Kembalikan dari Laravel Cache"]
    RUN_PY["runPython evaluation\ninfer.py evaluation"]
    READ_JSON["Baca metrics.json\n(dihasilkan train_model.py)"]
    BUILD_RESP["Susun EvaluationResponse:\naccuracy, precision, recall, f1,\ncv_f1_mean, cv_f1_std,\ncv_f1_per_fold, confusion_matrix,\nper_class (hoaks & fakta)"]
    RENDER["Tampilkan:\n- Metrik utama (card)\n- Confusion Matrix (grid 2×2)\n- Per-class metrics\n- CV F1 per-fold (bar)"]

    RENDER --> USER_ACT{"Error / Retry?"}
    USER_ACT -->|"Tap Retry"| AUTO

    OPEN --> AUTO --> CHECK_CACHE
    CHECK_CACHE -->|"Hit"| FROM_CACHE --> RENDER
    CHECK_CACHE -->|"Miss"| RUN_PY --> READ_JSON --> BUILD_RESP --> RENDER
```

---

## 9. Alur Data Repository (Offline-First)

Strategi sinkronisasi antara remote API dan local database.

```mermaid
flowchart TD
    CALL(["repository.getHistories\n(filter, query)"])

    TRY["Coba HTTP GET remote\n/api/v1/histories"]
    NET_OK{"Jaringan OK &\ndata remote ≥ 1 item?"}

    USE_REMOTE["Gunakan data remote\n(sumber kebenaran)"]
    CHECK_LOCAL{"localDb tidak kosong?"}
    USE_LOCAL["Fallback ke LocalHistoryDatabase\n(SQLite in-process)"]
    RETURN_EMPTY["Kembalikan empty list"]

    CALL --> TRY --> NET_OK
    NET_OK -->|"Ya"| USE_REMOTE
    NET_OK -->|"Tidak (error/timeout)"| CHECK_LOCAL
    CHECK_LOCAL -->|"Ya"| USE_LOCAL
    CHECK_LOCAL -->|"Tidak"| RETURN_EMPTY

    subgraph WRITE["✍️ Strategi Tulis"]
        DET["detectHoax(text)"]
        DET_FLOW["1. POST ke remote\n2. localDb.saveDetection(result)\n3. refreshAllWidgets()"]
        DET --> DET_FLOW

        DEL["deleteHistory(id)"]
        DEL_FLOW["1. localDb.deleteHistory(id)\n2. refreshAllWidgets()\n3. runCatching API delete\n   (best-effort, non-blocking)"]
        DEL --> DEL_FLOW

        CLR["clearHistories()"]
        CLR_FLOW["1. localDb.clearHistories()\n2. refreshAllWidgets()\n3. runCatching API clear\n   (best-effort, non-blocking)"]
        CLR --> CLR_FLOW
    end
```

---

## Ringkasan Alur Navigasi Android

```mermaid
flowchart LR
    LAUNCH(["App Launch\n/ Widget Intent"])
    DETEKSI["🔍 Deteksi\n(start destination)"]
    HASIL["📋 Hasil Deteksi\n(no bottom bar)"]
    ANALISIS["📊 Analisis"]
    WORDCLOUD["☁️ WordCloud"]
    EVALUASI["📈 Evaluasi"]
    RIWAYAT["🕐 Riwayat"]

    LAUNCH -->|"default"| DETEKSI
    LAUNCH -->|"EXTRA_START_TAB"| ANALISIS & WORDCLOUD & EVALUASI & RIWAYAT
    LAUNCH -->|"EXTRA_HISTORY_ID"| HASIL

    DETEKSI -->|"deteksi sukses"| HASIL
    RIWAYAT -->|"tap item"| HASIL
    HASIL -->|"back / cek lagi"| DETEKSI

    DETEKSI <-->|"BottomNav"| ANALISIS
    ANALISIS <-->|"BottomNav"| WORDCLOUD
    WORDCLOUD <-->|"BottomNav"| EVALUASI
    EVALUASI <-->|"BottomNav"| RIWAYAT
    RIWAYAT <-->|"BottomNav"| DETEKSI
```

---

> [!NOTE]
> Semua flowchart menggunakan data nyata dari kode sumber aplikasi. Lihat file implementasi:
> - `frontend/.../MainActivity.kt` — navigasi & widget intent
> - `frontend/.../CekKabarViewModel.kt` — state management
> - `frontend/.../CekKabarRepository.kt` — offline-first strategy
> - `backend/app/Services/HoaxInferenceService.php` — Python subprocess
> - `backend/app/Http/Controllers/Api/HoaxController.php` — REST endpoints
> - `modelling/train_model.py` — pipeline ML training
> - `backend/ml/infer.py` — runtime inference

"""
CekKabar – Simulasi Excel BERBASIS RUMUS
=========================================
Semua perhitungan menggunakan formula Excel nyata:
  - COUNTIF / COUNTA  → frekuensi kata
  - LN()              → log-likelihood
  - SUM()             → akumulasi log-posterior
  - EXP()             → softmax probabilitas
  - IF()              → label arah hoaks/fakta
  - Conditional formatting berbasis rumus

Sheet:
  1. Data & Label         – 8 judul berita + label
  2. Preprocessing        – tokenisasi manual + formula LOWER, SUBSTITUTE
  3. Frekuensi Kata       – COUNTIF per kata per kelas (referensi ke sheet 1)
  4. Likelihood (Laplace) – rumus P(w|kelas) = (COUNTIF+1)/(N+V) merujuk sheet 3
  5. Posterior (Berita Uji)– LN(likelihood) per token, SUM, softmax
  6. Kontribusi Kata      – log-ratio = LN(P_hoaks) - LN(P_fakta), sort via formula
  7. Evaluasi Model       – metrics dari metrics.json (satu-satunya nilai statis)
"""

import os, json, math, re
import pandas as pd
from openpyxl import Workbook
from openpyxl.styles import PatternFill, Font, Alignment, Border, Side
from openpyxl.utils import get_column_letter
from openpyxl.chart import BarChart, Reference
from openpyxl.formatting.rule import ColorScaleRule, CellIsRule, FormulaRule
from openpyxl.styles.differential import DifferentialStyle

# ─── Paths ────────────────────────────────────────────────────────────────────
BASE_DIR     = os.path.dirname(os.path.abspath(__file__))
DOCS_DIR     = os.path.join(BASE_DIR, "..", "docs")
METRICS_PATH = os.path.join(BASE_DIR, "metrics.json")
OUT_PATH     = os.path.join(DOCS_DIR, "simulasi_naive_bayes_cekkabar.xlsx")
os.makedirs(DOCS_DIR, exist_ok=True)

# ─── Colour palette ───────────────────────────────────────────────────────────
C_HEADER    = "2C3E50"
C_HOAKS_D   = "C0392B"
C_FAKTA_D   = "1A5276"
C_HOAKS_L   = "FADBD8"
C_FAKTA_L   = "D6EAF8"
C_HOAKS_M   = "E74C3C"
C_FAKTA_M   = "2980B9"
C_GREEN_D   = "1E8449"
C_GREEN_L   = "D5F5E3"
C_YELLOW    = "FEF9E7"
C_GRAY_L    = "F2F3F4"
C_WHITE     = "FFFFFF"
C_GOLD      = "F39C12"

thin  = Side(style="thin",   color="BDC3C7")
thick = Side(style="medium", color="7F8C8D")
BD    = Border(left=thin,  right=thin,  top=thin,  bottom=thin)
BDT   = Border(left=thick, right=thick, top=thick, bottom=thick)

def fill(c):  return PatternFill("solid", fgColor=c)
def bfnt(c="000000", sz=11): return Font(bold=True, color=c, size=sz)
def rfnt(c="000000", sz=10): return Font(color=c, size=sz)
def CA(wrap=False): return Alignment(horizontal="center", vertical="center", wrap_text=wrap)
def LA(wrap=True):  return Alignment(horizontal="left",   vertical="center", wrap_text=wrap)
def RA():           return Alignment(horizontal="right",  vertical="center")

def hdr(ws, row, col, val, bg=None, fg="FFFFFF", sz=11, wrap=False):
    c = ws.cell(row, col, val)
    c.fill = fill(bg or C_HEADER); c.font = bfnt(fg, sz)
    c.alignment = CA(wrap); c.border = BD
    return c

def dat(ws, row, col, val, bg=None, fg="000000", sz=10, align="L", bold=False, fmt=None):
    c = ws.cell(row, col, val)
    if bg: c.fill = fill(bg)
    c.font = Font(bold=bold, color=fg, size=sz)
    c.alignment = LA() if align=="L" else CA() if align=="C" else RA()
    c.border = BD
    if fmt: c.number_format = fmt
    return c

def col_w(ws, col, w):
    ws.column_dimensions[get_column_letter(col)].width = w

def title_row(ws, merge, text, bg=C_HEADER, h=36):
    ws.merge_cells(merge)
    r, c = int(merge[1]), 1
    ws[merge[:2]].value     = text
    ws[merge[:2]].fill      = fill(bg)
    ws[merge[:2]].font      = bfnt("FFFFFF", 14)
    ws[merge[:2]].alignment = CA()
    ws.row_dimensions[r].height = h

def sub_row(ws, merge, text, bg=C_GRAY_L, fg="555555"):
    ws.merge_cells(merge)
    ws[merge[:2]].value     = text
    ws[merge[:2]].fill      = fill(bg)
    ws[merge[:2]].font      = Font(italic=True, size=10, color=fg)
    ws[merge[:2]].alignment = CA()

# ─── Raw data ─────────────────────────────────────────────────────────────────
RAW = [
    (1, "Polri Tangkap Tersangka Kasus Penipuan Online Senilai Rp 10 Miliar", 0),
    (2, "Pemerintah Resmikan Jalan Tol Baru Sepanjang 45 Km di Sumatera",     0),
    (3, "KPU Umumkan Hasil Resmi Pilkada Serentak 2024 Beserta Rinciannya",   0),
    (4, "BPOM Setujui Vaksin Merah Putih untuk Distribusi Nasional",          0),
    (5, "VIRAL Kode Token Listrik Gratis PLN Tiga Bulan Tanpa Daftar",        1),
    (6, "HEBOH Presiden Bagikan Uang Rp 500 Ribu Lewat WhatsApp Sebarkan",    1),
    (7, "KONON Minum Air Kelapa Setiap Hari Sembuhkan Semua Penyakit Ajaib",  1),
    (8, "WASPADA Dana Bantuan UMKM Miliaran dari Pemerintah Daftar Sekarang", 1),
]

STOPWORDS = {
    "yang","dan","di","ke","dari","ini","itu","dengan","untuk","dalam",
    "adalah","pada","juga","oleh","atau","akan","telah","sudah","bisa",
    "ada","tidak","lebih","setelah","saat","agar","karena","hari","baru",
    "satu","tahun","dua","tiga","pun","jika","maka","namun","nya","kita",
    "mereka","saya","anda","kalian","secara","lewat","semua",
}

def tokenize(text):
    t = re.sub(r"[^a-z\s]", " ", text.lower())
    return [w for w in t.split() if len(w) > 2 and w not in STOPWORDS]

# Pre-compute tokens per berita (for sheet 3 COUNTIF source & sheet 5)
all_tokens_per_row = [(rid, judul, lbl, tokenize(judul)) for rid, judul, lbl, in [(r[0],r[1],r[2]) for r in RAW]]

# Full vocabulary (sorted, unique)
all_tokens_flat = [t for _, _, _, toks in all_tokens_per_row for t in toks]
vocab = sorted(set(all_tokens_flat))
V = len(vocab)

fakta_toks = [t for _, _, lbl, toks in all_tokens_per_row if lbl == 0 for t in toks]
hoaks_toks = [t for _, _, lbl, toks in all_tokens_per_row if lbl == 1 for t in toks]
N_F = len(fakta_toks)
N_H = len(hoaks_toks)

# ══════════════════════════════════════════════════════════════════════════════
wb = Workbook()
wb.remove(wb.active)

# ─────────────────────────────────────────────────────────────────────────────
# SHEET 1 – DATA & LABEL
# Named ranges: 'DataJudul' (B4:B11), 'DataLabel' (C4:C11)
# ─────────────────────────────────────────────────────────────────────────────
ws1 = wb.create_sheet("1. Data & Label")
ws1.sheet_view.showGridLines = False

title_row(ws1, "A1:E1", "📰  CekKabar – Dataset Contoh Berita (8 Judul)")
sub_row  (ws1, "A2:E2", "4 berita FAKTA (label=0) dari media kredibel + 4 berita HOAKS (label=1) konten viral")

# Column headers
for c, h in enumerate(["ID", "Judul Berita", "Label (0=Fakta/1=Hoaks)",
                        "Kelas", "Kata Pemicu"], 1):
    hdr(ws1, 3, c, h, sz=10, wrap=True)

col_w(ws1,1,6); col_w(ws1,2,58); col_w(ws1,3,22)
col_w(ws1,4,12); col_w(ws1,5,38)
ws1.row_dimensions[3].height = 32

notes = [
    "Media kredibel – Polri konfirmasi resmi",
    "Media kredibel – Rilis resmi Kementerian",
    "Media kredibel – Data resmi KPU",
    "Media kredibel – Rilis resmi BPOM",
    'Kata pemicu: "VIRAL", "gratis", "tanpa daftar"',
    'Kata pemicu: "HEBOH", "sebarkan", "WhatsApp"',
    'Kata pemicu: "KONON", "ajaib", "sembuhkan"',
    'Kata pemicu: "WASPADA", "miliaran", "daftar sekarang"',
]

for i, (rid, judul, lbl) in enumerate(RAW):
    r   = i + 4
    is_h = lbl == 1
    bg  = C_HOAKS_L if is_h else C_FAKTA_L

    dat(ws1, r, 1, rid,   bg=bg, align="C", bold=True)
    dat(ws1, r, 2, judul, bg=bg)
    dat(ws1, r, 3, lbl,   bg=bg, align="C")

    # Formula: =IF(C4=1,"HOAKS","FAKTA")
    kelas_cell = ws1.cell(r, 4)
    kelas_cell.value     = f'=IF(C{r}=1,"HOAKS","FAKTA")'
    kelas_cell.fill      = fill(C_HOAKS_L if is_h else C_FAKTA_L)
    kelas_cell.font      = Font(bold=True,
                                color=(C_HOAKS_D if is_h else C_FAKTA_D),
                                size=10)
    kelas_cell.alignment = CA()
    kelas_cell.border    = BD

    dat(ws1, r, 5, notes[i], bg=bg, sz=9)
    ws1.row_dimensions[r].height = 28

# Named ranges for formula references in later sheets
from openpyxl.workbook.defined_name import DefinedName
wb.defined_names["DataLabel"] = DefinedName(
    "DataLabel", attr_text="'1. Data & Label'!$C$4:$C$11")
wb.defined_names["DataJudul"] = DefinedName(
    "DataJudul", attr_text="'1. Data & Label'!$B$4:$B$11")

# Legend
ws1.merge_cells("A13:E13")
ws1["A13"].value     = "🔴 Merah = Hoaks  |  🔵 Biru = Fakta  |  Kolom D = formula =IF(C=1,\"HOAKS\",\"FAKTA\")"
ws1["A13"].fill      = fill("FDFEFE")
ws1["A13"].font      = Font(italic=True, size=9, color="555555")
ws1["A13"].alignment = CA()

# ─────────────────────────────────────────────────────────────────────────────
# SHEET 2 – PREPROCESSING
# Columns: ID | Judul Asli | Label | LOWER(judul) | Hapus Simbol | Tokens | #Token
# Formula: LOWER(), SUBSTITUTE(), LEN()-LEN(SUBSTITUTE())
# ─────────────────────────────────────────────────────────────────────────────
ws2 = wb.create_sheet("2. Preprocessing")
ws2.sheet_view.showGridLines = False

title_row(ws2, "A1:G1", "🔧  Tahap Preprocessing Teks")
sub_row  (ws2, "A2:G2",
          "Langkah: ① Lowercase  →  ② Hapus Angka & Simbol  →  "
          "③ Tokenisasi  →  ④ Hapus Stopword")

for c, h in enumerate(["ID","Judul Asli","Label","① Lowercase",
                        "② Hapus Angka/Simbol (SUBSTITUTE)","③ Token (Hapus Stopword)","④ Jml Token"], 1):
    hdr(ws2, 3, c, h, sz=9, wrap=True)

col_w(ws2,1,5); col_w(ws2,2,46); col_w(ws2,3,8)
col_w(ws2,4,46); col_w(ws2,5,46); col_w(ws2,6,52); col_w(ws2,7,10)
ws2.row_dimensions[3].height = 36

# Token strings (computed in Python, displayed as text – manual step)
token_strings = []
for _, _, lbl, toks in all_tokens_per_row:
    token_strings.append(" | ".join(toks))

# Map to sheet 1 rows (B4:B11, C4:C11)
for i in range(8):
    r    = i + 4
    s1r  = i + 4   # corresponding row in sheet 1
    is_h = RAW[i][2] == 1
    bg   = C_HOAKS_L if is_h else C_FAKTA_L
    toks = all_tokens_per_row[i][3]

    # ID – formula referencing sheet 1
    c = ws2.cell(r, 1); c.value = f"='1. Data & Label'!A{s1r}"
    c.fill=fill(bg); c.font=rfnt(sz=10); c.alignment=CA(); c.border=BD

    # Judul Asli – ref sheet 1
    c = ws2.cell(r, 2); c.value = f"='1. Data & Label'!B{s1r}"
    c.fill=fill(bg); c.font=rfnt(sz=9); c.alignment=LA(); c.border=BD

    # Label – ref sheet 1
    c = ws2.cell(r, 3); c.value = f"='1. Data & Label'!C{s1r}"
    c.fill=fill(bg); c.font=rfnt(sz=10); c.alignment=CA(); c.border=BD

    # ① Lowercase – Excel formula LOWER()
    c = ws2.cell(r, 4); c.value = f"=LOWER('1. Data & Label'!B{s1r})"
    c.fill=fill(bg); c.font=rfnt(sz=9); c.alignment=LA(); c.border=BD

    # ② Hapus angka & simbol – chain SUBSTITUTE digit 0-9 + simbol aman
    # Catatan: apostrophe (') dan double-quote (") dihindari (bermasalah di XML formula)
    # Gunakan CLEAN() di awal untuk hapus karakter non-printable
    sub_chain = f"CLEAN(LOWER('1. Data & Label'!B{s1r}))"
    for d in "0123456789":
        sub_chain = f"SUBSTITUTE({sub_chain},{chr(34)}{d}{chr(34)},{chr(34)}{chr(34)})"
    # Simbol yang aman di XML (hindari ' dan ")
    for sym in [".", ",", "!", "?", "(", ")", "/", "-", ":", ";"]:
        sub_chain = f"SUBSTITUTE({sub_chain},{chr(34)}{sym}{chr(34)},{chr(34)}{chr(34)})"
    c = ws2.cell(r, 5); c.value = f"=TRIM({sub_chain})"
    c.fill=fill(bg); c.font=rfnt(sz=9); c.alignment=LA(); c.border=BD

    # ③ Tokens (Hapus Stopword) – FORMULA Excel:
    # Bungkus E{r} dengan spasi kiri-kanan, hapus stopword + kata 2 huruf umum via SUBSTITUTE,
    # lalu TRIM() hapus spasi berlebih, lalu ganti spasi menjadi " | "
    sw_sheet2 = [
        "yang", "dan", "di", "ke", "dari", "ini", "itu", "dengan",
        "untuk", "dalam", "adalah", "pada", "juga", "oleh", "atau",
        "akan", "telah", "sudah", "bisa", "ada", "tidak", "hari",
        "baru", "tiga", "lewat", "semua", "rp", "km", "sebagai", "karena"
    ]
    tok_chain = f'" "&E{r}&" "'
    for sw in sw_sheet2:
        tok_chain = f'SUBSTITUTE({tok_chain}," {sw} "," ")'
    c = ws2.cell(r, 6)
    c.value = f'=SUBSTITUTE(TRIM({tok_chain})," "," | ")'
    c.fill=fill(bg); c.font=rfnt(sz=9,c="333333"); c.alignment=LA(); c.border=BD

    # ④ Jml Token – FORMULA: hitung pipe separator + 1
    # Rumus: IF(F{r}="",0, LEN(TRIM(F{r}))-LEN(SUBSTITUTE(TRIM(F{r}),"|",""))+1)
    c = ws2.cell(r, 7)
    c.value = (f'=IF(F{r}="",0,'
               f'LEN(TRIM(F{r}))-LEN(SUBSTITUTE(TRIM(F{r}),"|",""))+1)')
    c.fill=fill(bg); c.font=Font(bold=True, size=10)
    c.alignment=CA(); c.border=BD

    ws2.row_dimensions[r].height = 42

# Note
ws2.merge_cells("A13:G13")
ws2["A13"].value = ("💡 Semua tahap pakai rumus! ① LOWER()  |  ② TRIM(SUBSTITUTE(...)) hapus angka & simbol  |  "
                    "③ SUBSTITUTE(TRIM(SUBSTITUTE(...))) hapus stopword & pisah ' | '  |  ④ LEN()-LEN(SUBSTITUTE()) hitung token")
ws2["A13"].fill      = fill("FDFEFE"); ws2["A13"].font = Font(italic=True, size=9, color="555555")
ws2["A13"].alignment = LA(); ws2.row_dimensions[13].height = 28

# ─────────────────────────────────────────────────────────────────────────────
# SHEET 3 – FREKUENSI KATA
# Source data: token strings di kolom F sheet 2 (F4:F11)
# Formula: =COUNTIF menggunakan wildcard "*kata*" per kelas
#
# Layout:
#   A: Kata  |  B: Token Fakta (concat)  |  C: Token Hoaks (concat)
#   D: Count Fakta  |  E: Count Hoaks  |  F: Total  |  G: Muncul di
#
# Rows 4-7:  FAKTA token strings (referensi sheet 2 F4:F7)
# Rows 8-11: HOAKS token strings (referensi sheet 2 F8:F11)
# Rows 14+:  Vocabulary table dengan COUNTIF
# ─────────────────────────────────────────────────────────────────────────────
ws3 = wb.create_sheet("3. Frekuensi Kata")
ws3.sheet_view.showGridLines = False

title_row(ws3, "A1:G1", "📊  Tabel Frekuensi Kata per Kelas")

# Info banner
ws3.merge_cells("A2:G2")
ws3["A2"].value = (f"V = {V} kata unik  |  N_Fakta = {N_F} token  |  N_Hoaks = {N_H} token  "
                   f"|  Formula: COUNTIF(sumber_kelas, \"*kata*\")")
ws3["A2"].fill=fill(C_GRAY_L); ws3["A2"].font=Font(italic=True,size=10,color="555555")
ws3["A2"].alignment=CA()

# --- Sub-section: sumber token per kelas ---
ws3.merge_cells("A3:G3")
ws3["A3"].value = "▼  Sumber Token (referensi dari Sheet 2 Kolom F – digunakan COUNTIF di bawah)"
ws3["A3"].fill=fill(C_HEADER); ws3["A3"].font=bfnt("FFFFFF",10); ws3["A3"].alignment=CA()

# FAKTA sources (rows 4-7 → sheet 2 rows 4-7)
for i in range(4):
    r = i + 4
    ws3.merge_cells(f"A{r}:G{r}")
    c = ws3.cell(r, 1)
    c.value = f"='2. Preprocessing'!F{r}"  # token string
    c.fill = fill(C_FAKTA_L); c.font = rfnt(sz=9, c=C_FAKTA_D)
    c.alignment = LA(); c.border = BD

# HOAKS sources (rows 8-11 → sheet 2 rows 8-11)
for i in range(4):
    r = i + 8
    s2r = i + 8
    ws3.merge_cells(f"A{r}:G{r}")
    c = ws3.cell(r, 1)
    c.value = f"='2. Preprocessing'!F{s2r}"
    c.fill = fill(C_HOAKS_L); c.font = rfnt(sz=9, c=C_HOAKS_D)
    c.alignment = LA(); c.border = BD

# Legend for sumber
ws3.merge_cells("A12:G12")
ws3["A12"].value = "🔵 Baris 4-7 = Token berita FAKTA  |  🔴 Baris 8-11 = Token berita HOAKS"
ws3["A12"].fill=fill("FDFEFE"); ws3["A12"].font=Font(italic=True,size=9,color="555555")
ws3["A12"].alignment=CA()

# --- Vocabulary table ---
ws3.merge_cells("A13:G13")
ws3["A13"].value = "▼  Tabel Frekuensi Kata (COUNTIF menggunakan wildcard \"*kata*\" pada sumber token di atas)"
ws3["A13"].fill=fill(C_HEADER); ws3["A13"].font=bfnt("FFFFFF",10); ws3["A13"].alignment=CA()

hdrs3 = ["Kata (Vocab)", "Count Fakta", "Count Hoaks", "Total", "Muncul di Kelas"]
for c, h in enumerate(hdrs3, 1):
    hdr(ws3, 14, c, h, sz=10)

col_w(ws3,1,20); col_w(ws3,2,14); col_w(ws3,3,14)
col_w(ws3,4,12); col_w(ws3,5,22)

# FAKTA range: A4:A7  → sheet 3 rows 4-7 (merged, so COUNTIF on those rows)
# HOAKS range: A8:A11 → sheet 3 rows 8-11
# We use wildcard: =COUNTIF($A$4:$A$7,"*"&A15&"*")
# (each row in A4:A7 is a token string like "viral | kode | token | listrik")

FAKTA_RANGE = "$A$4:$A$7"   # source strings in ws3
HOAKS_RANGE = "$A$8:$A$11"

for i, word in enumerate(vocab):
    r = i + 15
    is_h_dom = word in hoaks_toks and word not in fakta_toks
    is_f_dom = word in fakta_toks and word not in hoaks_toks
    bg = C_HOAKS_L if is_h_dom else C_FAKTA_L if is_f_dom else C_YELLOW

    # Kata
    dat(ws3, r, 1, word, bg=bg, bold=True)

    # Count Fakta =COUNTIF($A$4:$A$7,"*"&A{r}&"*")
    c = ws3.cell(r, 2)
    c.value     = f'=COUNTIF({FAKTA_RANGE},"*"&A{r}&"*")'
    c.fill=fill(bg); c.font=Font(size=10,color=C_FAKTA_D)
    c.alignment=CA(); c.border=BD

    # Count Hoaks =COUNTIF($A$8:$A$11,"*"&A{r}&"*")
    c = ws3.cell(r, 3)
    c.value     = f'=COUNTIF({HOAKS_RANGE},"*"&A{r}&"*")'
    c.fill=fill(bg); c.font=Font(size=10,color=C_HOAKS_D)
    c.alignment=CA(); c.border=BD

    # Total =B{r}+C{r}
    c = ws3.cell(r, 4)
    c.value     = f'=B{r}+C{r}'
    c.fill=fill(bg); c.alignment=CA(); c.border=BD; c.font=rfnt(sz=10)

    # Kelas =IF(AND(B{r}>0,C{r}>0),"Fakta & Hoaks",IF(B{r}>0,"Fakta","Hoaks"))
    c = ws3.cell(r, 5)
    c.value = (f'=IF(AND(B{r}>0,C{r}>0),"Fakta & Hoaks",'
               f'IF(B{r}>0,"Fakta","Hoaks"))')
    c.fill=fill(bg); c.alignment=CA(); c.border=BD; c.font=rfnt(sz=10)

# Total row
sr = len(vocab) + 15
ws3.cell(sr,1,"TOTAL").font=bfnt(); ws3.cell(sr,1).fill=fill(C_GRAY_L); ws3.cell(sr,1).border=BDT
c=ws3.cell(sr,2); c.value=f"=SUM(B15:B{sr-1})"; c.fill=fill(C_FAKTA_L)
c.font=bfnt(C_FAKTA_D); c.alignment=CA(); c.border=BDT
c=ws3.cell(sr,3); c.value=f"=SUM(C15:C{sr-1})"; c.fill=fill(C_HOAKS_L)
c.font=bfnt(C_HOAKS_D); c.alignment=CA(); c.border=BDT
c=ws3.cell(sr,4); c.value=f"=B{sr}+C{sr}"; c.alignment=CA(); c.border=BDT; c.font=bfnt()

# Named ranges for Sheet 4 to reference
# VocabWords: Sheet3!A15:A{last}  CountFakta: B15:B{last}  CountHoaks: C15:C{last}
last_vocab_row = len(vocab) + 14
wb.defined_names["VocabWords"]  = DefinedName("VocabWords",
    attr_text=f"'3. Frekuensi Kata'!$A$15:$A${last_vocab_row}")
wb.defined_names["CountFakta"]  = DefinedName("CountFakta",
    attr_text=f"'3. Frekuensi Kata'!$B$15:$B${last_vocab_row}")
wb.defined_names["CountHoaks"]  = DefinedName("CountHoaks",
    attr_text=f"'3. Frekuensi Kata'!$C$15:$C${last_vocab_row}")

# ─────────────────────────────────────────────────────────────────────────────
# SHEET 4 – LIKELIHOOD (LAPLACE SMOOTHING)
# P(w|Fakta) = (CountFakta + 1) / (N_Fakta + V)
# P(w|Hoaks) = (CountHoaks + 1) / (N_Hoaks + V)
# Semua menggunakan referensi ke sheet 3
# ─────────────────────────────────────────────────────────────────────────────
ws4 = wb.create_sheet("4. Likelihood (Laplace)")
ws4.sheet_view.showGridLines = False

title_row(ws4, "A1:H1", "📐  Likelihood P(kata|kelas) dengan Laplace Smoothing")

ws4.merge_cells("A2:H2")
ws4["A2"].value = ("Rumus:  P(w|kelas) = (count(w,kelas) + 1) / (N_kelas + V)  "
                   f"|  V={V} kata  |  N_Fakta={N_F}  |  N_Hoaks={N_H}")
ws4["A2"].fill=fill(C_GRAY_L); ws4["A2"].font=Font(italic=True,size=10,color="555555")
ws4["A2"].alignment=CA()

# Parameter cells (editable constants)
ws4.merge_cells("A3:H3")
ws4["A3"].value = ("▼ Parameter (sel kuning = nilai yang bisa diubah, formula di bawah merujuk sel ini)")
ws4["A3"].fill=fill(C_HEADER); ws4["A3"].font=bfnt("FFFFFF",10); ws4["A3"].alignment=CA()

params = [
    ("N_Fakta (total token kelas Fakta)", N_F,  "J3", C_FAKTA_L),
    ("N_Hoaks (total token kelas Hoaks)", N_H,  "L3", C_HOAKS_L),
    ("V (ukuran vocab/kosakata)",         V,    "N3", C_YELLOW),
]
# Put param labels+values in a row
ws4.cell(4,1,"Parameter").fill=fill(C_GRAY_L); ws4.cell(4,1).font=bfnt(sz=10); ws4.cell(4,1).border=BD
ws4.cell(4,2,"Nilai").fill=fill(C_GRAY_L); ws4.cell(4,2).font=bfnt(sz=10); ws4.cell(4,2).border=BD
ws4.cell(4,3,"Rumus Penyebut Fakta").fill=fill(C_GRAY_L); ws4.cell(4,3).font=bfnt(sz=10); ws4.cell(4,3).border=BD
ws4.cell(4,4,"Nilai Penyebut Fakta").fill=fill(C_GRAY_L); ws4.cell(4,4).font=bfnt(sz=10); ws4.cell(4,4).border=BD
ws4.cell(4,5,"Rumus Penyebut Hoaks").fill=fill(C_GRAY_L); ws4.cell(4,5).font=bfnt(sz=10); ws4.cell(4,5).border=BD
ws4.cell(4,6,"Nilai Penyebut Hoaks").fill=fill(C_GRAY_L); ws4.cell(4,6).font=bfnt(sz=10); ws4.cell(4,6).border=BD
ws4.row_dimensions[4].height=22

# Param values in row 5 (editable)
ws4.cell(5,1,"N_Fakta").font=rfnt(sz=10); ws4.cell(5,1).border=BD; ws4.cell(5,1).alignment=LA()
c=ws4.cell(5,2,N_F); c.fill=fill(C_FAKTA_L); c.font=bfnt(C_FAKTA_D); c.alignment=CA(); c.border=BD  # <-- editable
ws4.cell(5,3,"=B5+D5").font=rfnt(sz=9); ws4.cell(5,3).border=BD; ws4.cell(5,3).alignment=CA()
c=ws4.cell(5,4,V);   c.fill=fill(C_YELLOW); c.font=bfnt(C_GOLD); c.alignment=CA(); c.border=BD  # V editable
c=ws4.cell(5,5,"=C5");c.font=rfnt(sz=9); c.border=BD; c.alignment=CA()
ws4.cell(5,6,"N_Hoaks").font=rfnt(sz=10); ws4.cell(5,6).border=BD; ws4.cell(5,6).alignment=LA()
c=ws4.cell(5,7,N_H); c.fill=fill(C_HOAKS_L); c.font=bfnt(C_HOAKS_D); c.alignment=CA(); c.border=BD  # editable
c=ws4.cell(5,8,V);   c.fill=fill(C_YELLOW); c.font=bfnt(C_GOLD); c.alignment=CA(); c.border=BD  # V editable

# Fix param cells properly
ws4.cell(5,3).value = "=B5+D5"   # N_F + V
ws4.cell(5,5).value = "=G5+H5"   # N_H + V

# -- Cleaner param layout --
# B5 = N_Fakta (editable, yellow bg)
# D5 = V
# B5+D5 = penyebut fakta (shown in C5)
# G5 = N_Hoaks, H5 = V
# G5+H5 = penyebut hoaks (shown in F5... actually let me redo)

# Reset param row cleanly:
for col in range(1,9):
    ws4.cell(5,col).value = None

# B5 = N_Fakta  |  C5 = V  |  D5 = penyebut fakta =B5+C5
# F5 = N_Hoaks  |  G5 = V  |  H5 = penyebut hoaks =F5+G5
c=ws4.cell(5,1,"N_Fakta ="); c.font=bfnt(sz=10); c.alignment=RA(); c.border=BD
c=ws4.cell(5,2,N_F); c.fill=fill(C_FAKTA_L); c.font=bfnt(C_FAKTA_D,12); c.alignment=CA(); c.border=BD
c=ws4.cell(5,3,"V ="); c.font=bfnt(sz=10); c.alignment=RA(); c.border=BD
c=ws4.cell(5,4,V); c.fill=fill(C_YELLOW); c.font=bfnt(C_GOLD,12); c.alignment=CA(); c.border=BD
c=ws4.cell(5,5,"Penyebut Fakta = N_F+V ="); c.font=bfnt(sz=9); c.alignment=RA(); c.border=BD
c=ws4.cell(5,6,"=B5+D5"); c.fill=fill(C_FAKTA_L); c.font=bfnt(C_FAKTA_D,13); c.alignment=CA(); c.border=BD
c=ws4.cell(5,7,"N_Hoaks ="); c.font=bfnt(sz=10); c.alignment=RA(); c.border=BD
c=ws4.cell(5,8,N_H); c.fill=fill(C_HOAKS_L); c.font=bfnt(C_HOAKS_D,12); c.alignment=CA(); c.border=BD

ws4.merge_cells("A3:H3")
ws4.cell(3,1).value = "▼  Parameter Laplace (sel isian = nilai yang dapat diubah, formula merujuk ke sini)"
ws4.cell(3,1).fill=fill(C_HEADER); ws4.cell(3,1).font=bfnt("FFFFFF",10); ws4.cell(3,1).alignment=CA()

# Penyebut Hoaks row
ws4.cell(6,1,"Penyebut Hoaks = N_H+V =").font=bfnt(sz=9); ws4.cell(6,1).alignment=RA()
c=ws4.cell(6,2,"=H5+D5"); c.fill=fill(C_HOAKS_L); c.font=bfnt(C_HOAKS_D,13); c.alignment=CA(); c.border=BD
ws4.cell(6,3,"← Lihat H5 (N_Hoaks) + D5 (V)").font=Font(italic=True,size=9,color="555555")
ws4.row_dimensions[5].height=26; ws4.row_dimensions[6].height=24

# -- Vocabulary table --
ws4.merge_cells("A7:H7")
ws4["A7"].value = "▼  Tabel Likelihood (semua formula merujuk ke Sheet 3 dan parameter di atas)"
ws4["A7"].fill=fill(C_HEADER); ws4["A7"].font=bfnt("FFFFFF",10); ws4["A7"].alignment=CA()

hdrs4 = ["Kata","count(Fakta)","count(Hoaks)",
          "P(w|Fakta)","P(w|Hoaks)","log P(w|Fakta)","log P(w|Hoaks)","Lebih Besar"]
for c_i, h in enumerate(hdrs4, 1):
    hdr(ws4, 8, c_i, h, sz=9, wrap=True)

col_w(ws4,1,20); col_w(ws4,2,14); col_w(ws4,3,14)
col_w(ws4,4,16); col_w(ws4,5,16)
col_w(ws4,6,18); col_w(ws4,7,18); col_w(ws4,8,15)
ws4.row_dimensions[8].height=28

# References to sheet 3 vocab rows (15 to 15+V-1)
# CountFakta for word i is in '3. Frekuensi Kata'!B{15+i}
# P(w|Fakta) = (B_s3 + 1) / ($B$5 + $D$5)  = (count+1)/penyebut_fakta
# penyebut fakta is in ws4 cell B6 (=H5+D5... actually $B$5+$D$5)
# Let's use: penyebut_fakta = $B$5+$D$5  penyebut_hoaks = $H$5+$D$5

PF = "$B$5+$D$5"   # penyebut fakta formula string
PH = "$H$5+$D$5"   # penyebut hoaks

for i, word in enumerate(vocab):
    r    = i + 9
    s3r  = i + 15   # row in sheet 3 vocab table
    is_h = word in hoaks_toks and word not in fakta_toks
    is_f = word in fakta_toks and word not in hoaks_toks
    bg   = C_HOAKS_L if is_h else C_FAKTA_L if is_f else C_YELLOW

    # Kata – ref sheet 3
    c = ws4.cell(r,1); c.value=f"='3. Frekuensi Kata'!A{s3r}"
    c.fill=fill(bg); c.font=Font(bold=True,size=10); c.alignment=LA(); c.border=BD

    # count(Fakta) ref sheet 3
    c = ws4.cell(r,2); c.value=f"='3. Frekuensi Kata'!B{s3r}"
    c.fill=fill(bg); c.font=Font(size=10,color=C_FAKTA_D)
    c.alignment=CA(); c.border=BD

    # count(Hoaks) ref sheet 3
    c = ws4.cell(r,3); c.value=f"='3. Frekuensi Kata'!C{s3r}"
    c.fill=fill(bg); c.font=Font(size=10,color=C_HOAKS_D)
    c.alignment=CA(); c.border=BD

    # P(w|Fakta) = (B{r}+1) / (N_F+V) = (B{r}+1) / ($B$5+$D$5)
    c = ws4.cell(r,4); c.value=f"=(B{r}+1)/({PF})"
    c.fill=fill(bg); c.font=Font(size=10,color=C_FAKTA_D)
    c.alignment=CA(); c.border=BD; c.number_format="0.000000"

    # P(w|Hoaks) = (C{r}+1) / (N_H+V) = (C{r}+1) / ($H$5+$D$5)
    c = ws4.cell(r,5); c.value=f"=(C{r}+1)/({PH})"
    c.fill=fill(bg); c.font=Font(size=10,color=C_HOAKS_D)
    c.alignment=CA(); c.border=BD; c.number_format="0.000000"

    # log P(w|Fakta) = LN(D{r})
    c = ws4.cell(r,6); c.value=f"=LN(D{r})"
    c.fill=fill(bg); c.font=Font(size=10,color=C_FAKTA_D)
    c.alignment=CA(); c.border=BD; c.number_format="0.0000"

    # log P(w|Hoaks) = LN(E{r})
    c = ws4.cell(r,7); c.value=f"=LN(E{r})"
    c.fill=fill(bg); c.font=Font(size=10,color=C_HOAKS_D)
    c.alignment=CA(); c.border=BD; c.number_format="0.0000"

    # Label: IF(E>D, "Hoaks","Fakta")
    c = ws4.cell(r,8); c.value=f'=IF(E{r}>D{r},"→ HOAKS","→ FAKTA")'
    c.fill=fill(bg); c.font=Font(bold=True,size=10,
                                  color=(C_HOAKS_D if is_h else C_FAKTA_D))
    c.alignment=CA(); c.border=BD

# ─────────────────────────────────────────────────────────────────────────────
# SHEET 5 – POSTERIOR (BERITA UJI) — INPUT BEBAS (CUSTOM TEXT)
# ─ Desain:
#   • C2:G2 = Sel input teks berita uji bebas (ketik kalimat apa saja!)
#   • C3:G3 = Preprocessing otomatis (lowercase, hapus angka & simbol)
#   • Baris 6-21 = Tabel token (16 slot) → pecah kata dari C3 via MID+SUBSTITUTE+REPT
#   • Stopword & kata <=2 huruf otomatis dilewati (tidak masuk hitungan SUM/COUNT)
#   • Kata valid otomatis VLOOKUP ke Sheet 4 (kata baru/OOV memakai Laplace 1/(N+V))
#   • Baris 50+ = Daftar Stopwords (dirujuk oleh COUNTIF untuk filter kata)
# ─────────────────────────────────────────────────────────────────────────────
ws5 = wb.create_sheet("5. Posterior (Berita Uji)")
ws5.sheet_view.showGridLines = False

N_ROWS = 16   # kapasitas hingga 16 kata dari input bebas
DATA_ROW_START = 6
last_data_row  = DATA_ROW_START + N_ROWS - 1

# ── Baris helper Stopwords (row 50+) ─────────────────────────────────────────
HELPER_ROW_START = 50
sorted_stops = sorted(STOPWORDS)
SW_START = HELPER_ROW_START + 1
SW_END   = HELPER_ROW_START + len(sorted_stops)
SW_RANGE = f"$A${SW_START}:$A${SW_END}"

ws5.merge_cells(f"A{HELPER_ROW_START-1}:G{HELPER_ROW_START-1}")
ws5.cell(HELPER_ROW_START-1,1).value = (
    "🗂  Daftar Stopwords (dirujuk oleh COUNTIF di tabel atas agar kata hubung otomatis dilewati)")
ws5.cell(HELPER_ROW_START-1,1).fill = fill(C_HEADER)
ws5.cell(HELPER_ROW_START-1,1).font = bfnt("FFFFFF", 9)
ws5.cell(HELPER_ROW_START-1,1).alignment = LA()
ws5.row_dimensions[HELPER_ROW_START-1].height = 20

ws5.cell(HELPER_ROW_START, 1, "Stopword").fill = fill(C_GRAY_L)
ws5.cell(HELPER_ROW_START, 1).font = bfnt(sz=9)
ws5.cell(HELPER_ROW_START, 1).alignment = CA(); ws5.cell(HELPER_ROW_START, 1).border = BD

for idx_sw, sw in enumerate(sorted_stops):
    r_sw = SW_START + idx_sw
    c_sw = ws5.cell(r_sw, 1, sw)
    c_sw.fill = fill(C_GRAY_L); c_sw.font = rfnt(sz=9)
    c_sw.alignment = CA(); c_sw.border = BD

# ── Bagian utama sheet ───────────────────────────────────────────────────────
title_row(ws5, "A1:G1", "🧮  Perhitungan Posterior Naive Bayes (Input Berita Bebas)")

# Baris 2: Input bebas judul/isi berita
ws5.merge_cells("A2:B2")
ws5["A2"].value = "✏️  Ketik Berita Uji (Bebas):"
ws5["A2"].fill=fill(C_GRAY_L); ws5["A2"].font=bfnt(sz=10)
ws5["A2"].alignment=LA(); ws5["A2"].border=BD

ws5.merge_cells("C2:G2")
c_inp = ws5["C2"]
c_inp.value     = RAW[4][1]   # Default: contoh berita hoaks ke-5, bebas diganti user
c_inp.fill      = fill(C_YELLOW)
c_inp.font      = Font(bold=True, size=11, color=C_HEADER)
c_inp.alignment = LA(wrap=True); c_inp.border = BDT
ws5.row_dimensions[2].height = 34

# Baris 3: Hasil preprocessing otomatis dari $C$2
ws5.merge_cells("A3:B3")
ws5["A3"].value = "Hasil Preprocessing (Otomatis):"
ws5["A3"].fill=fill(C_GRAY_L); ws5["A3"].font=bfnt(sz=9)
ws5["A3"].alignment=LA(); ws5["A3"].border=BD

sub_inp = "CLEAN(LOWER($C$2))"
for d in "0123456789":
    sub_inp = f"SUBSTITUTE({sub_inp},{chr(34)}{d}{chr(34)},{chr(34)} {chr(34)})"
for sym in [".", ",", "!", "?", "(", ")", "/", "-", ":", ";"]:
    sub_inp = f"SUBSTITUTE({sub_inp},{chr(34)}{sym}{chr(34)},{chr(34)} {chr(34)})"

ws5.merge_cells("C3:G3")
c_pp = ws5["C3"]
c_pp.value     = f"=TRIM({sub_inp})"
c_pp.fill      = fill("FDFEFE"); c_pp.font = Font(italic=True, size=9, color="333333")
c_pp.alignment = LA(wrap=True); c_pp.border = BD
ws5.row_dimensions[3].height = 28

# Baris 4: Info rumus
ws5.merge_cells("A4:G4")
ws5["A4"].value = ("Rumus: log P(Hoaks|d) = LN(0.5) + Σ LN(P(wᵢ|Hoaks))  "
                   "|  Ketik kalimat apa saja di sel kuning C2 (maks 16 kata) → hasil hitung otomatis!")
ws5["A4"].fill=fill(C_GRAY_L); ws5["A4"].font=Font(italic=True,size=9,color="555555")
ws5["A4"].alignment=CA()
ws5.row_dimensions[4].height=22

# ── Header tabel token ────────────────────────────────────────────────────────
hdrs5 = ["Token (kata)","P(w|Fakta)","P(w|Hoaks)",
          "log P(w|Fakta)","log P(w|Hoaks)","Berkontribusi ke","Selisih log"]
for c_i, h in enumerate(hdrs5, 1):
    hdr(ws5, DATA_ROW_START-1, c_i, h, sz=9, wrap=True)
ws5.row_dimensions[DATA_ROW_START-1].height = 30

col_w(ws5,1,20); col_w(ws5,2,15); col_w(ws5,3,15)
col_w(ws5,4,17); col_w(ws5,5,17); col_w(ws5,6,22); col_w(ws5,7,14)
col_w(ws5,8,3)   # spacer

# ── Referensi VLOOKUP ke Sheet 4 ─────────────────────────────────────────────
S4_RANGE = f"'4. Likelihood (Laplace)'!$A$9:$G${9+V-1}"
FALLBACK_PF = "1/('4. Likelihood (Laplace)'!$B$5+'4. Likelihood (Laplace)'!$D$5)"
FALLBACK_PH = "1/('4. Likelihood (Laplace)'!$H$5+'4. Likelihood (Laplace)'!$D$5)"

# ── Data rows: 16 slot token (N_ROWS) ────────────────────────────────────────
for pos in range(1, N_ROWS + 1):
    r = DATA_ROW_START + pos - 1

    # Kolom A: Pecah kata ke-pos dari hasil preprocessing $C$3 (kompatibel semua versi Excel)
    split_formula = (f'=TRIM(MID(SUBSTITUTE($C$3," ",REPT(" ",100)),{(pos-1)*100+1},100))')
    c = ws5.cell(r, 1); c.value = split_formula
    c.fill=fill(C_GRAY_L); c.font=Font(bold=True, size=10)
    c.alignment=CA(); c.border=BD

    # Kondisi skip: kosong, panjang <= 2, atau masuk daftar stopword
    skip_cond = f'OR(A{r}="",LEN(A{r})<=2,COUNTIF({SW_RANGE},A{r})>0)'

    # Kolom B & C: P(w|Fakta) dan P(w|Hoaks)
    for target_col, vlookup_col, fg, fallback in [
        (2, 4, C_FAKTA_D, FALLBACK_PF),
        (3, 5, C_HOAKS_D, FALLBACK_PH),
    ]:
        c = ws5.cell(r, target_col)
        c.value = (f'=IF({skip_cond},"",IFERROR(VLOOKUP(A{r},{S4_RANGE},{vlookup_col},0),{fallback}))')
        c.fill=fill(C_GRAY_L); c.font=Font(size=10, color=fg)
        c.alignment=CA(); c.border=BD; c.number_format="0.000000"

    # D: log P(w|Fakta)
    c = ws5.cell(r, 4); c.value = f'=IF({skip_cond},"",LN(B{r}))'
    c.fill=fill(C_FAKTA_L); c.font=Font(size=10, color=C_FAKTA_D)
    c.alignment=CA(); c.border=BD; c.number_format="0.0000"

    # E: log P(w|Hoaks)
    c = ws5.cell(r, 5); c.value = f'=IF({skip_cond},"",LN(C{r}))'
    c.fill=fill(C_HOAKS_L); c.font=Font(size=10, color=C_HOAKS_D)
    c.alignment=CA(); c.border=BD; c.number_format="0.0000"

    # F: Berkontribusi ke
    c = ws5.cell(r, 6)
    c.value = f'=IF(A{r}="","",IF(OR(LEN(A{r})<=2,COUNTIF({SW_RANGE},A{r})>0),"(stopword/pendek)",IF(E{r}>D{r},"→ HOAKS","→ FAKTA")))'
    c.fill=fill(C_GRAY_L); c.font=Font(bold=True, size=10)
    c.alignment=CA(); c.border=BD

    # G: Selisih log
    c = ws5.cell(r, 7); c.value = f'=IF({skip_cond},"",E{r}-D{r})'
    c.fill=fill(C_GRAY_L); c.font=Font(size=10)
    c.alignment=CA(); c.border=BD; c.number_format="0.0000"

    ws5.row_dimensions[r].height = 22

# Conditional formatting: warnai baris berdasarkan selisih log (kolom G)
# Menggunakan ISNUMBER($G6) agar sel kosong/teks tidak ikut terwarnai
ws5.conditional_formatting.add(
    f"A{DATA_ROW_START}:G{last_data_row}",
    FormulaRule(
        formula=[f'AND(ISNUMBER($G{DATA_ROW_START}),$G{DATA_ROW_START}>0)'],
        fill=fill(C_HOAKS_L)
    )
)
ws5.conditional_formatting.add(
    f"A{DATA_ROW_START}:G{last_data_row}",
    FormulaRule(
        formula=[f'AND(ISNUMBER($G{DATA_ROW_START}),$G{DATA_ROW_START}<0)'],
        fill=fill(C_GREEN_L)
    )
)

# ── Summary: Σ log-posterior & probabilitas ───────────────────────────────────
SUM_D = f"D{DATA_ROW_START}:D{last_data_row}"
SUM_E = f"E{DATA_ROW_START}:E{last_data_row}"

nr  = last_data_row + 2
nr2 = nr + 1
nr3 = nr + 2
nr4 = nr + 3

# Header summary
ws5.merge_cells(f"A{nr}:C{nr}")
ws5.cell(nr,1,"▼  Akumulasi Log-Posterior").fill=fill(C_HEADER)
ws5.cell(nr,1).font=bfnt("FFFFFF",11); ws5.cell(nr,1).alignment=LA(); ws5.cell(nr,1).border=BD
hdr(ws5,nr,4,"log P(Fakta|d)",bg=C_FAKTA_M,sz=10)
hdr(ws5,nr,5,"log P(Hoaks|d)",bg=C_HOAKS_M,sz=10)
ws5.merge_cells(f"F{nr}:G{nr}")
ws5.cell(nr,6,"Rumus").fill=fill(C_GRAY_L); ws5.cell(nr,6).font=bfnt(sz=9)
ws5.cell(nr,6).alignment=CA(); ws5.cell(nr,6).border=BD
ws5.row_dimensions[nr].height=24

# Jumlah token aktif = COUNT(D_range) karena hanya token valid berisi angka
ws5.merge_cells(f"A{nr2}:C{nr2}")
ws5.cell(nr2,1,"Jumlah token aktif (non-stopword)").fill=fill(C_GRAY_L)
ws5.cell(nr2,1).font=rfnt(sz=10); ws5.cell(nr2,1).alignment=LA(); ws5.cell(nr2,1).border=BD
c=ws5.cell(nr2,4)
c.value=f'=COUNT({SUM_D})'
c.fill=fill(C_FAKTA_L); c.font=bfnt(C_FAKTA_D,11); c.alignment=CA(); c.border=BD
c=ws5.cell(nr2,5)
c.value=f'=COUNT({SUM_E})'
c.fill=fill(C_HOAKS_L); c.font=bfnt(C_HOAKS_D,11); c.alignment=CA(); c.border=BD
ws5.merge_cells(f"F{nr2}:G{nr2}")
ws5.cell(nr2,6,f'COUNT(D{DATA_ROW_START}:D{last_data_row})').fill=fill(C_GRAY_L)
ws5.cell(nr2,6).font=Font(italic=True,size=9,color="555555"); ws5.cell(nr2,6).alignment=CA()
ws5.row_dimensions[nr2].height=24

# Σ log-posterior = SUM kolom D/E (IF kosong → SUM abaikan) + LN(0.5)
ws5.merge_cells(f"A{nr3}:C{nr3}")
ws5.cell(nr3,1,"Σ log P(wᵢ|kelas) + LN(Prior=0.5)").fill=fill(C_GRAY_L)
ws5.cell(nr3,1).font=rfnt(sz=10); ws5.cell(nr3,1).alignment=LA(); ws5.cell(nr3,1).border=BD
c=ws5.cell(nr3,4); c.value=f"=SUM({SUM_D})+LN(0.5)"
c.fill=fill(C_FAKTA_L); c.font=bfnt(C_FAKTA_D,12); c.alignment=CA(); c.border=BD; c.number_format="0.0000"
c=ws5.cell(nr3,5); c.value=f"=SUM({SUM_E})+LN(0.5)"
c.fill=fill(C_HOAKS_L); c.font=bfnt(C_HOAKS_D,12); c.alignment=CA(); c.border=BD; c.number_format="0.0000"
ws5.merge_cells(f"F{nr3}:G{nr3}")
ws5.cell(nr3,6,f"SUM(D{DATA_ROW_START}:D{last_data_row}) + LN(0.5)").fill=fill(C_GRAY_L)
ws5.cell(nr3,6).font=Font(italic=True,size=9,color="555555"); ws5.cell(nr3,6).alignment=CA()
ws5.row_dimensions[nr3].height=26

# Probabilitas Softmax
ws5.merge_cells(f"A{nr4}:C{nr4}")
ws5.cell(nr4,1,"Probabilitas  (EXP / Softmax)").fill=fill(C_GRAY_L)
ws5.cell(nr4,1).font=bfnt(sz=10); ws5.cell(nr4,1).alignment=LA(); ws5.cell(nr4,1).border=BD

D_post = f"D{nr3}"; E_post = f"E{nr3}"
max_ref = f"MAX({D_post},{E_post})"

c=ws5.cell(nr4,4)
c.value=(f"=EXP({D_post}-{max_ref})/(EXP({D_post}-{max_ref})+EXP({E_post}-{max_ref}))")
c.fill=fill(C_FAKTA_L); c.font=bfnt(C_FAKTA_D,13); c.alignment=CA(); c.border=BD; c.number_format="0.00%"

c=ws5.cell(nr4,5)
c.value=(f"=EXP({E_post}-{max_ref})/(EXP({D_post}-{max_ref})+EXP({E_post}-{max_ref}))")
c.fill=fill(C_HOAKS_L); c.font=bfnt(C_HOAKS_D,13); c.alignment=CA(); c.border=BD; c.number_format="0.00%"

ws5.merge_cells(f"F{nr4}:G{nr4}")
ws5.cell(nr4,6,"EXP(logH-MAX) / (EXP(logF-MAX)+EXP(logH-MAX))").fill=fill(C_GRAY_L)
ws5.cell(nr4,6).font=Font(italic=True,size=9,color="555555"); ws5.cell(nr4,6).alignment=CA()
ws5.row_dimensions[nr4].height=28

# Verdict banner
nr5 = nr4 + 1
ws5.merge_cells(f"A{nr5}:G{nr5}")
verd = (f'=IF(E{nr4}>D{nr4},'
        f'"⚡ PREDIKSI: HOAKS  ("&TEXT(E{nr4},"0.0%")&" probabilitas hoaks)",'
        f'"⚡ PREDIKSI: FAKTA  ("&TEXT(D{nr4},"0.0%")&" probabilitas fakta)")')
c=ws5.cell(nr5,1); c.value=verd
c.fill=fill(C_HOAKS_D); c.font=Font(bold=True,size=14,color="FFFFFF")
c.alignment=CA(); c.border=BDT
ws5.row_dimensions[nr5].height=36

# Footnote
nr6 = nr5 + 1
ws5.merge_cells(f"A{nr6}:G{nr6}")
ws5.cell(nr6,1).value = (
    "💡 Cara pakai: ketik kalimat berita apa saja di sel kuning C2 (maks 16 kata) → C3 membersihkan teks otomatis, "
    "kolom A memecah kata via MID+SUBSTITUTE+REPT, stopword dilewati, dan probabilitas dihitung otomatis!")
ws5.cell(nr6,1).fill=fill("FDFEFE")
ws5.cell(nr6,1).font=Font(italic=True,size=9,color="555555")
ws5.cell(nr6,1).alignment=LA()
ws5.row_dimensions[nr6].height=26

# ─────────────────────────────────────────────────────────────────────────────
# SHEET 6 – KONTRIBUSI KATA (DASAR HIGHLIGHT MERAH/HIJAU)
# Kontribusi = log P(w|Hoaks) - log P(w|Fakta)
# = LN(P_hoaks) - LN(P_fakta) = G{s4r} - F{s4r}  (from sheet 4)
# ─────────────────────────────────────────────────────────────────────────────
ws6 = wb.create_sheet("6. Kontribusi Kata (Highlight)")
ws6.sheet_view.showGridLines = False

title_row(ws6, "A1:H1", "🔴🟢  Kontribusi Kata – Dasar Highlight Merah/Hijau di Aplikasi")

ws6.merge_cells("A2:H2")
ws6["A2"].value = ("Kontribusi = log P(w|Hoaks) − log P(w|Fakta) = LN(P_hoaks) − LN(P_fakta)  "
                   "|  Positif (merah) → mendorong HOAKS  |  Negatif (hijau) → mendorong FAKTA")
ws6["A2"].fill=fill(C_GRAY_L); ws6["A2"].font=Font(italic=True,size=10,color="555555")
ws6["A2"].alignment=CA()

hdrs6 = ["Kata","count(Fakta)","count(Hoaks)",
          "log P(w|Fakta)","log P(w|Hoaks)",
          "Kontribusi Hoaks\n=logH−logF","Arah","Warna di App"]
for c_i, h in enumerate(hdrs6, 1):
    hdr(ws6, 3, c_i, h, sz=9, wrap=True)
ws6.row_dimensions[3].height=36

col_w(ws6,1,22); col_w(ws6,2,14); col_w(ws6,3,14)
col_w(ws6,4,18); col_w(ws6,5,18)
col_w(ws6,6,22); col_w(ws6,7,16); col_w(ws6,8,16)

# Sheet 4 vocab rows start at row 9
for i, word in enumerate(vocab):
    r   = i + 4
    s4r = i + 9
    is_h = word in hoaks_toks and word not in fakta_toks
    is_f = word in fakta_toks and word not in hoaks_toks
    bg   = C_HOAKS_L if is_h else C_FAKTA_L if is_f else C_YELLOW

    S4 = "'4. Likelihood (Laplace)'"

    # Kata
    c=ws6.cell(r,1); c.value=f"={S4}!A{s4r}"
    c.fill=fill(bg); c.font=Font(bold=True,size=10); c.alignment=LA(); c.border=BD

    # count Fakta
    c=ws6.cell(r,2); c.value=f"={S4}!B{s4r}"
    c.fill=fill(bg); c.font=Font(size=10,color=C_FAKTA_D)
    c.alignment=CA(); c.border=BD

    # count Hoaks
    c=ws6.cell(r,3); c.value=f"={S4}!C{s4r}"
    c.fill=fill(bg); c.font=Font(size=10,color=C_HOAKS_D)
    c.alignment=CA(); c.border=BD

    # log P(w|Fakta) = Sheet4!F{s4r}
    c=ws6.cell(r,4); c.value=f"={S4}!F{s4r}"
    c.fill=fill(bg); c.font=Font(size=10,color=C_FAKTA_D)
    c.alignment=CA(); c.border=BD; c.number_format="0.0000"

    # log P(w|Hoaks) = Sheet4!G{s4r}
    c=ws6.cell(r,5); c.value=f"={S4}!G{s4r}"
    c.fill=fill(bg); c.font=Font(size=10,color=C_HOAKS_D)
    c.alignment=CA(); c.border=BD; c.number_format="0.0000"

    # Kontribusi = E{r} - D{r}
    c=ws6.cell(r,6); c.value=f"=E{r}-D{r}"
    is_h_actual = word in hoaks_toks and word not in fakta_toks
    c.fill=fill(bg)
    c.font=Font(bold=True,size=11,
                color=(C_HOAKS_D if is_h_actual else
                       C_GREEN_D if is_f else "111111"))
    c.alignment=CA(); c.border=BD; c.number_format="0.0000"

    # Arah = IF(F{r}>0,"→ HOAKS","→ FAKTA")
    c=ws6.cell(r,7); c.value=f'=IF(F{r}>0,"→ HOAKS","→ FAKTA")'
    c.fill=fill(bg)
    c.font=Font(bold=True,size=10,
                color=(C_HOAKS_D if is_h_actual else C_GREEN_D if is_f else "111111"))
    c.alignment=CA(); c.border=BD

    # Warna di App = IF(F{r}>0,"🔴 MERAH","🟢 HIJAU")
    c=ws6.cell(r,8); c.value=f'=IF(F{r}>0,"🔴 MERAH","🟢 HIJAU")'
    c.fill=fill(bg)
    c.font=Font(bold=True,size=10,
                color=(C_HOAKS_D if is_h_actual else C_GREEN_D if is_f else "111111"))
    c.alignment=CA(); c.border=BD

# Color scale conditional formatting on column F (kontribusi)
last_r6 = len(vocab) + 3
ws6.conditional_formatting.add(
    f"F4:F{last_r6}",
    ColorScaleRule(
        start_type="min",  start_color="1E8449",   # green (fakta)
        mid_type="num",    mid_value=0, mid_color="FFFFFF",
        end_type="max",    end_color="C0392B"       # red (hoaks)
    )
)

# Bar chart – Top 10 contributors
chart_r = last_r6 + 3
ws6.cell(chart_r, 1, "Kata").font=bfnt(); ws6.cell(chart_r,1).border=BD
ws6.cell(chart_r, 2, "Kontribusi").font=bfnt(); ws6.cell(chart_r,2).border=BD

# Sort vocab by kontribusi (pre-computed for chart data only)
from collections import Counter as _C
_freq_f = _C(fakta_toks); _freq_h = _C(hoaks_toks)
_pf = lambda w: (_freq_f[w]+1)/(N_F+V)
_ph = lambda w: (_freq_h[w]+1)/(N_H+V)
_contrib = {w: math.log(_ph(w))-math.log(_pf(w)) for w in vocab}
top5h = sorted(vocab, key=lambda w: _contrib[w], reverse=True)[:5]
top5f = sorted(vocab, key=lambda w: _contrib[w])[:5]
chart_words = top5h + top5f

for j, w in enumerate(chart_words):
    ws6.cell(chart_r+1+j, 1, w)
    ws6.cell(chart_r+1+j, 2, round(_contrib[w], 4))

chart6 = BarChart()
chart6.type   = "bar"
chart6.title  = "Top 5 Kata HOAKS vs Top 5 Kata FAKTA (Kontribusi Hoaks)"
chart6.y_axis.title = "Kontribusi Hoaks"
chart6.x_axis.title = "Kata"
chart6.width  = 22; chart6.height = 14

cats = Reference(ws6, min_col=1, min_row=chart_r+1, max_row=chart_r+len(chart_words))
data = Reference(ws6, min_col=2, min_row=chart_r,   max_row=chart_r+len(chart_words))
chart6.add_data(data, titles_from_data=True)
chart6.set_categories(cats)
chart6.series[0].graphicalProperties.solidFill = "2980B9"
ws6.add_chart(chart6, f"A{chart_r+len(chart_words)+2}")

# ─────────────────────────────────────────────────────────────────────────────
# SHEET 7 – EVALUASI MODEL
# ─────────────────────────────────────────────────────────────────────────────
ws7 = wb.create_sheet("7. Evaluasi Model")
ws7.sheet_view.showGridLines = False

title_row(ws7, "A1:F1", "📈  Evaluasi Model Nyata (TF-IDF + Multinomial Naive Bayes)")
sub_row  (ws7, "A2:F2",
          "Hasil dari train_model.py pada dataset 23.632 berita (11.816 Hoaks + 11.816 Fakta)")

metrics = {}
if os.path.exists(METRICS_PATH):
    with open(METRICS_PATH, encoding="utf-8") as f:
        metrics = json.load(f)

col_w(ws7,1,26); col_w(ws7,2,22); col_w(ws7,3,5)
col_w(ws7,4,22); col_w(ws7,5,22); col_w(ws7,6,5)

if metrics:
    # Metric table
    hdr(ws7,3,1,"Metrik",sz=11); hdr(ws7,3,2,"Nilai",sz=11)
    ws7.row_dimensions[3].height=24

    metric_rows = [
        ("Accuracy",        metrics.get("accuracy","N/A"),    C_GREEN_L,  C_GREEN_D),
        ("Precision",       metrics.get("precision","N/A"),   C_FAKTA_L,  C_FAKTA_D),
        ("Recall",          metrics.get("recall","N/A"),      C_FAKTA_L,  C_FAKTA_D),
        ("F1-Score",        metrics.get("f1_score","N/A"),    C_GREEN_L,  C_GREEN_D),
        ("CV F1 (5-fold)",
         f'{metrics.get("cv_f1_mean","?")} ± {metrics.get("cv_f1_std","?")}',
                                                               C_YELLOW,   C_GOLD),
        ("Train Size",      metrics.get("n_train","N/A"),     C_GRAY_L,   "111111"),
        ("Test  Size",      metrics.get("n_test","N/A"),      C_GRAY_L,   "111111"),
        ("Vocab Size (model)",metrics.get("n_vocab","N/A"),   C_GRAY_L,   "111111"),
    ]
    for i,(name,val,bg,fg) in enumerate(metric_rows):
        r = i+4
        dat(ws7,r,1,name,bg=bg,bold=True,fg=fg,sz=11)
        dat(ws7,r,2,val, bg=bg,bold=True,fg=fg,sz=12,align="C")
        ws7.row_dimensions[r].height=26

    # Confusion matrix
    cm = metrics.get("confusion_matrix",[[0,0],[0,0]])
    hdr(ws7,3,4,"Confusion Matrix",bg=C_HEADER,sz=11)
    ws7.merge_cells("D3:F3")

    hdr(ws7,4,4,"",bg=C_GRAY_L,fg="000000")
    hdr(ws7,4,5,"Pred: Fakta",bg=C_FAKTA_M)
    hdr(ws7,4,6,"Pred: Hoaks",bg=C_HOAKS_M)
    ws7.row_dimensions[4].height=24

    row_lbl = ["Aktual: Fakta","Aktual: Hoaks"]
    cm_bg   = [[C_GREEN_L, C_HOAKS_L],[C_HOAKS_L, C_GREEN_L]]
    for ri,row_v in enumerate(cm):
        r = 5+ri
        dat(ws7,r,4,row_lbl[ri],bg=(C_FAKTA_L if ri==0 else C_HOAKS_L),
            bold=True,fg=(C_FAKTA_D if ri==0 else C_HOAKS_D))
        for ci,v in enumerate(row_v):
            cell=ws7.cell(r,5+ci,v)
            cell.fill=fill(cm_bg[ri][ci])
            cell.font=Font(bold=True,size=14,
                           color=(C_GREEN_D if ri==ci else C_HOAKS_D))
            cell.alignment=CA(); cell.border=BD
        ws7.row_dimensions[r].height=30

    ws7.merge_cells("D7:F7")
    ws7["D7"].value="🟢 Diagonal = Prediksi Benar  |  🔴 Off-diagonal = Prediksi Salah"
    ws7["D7"].fill=fill("FDFEFE"); ws7["D7"].font=Font(italic=True,size=9,color="555555")
    ws7["D7"].alignment=CA()

    # CV fold scores
    cv_scores = metrics.get("cv_f1_per_fold",[])
    if cv_scores:
        hdr(ws7,10,1,"5-Fold Cross-Validation F1",sz=10)
        ws7.merge_cells("A10:B10")
        for fi,sc in enumerate(cv_scores):
            hdr(ws7,10,3+fi,f"Fold {fi+1}",sz=9)
            dat(ws7,11,3+fi,sc,bg=C_GREEN_L,bold=True,fg=C_GREEN_D,sz=11,align="C")
            ws7.row_dimensions[11].height=26

        avg_col = 3+len(cv_scores)
        hdr(ws7,10,avg_col,"Rata-rata",sz=9)
        c=ws7.cell(11,avg_col)
        c.value = f"=AVERAGE({get_column_letter(3)}11:{get_column_letter(avg_col-1)}11)"
        c.fill=fill(C_YELLOW); c.font=bfnt(C_GOLD,12); c.alignment=CA(); c.border=BD
        c.number_format="0.0000"

else:
    ws7.merge_cells("A4:F4")
    ws7["A4"].value = "⚠  metrics.json tidak ditemukan – jalankan train_model.py dahulu"
    ws7["A4"].fill=fill("FDFCBF"); ws7["A4"].font=bfnt(C_GOLD,11); ws7["A4"].alignment=CA()

# ─────────────────────────────────────────────────────────────────────────────
# Save
# ─────────────────────────────────────────────────────────────────────────────
wb.save(OUT_PATH)
print(f"\n✅  Excel berhasil disimpan: {OUT_PATH}")
print("Sheet yang dibuat:")
for s in wb.sheetnames:
    print(f"  • {s}")
print("\nFormula Excel yang digunakan:")
print("  =LOWER()           – Sheet 2 Preprocessing")
print("  =SUBSTITUTE()      – Sheet 2 hapus angka/simbol")
print("  =LEN()-LEN(SUBSTITUTE())  – Sheet 2 hitung token")
print("  =COUNTIF(*kata*)   – Sheet 3 frekuensi per kelas")
print("  =(count+1)/(N+V)   – Sheet 4 Laplace likelihood")
print("  =LN()              – Sheet 4 & 5 log-likelihood")
print("  =VLOOKUP()         – Sheet 5 ambil likelihood dari sheet 4")
print("  =SUM()+LN(0.5)     – Sheet 5 log-posterior total")
print("  =EXP()/normalisasi – Sheet 5 softmax probabilitas")
print("  =IF(E>D,HOAKS,FAKTA) – Sheet 4,5,6 label arah")
print("  =LN(P_hoaks)-LN(P_fakta) – Sheet 6 kontribusi highlight")
print("  =AVERAGE()         – Sheet 7 rata-rata CV folds")

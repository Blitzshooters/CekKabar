"""
CekKabar – Hoax Detector Modelling
===================================
Pipeline: TF-IDF + Multinomial Naive Bayes
Dataset : Indonesian Hoax News Detection (Antara/Kompas/Detik + TurnBackHoax)

Output files (all written to modelling/):
  - model_nb.pkl          : trained pipeline (TfidfVectorizer + MultinomialNB)
  - label_encoder.pkl     : (not needed; labels are already 0/1)
  - word_contributions.csv: log P(w|hoaks) - log P(w|fakta) per vocab word
  - metrics.json          : accuracy, precision, recall, F1, confusion matrix
  - classification_report.txt
"""

import os
import json
import pickle
import warnings
warnings.filterwarnings("ignore")

import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import Pipeline
from sklearn.model_selection import train_test_split, StratifiedKFold, cross_val_score
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score, f1_score,
    confusion_matrix, classification_report
)

# ─── Paths ────────────────────────────────────────────────────────────────────
BASE_DIR  = os.path.dirname(os.path.abspath(__file__))
DATA_DIR  = os.path.join(BASE_DIR, "Dataset Deteksi Berita Hoaks Indo", "Cleaned Dataset V3")

# ─── 1. Load & unify datasets ─────────────────────────────────────────────────
print("=" * 60)
print("1. LOADING DATASETS")
print("=" * 60)

# Fakta sources (label = 0)
dfs_fakta = []
for fname in ["antaranews_cleaned_v3.csv", "detik_cleaned_v3.csv", "kompas_cleaned_v3.csv"]:
    df = pd.read_csv(os.path.join(DATA_DIR, fname), usecols=["clean_text", "label"])
    df = df.rename(columns={"label": "label_raw"})
    df["label"] = df["label_raw"].astype(int)   # 0 = fakta
    dfs_fakta.append(df[["clean_text", "label"]])
    print(f"  {fname}: {len(df)} rows | labels: {df['label'].value_counts().to_dict()}")

# Hoaks source (hoax = 1)
df_tbh = pd.read_csv(os.path.join(DATA_DIR, "tbh_cleaned_v3.csv"), usecols=["clean_text", "hoax"])
df_tbh = df_tbh.rename(columns={"hoax": "label"})
df_tbh["label"] = df_tbh["label"].astype(int)
print(f"  tbh_cleaned_v3.csv: {len(df_tbh)} rows | labels: {df_tbh['label'].value_counts().to_dict()}")

df_all = pd.concat(dfs_fakta + [df_tbh], ignore_index=True)
df_all = df_all.dropna(subset=["clean_text", "label"])
df_all["clean_text"] = df_all["clean_text"].astype(str).str.strip()
df_all = df_all[df_all["clean_text"].str.len() > 10]

print(f"\nTotal setelah merge: {len(df_all)} rows")
print(f"Distribusi label:\n{df_all['label'].value_counts()}")

# ─── 2. Balance dataset ───────────────────────────────────────────────────────
print("\n" + "=" * 60)
print("2. BALANCING DATASET")
print("=" * 60)

n_hoaks = df_all[df_all["label"] == 1].shape[0]
n_fakta = df_all[df_all["label"] == 0].shape[0]
n_min   = min(n_hoaks, n_fakta)

df_hoaks = df_all[df_all["label"] == 1].sample(n_min, random_state=42)
df_fakta = df_all[df_all["label"] == 0].sample(n_min, random_state=42)
df_bal   = pd.concat([df_hoaks, df_fakta]).sample(frac=1, random_state=42).reset_index(drop=True)

print(f"  Hoaks : {n_min}")
print(f"  Fakta : {n_min}")
print(f"  Total : {len(df_bal)}")

# ─── 3. Train / Test split ────────────────────────────────────────────────────
print("\n" + "=" * 60)
print("3. TRAIN / TEST SPLIT (80/20)")
print("=" * 60)

X = df_bal["clean_text"]
y = df_bal["label"]

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)
print(f"  Train : {len(X_train)} | Test : {len(X_test)}")

# ─── 4. Build & train pipeline ────────────────────────────────────────────────
print("\n" + "=" * 60)
print("4. TRAINING  TF-IDF + Multinomial Naive Bayes")
print("=" * 60)

pipeline = Pipeline([
    ("tfidf", TfidfVectorizer(
        max_features=20000,
        ngram_range=(1, 2),
        sublinear_tf=True,
        min_df=2,
        max_df=0.95
    )),
    ("nb", MultinomialNB(alpha=1.0))   # alpha=1 → Laplace smoothing
])

pipeline.fit(X_train, y_train)
print("  Pipeline berhasil dilatih.")

# ─── 5. Evaluate ──────────────────────────────────────────────────────────────
print("\n" + "=" * 60)
print("5. EVALUASI")
print("=" * 60)

y_pred = pipeline.predict(X_test)
y_prob = pipeline.predict_proba(X_test)[:, 1]   # prob hoaks

acc  = accuracy_score(y_test, y_pred)
prec = precision_score(y_test, y_pred)
rec  = recall_score(y_test, y_pred)
f1   = f1_score(y_test, y_pred)
cm   = confusion_matrix(y_test, y_pred).tolist()

print(f"  Accuracy  : {acc:.4f}")
print(f"  Precision : {prec:.4f}")
print(f"  Recall    : {rec:.4f}")
print(f"  F1-Score  : {f1:.4f}")
print(f"  Confusion Matrix:\n{np.array(cm)}")

# 5-fold cross-validation
cv_scores = cross_val_score(pipeline, X, y, cv=5, scoring="f1")
print(f"\n  5-Fold CV F1 : {cv_scores.mean():.4f} ± {cv_scores.std():.4f}")
print(f"  Per-fold     : {[round(s,4) for s in cv_scores]}")

report = classification_report(y_test, y_pred, target_names=["Fakta", "Hoaks"])
print(f"\n{report}")

# ─── 6. Word contributions ────────────────────────────────────────────────────
print("\n" + "=" * 60)
print("6. WORD CONTRIBUTIONS  (log P(w|Hoaks) - log P(w|Fakta))")
print("=" * 60)

tfidf = pipeline.named_steps["tfidf"]
nb    = pipeline.named_steps["nb"]

feature_names = np.array(tfidf.get_feature_names_out())
# log prob per class: shape (n_classes, n_features)
log_prob = nb.feature_log_prob_          # [fakta_idx=0, hoaks_idx=1]
# contribution score = log P(w|Hoaks) - log P(w|Fakta)
contrib = log_prob[1] - log_prob[0]

word_contrib_df = pd.DataFrame({
    "kata"            : feature_names,
    "log_p_hoaks"     : log_prob[1],
    "log_p_fakta"     : log_prob[0],
    "kontribusi_hoaks": contrib          # positif → condong hoaks
}).sort_values("kontribusi_hoaks", ascending=False)

# Save full
contrib_path = os.path.join(BASE_DIR, "word_contributions.csv")
word_contrib_df.to_csv(contrib_path, index=False)
print(f"  Disimpan ke: {contrib_path}")
print(f"\n  Top 10 kata HOAKS:")
print(word_contrib_df.head(10)[["kata","kontribusi_hoaks"]].to_string(index=False))
print(f"\n  Top 10 kata FAKTA (kontribusi negatif):")
print(word_contrib_df.tail(10)[["kata","kontribusi_hoaks"]].to_string(index=False))

# ─── 7. Save artefacts ────────────────────────────────────────────────────────
print("\n" + "=" * 60)
print("7. MENYIMPAN ARTEFAK")
print("=" * 60)

# Pipeline
model_path = os.path.join(BASE_DIR, "model_nb.pkl")
with open(model_path, "wb") as f:
    pickle.dump(pipeline, f)
print(f"  model_nb.pkl            → {model_path}")

# Metrics
metrics = {
    "accuracy" : round(acc,  4),
    "precision": round(prec, 4),
    "recall"   : round(rec,  4),
    "f1_score" : round(f1,   4),
    "cv_f1_mean": round(float(cv_scores.mean()), 4),
    "cv_f1_std" : round(float(cv_scores.std()),  4),
    "cv_f1_per_fold": [round(float(s), 4) for s in cv_scores],
    "confusion_matrix": cm,
    "n_train"  : int(len(X_train)),
    "n_test"   : int(len(X_test)),
    "n_vocab"  : int(len(feature_names)),
    "class_names": ["Fakta (0)", "Hoaks (1)"]
}
metrics_path = os.path.join(BASE_DIR, "metrics.json")
with open(metrics_path, "w", encoding="utf-8") as f:
    json.dump(metrics, f, indent=2, ensure_ascii=False)
print(f"  metrics.json            → {metrics_path}")

# Classification report
report_path = os.path.join(BASE_DIR, "classification_report.txt")
with open(report_path, "w", encoding="utf-8") as f:
    f.write(f"CekKabar – Hoax Detector\n")
    f.write(f"Model: TF-IDF (max_features=20000, ngram=(1,2)) + MultinomialNB (alpha=1.0)\n\n")
    f.write(f"Train size : {len(X_train)}\n")
    f.write(f"Test  size : {len(X_test)}\n")
    f.write(f"Vocab size : {len(feature_names)}\n\n")
    f.write(report)
    f.write(f"\nConfusion Matrix:\n{np.array(cm)}\n")
    f.write(f"\n5-Fold CV F1 : {cv_scores.mean():.4f} ± {cv_scores.std():.4f}\n")
    f.write(f"Per-fold     : {[round(s,4) for s in cv_scores]}\n")
print(f"  classification_report.txt → {report_path}")

print("\n" + "=" * 60)
print("SELESAI! Semua artefak berhasil disimpan.")
print("=" * 60)


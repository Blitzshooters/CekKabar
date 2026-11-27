"""
CekKabar – ML Inference & Analytics CLI
=======================================
Dipanggil oleh Laravel backend (HoaxInferenceService) via stdin/stdout JSON.

Mode:
  python infer.py predict      (baca {"text": "..."} dari stdin)
  python infer.py analysis
  python infer.py wordcloud <hoaks|fakta> [query]
  python infer.py evaluation
"""

import json
import os
import pickle
import re
import sys

import numpy as np
import pandas as pd

# ─── Paths ────────────────────────────────────────────────────────────────────
BASE_DIR      = os.path.dirname(os.path.abspath(__file__))
MODELLING_DIR = os.path.abspath(os.path.join(BASE_DIR, "..", "..", "modelling"))
MODEL_PATH    = os.path.join(MODELLING_DIR, "model_nb.pkl")
CONTRIB_PATH  = os.path.join(MODELLING_DIR, "word_contributions.csv")
METRICS_PATH  = os.path.join(MODELLING_DIR, "metrics.json")

STOPWORDS = {
    "yang", "dan", "di", "ke", "dari", "ini", "itu", "dengan", "untuk", "dalam",
    "adalah", "pada", "juga", "oleh", "atau", "akan", "telah", "sudah", "bisa",
    "ada", "tidak", "lebih", "setelah", "saat", "agar", "karena", "pun", "jika",
    "maka", "namun", "nya", "kita", "mereka", "saya", "anda", "kalian", "secara",
    "bagi", "bahwa", "sebagai", "tersebut", "antara", "seperti", "hingga", "sampai",
    "serta", "atau", "ia", "dia", "kami", "para", "terhadap", "atas", "bawah",
}


def load_contrib_map():
    df = pd.read_csv(CONTRIB_PATH)
    vocab_map = {}
    for _, row in df.iterrows():
        kata = str(row["kata"])
        vocab_map[kata] = {
            "score": float(row["kontribusi_hoaks"]),
            "log_p_hoaks": float(row["log_p_hoaks"]),
            "log_p_fakta": float(row["log_p_fakta"]),
        }
    return df, vocab_map


def clean_for_model(text: str) -> str:
    t = text.lower()
    t = re.sub(r"[^a-z0-9\s]", " ", t)
    t = re.sub(r"\s+", " ", t).strip()
    return t


def handle_predict(payload: dict) -> dict:
    raw_text = str(payload.get("text", "")).strip()
    if not raw_text:
        return {"error": "Teks tidak boleh kosong"}

    with open(MODEL_PATH, "rb") as f:
        pipeline = pickle.load(f)

    cleaned = clean_for_model(raw_text)
    probs = pipeline.predict_proba([cleaned])[0]
    p_fakta = float(probs[0])
    p_hoaks = float(probs[1])

    verdict = "HOAKS" if p_hoaks >= 0.5 else "FAKTA"
    hoax_pct = round(p_hoaks * 100, 1)
    fakta_pct = round(p_fakta * 100, 1)
    max_prob = max(p_hoaks, p_fakta)

    if max_prob >= 0.80:
        confidence_label = "TINGKAT KEYAKINAN TINGGI"
    elif max_prob >= 0.65:
        confidence_label = "TINGKAT KEYAKINAN SEDANG"
    else:
        confidence_label = "PERLU VERIFIKASI LANJUT"

    _, vocab_map = load_contrib_map()

    # Tokenize keeping original words & punctuation separated
    raw_tokens = re.findall(r"[A-Za-z0-9]+|[^\w\s]", raw_text)
    tokens_out = []
    word_scores = {}

    for tok in raw_tokens:
        is_word = bool(re.match(r"^[A-Za-z0-9]+$", tok))
        clean_w = tok.lower()
        score = 0.0
        sentiment = "neutral"

        if is_word and clean_w not in STOPWORDS and len(clean_w) > 2:
            if clean_w in vocab_map:
                score = round(vocab_map[clean_w]["score"], 2)
                if score > 0.25:
                    sentiment = "hoaks"
                elif score < -0.25:
                    sentiment = "fakta"

                if clean_w not in word_scores:
                    word_scores[clean_w] = {
                        "word": clean_w,
                        "score": score,
                        "sentiment": sentiment,
                        "log_p_hoaks": round(vocab_map[clean_w]["log_p_hoaks"], 3),
                        "log_p_fakta": round(vocab_map[clean_w]["log_p_fakta"], 3),
                    }

        tokens_out.append({
            "text": tok,
            "clean": clean_w,
            "is_word": is_word,
            "score": score,
            "sentiment": sentiment,
        })

    # Top influential words (up to 4 hoaks + 4 fakta, sorted by abs score)
    hoaks_words = sorted(
        [v for v in word_scores.values() if v["score"] > 0.15],
        key=lambda x: x["score"],
        reverse=True,
    )[:4]
    fakta_words = sorted(
        [v for v in word_scores.values() if v["score"] < -0.15],
        key=lambda x: x["score"],
    )[:4]

    top_influential = sorted(
        hoaks_words + fakta_words,
        key=lambda x: abs(x["score"]),
        reverse=True,
    )

    top_keywords = [w["word"] for w in sorted(
        word_scores.values(), key=lambda x: abs(x["score"]), reverse=True
    )[:3]]

    word_count = sum(1 for t in tokens_out if t["is_word"])

    return {
        "verdict": verdict,
        "hoax_probability": hoax_pct,
        "fakta_probability": fakta_pct,
        "confidence_label": confidence_label,
        "word_count": word_count,
        "top_keywords": top_keywords,
        "tokens": tokens_out,
        "top_influential_words": top_influential,
    }


def handle_analysis() -> dict:
    df, _ = load_contrib_map()
    with open(METRICS_PATH, "r", encoding="utf-8") as f:
        metrics = json.load(f)

    # Filter unigrams for cleaner display in UI
    df_uni = df[~df["kata"].astype(str).str.contains(" ", regex=False)].copy()
    df_uni = df_uni[df_uni["kata"].astype(str).str.len() > 2]

    top_hoax = []
    for _, r in df_uni.head(10).iterrows():
        top_hoax.append({
            "word": str(r["kata"]),
            "score": round(float(r["kontribusi_hoaks"]), 2),
        })

    top_fakta = []
    for _, r in df_uni.tail(10).iloc[::-1].iterrows():
        top_fakta.append({
            "word": str(r["kata"]),
            "score": round(abs(float(r["kontribusi_hoaks"])), 2),
        })

    return {
        "vocab_size": int(metrics.get("n_vocab", len(df))),
        "prior_hoax": 50.0,
        "prior_fakta": 50.0,
        "n_train": int(metrics.get("n_train", 18905)),
        "n_test": int(metrics.get("n_test", 4727)),
        "top_hoax_words": top_hoax,
        "top_fakta_words": top_fakta,
    }


def handle_wordcloud(corpus: str = "hoaks", query: str = "") -> dict:
    df, _ = load_contrib_map()
    df_uni = df[~df["kata"].astype(str).str.contains(" ", regex=False)].copy()
    df_uni = df_uni[df_uni["kata"].astype(str).str.len() > 2]

    if query:
        df_uni = df_uni[df_uni["kata"].astype(str).str.contains(query.lower(), regex=False)]

    if corpus == "fakta":
        subset = df_uni.sort_values("kontribusi_hoaks", ascending=True).head(30)
        scores = [abs(float(s)) for s in subset["kontribusi_hoaks"]]
    else:
        subset = df_uni.sort_values("kontribusi_hoaks", ascending=False).head(30)
        scores = [max(0.0, float(s)) for s in subset["kontribusi_hoaks"]]

    max_s = max(scores) if scores else 1.0
    min_s = min(scores) if scores else 0.0
    span = (max_s - min_s) if (max_s - min_s) > 1e-6 else 1.0

    words = []
    for (_, r), sc in zip(subset.iterrows(), scores):
        norm = round(0.25 + 0.75 * ((sc - min_s) / span), 3)
        words.append({
            "word": str(r["kata"]),
            "score": round(sc, 2),
            "weight": norm,
        })

    return {
        "corpus": corpus,
        "total_docs": 11816,
        "unique_words": int(len(df_uni)),
        "top_word": words[0] if words else None,
        "words": words,
    }


def handle_evaluation() -> dict:
    with open(METRICS_PATH, "r", encoding="utf-8") as f:
        metrics = json.load(f)

    cm = metrics.get("confusion_matrix", [[0, 0], [0, 0]])
    # cm layout from sklearn: [[TN(Fakta->Fakta), FP(Fakta->Hoaks)], [FN(Hoaks->Fakta), TP(Hoaks->Hoaks)]]
    tn, fp = int(cm[0][0]), int(cm[0][1])
    fn, tp = int(cm[1][0]), int(cm[1][1])
    total = max(1, tn + fp + fn + tp)

    prec_hoaks = tp / max(1, tp + fp)
    rec_hoaks = tp / max(1, tp + fn)
    f1_hoaks = (2 * prec_hoaks * rec_hoaks) / max(1e-9, prec_hoaks + rec_hoaks)

    prec_fakta = tn / max(1, tn + fn)
    rec_fakta = tn / max(1, tn + fp)
    f1_fakta = (2 * prec_fakta * rec_fakta) / max(1e-9, prec_fakta + rec_fakta)

    return {
        "accuracy": round(float(metrics["accuracy"]) * 100, 1),
        "precision": round(float(metrics["precision"]) * 100, 1),
        "recall": round(float(metrics["recall"]) * 100, 1),
        "f1_score": round(float(metrics["f1_score"]) * 100, 1),
        "cv_f1_mean": round(float(metrics["cv_f1_mean"]) * 100, 1),
        "cv_f1_std": round(float(metrics["cv_f1_std"]) * 100, 2),
        "cv_f1_per_fold": [round(float(x) * 100, 1) for x in metrics.get("cv_f1_per_fold", [])],
        "n_train": int(metrics["n_train"]),
        "n_test": int(metrics["n_test"]),
        "n_vocab": int(metrics["n_vocab"]),
        "confusion_matrix": {
            "tp": tp,
            "fp": fp,
            "fn": fn,
            "tn": tn,
            "total": total,
        },
        "per_class": {
            "hoaks": {
                "precision": round(prec_hoaks * 100, 1),
                "recall": round(rec_hoaks * 100, 1),
                "f1": round(f1_hoaks * 100, 1),
            },
            "fakta": {
                "precision": round(prec_fakta * 100, 1),
                "recall": round(rec_fakta * 100, 1),
                "f1": round(f1_fakta * 100, 1),
            },
        },
    }


def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "Missing command"}))
        sys.exit(1)

    cmd = sys.argv[1]
    if cmd == "predict":
        raw_stdin = sys.stdin.read().lstrip("\ufeff")
        payload = json.loads(raw_stdin) if raw_stdin.strip() else {}
        res = handle_predict(payload)
    elif cmd == "analysis":
        res = handle_analysis()
    elif cmd == "wordcloud":
        corpus = sys.argv[2] if len(sys.argv) > 2 else "hoaks"
        query = sys.argv[3] if len(sys.argv) > 3 else ""
        res = handle_wordcloud(corpus, query)
    elif cmd == "evaluation":
        res = handle_evaluation()
    else:
        res = {"error": f"Unknown command: {cmd}"}

    print(json.dumps(res, ensure_ascii=False))


if __name__ == "__main__":
    main()

<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\DetectHoaxRequest;
use App\Models\DetectionHistory;
use App\Services\HoaxInferenceService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class HoaxController extends Controller
{
    public function __construct(
        private readonly HoaxInferenceService $inferenceService
    ) {}

    public function health(): JsonResponse
    {
        return response()->json([
            'status' => 'ok',
            'service' => 'CekKabar API',
            'model' => 'TF-IDF + Multinomial Naive Bayes',
        ]);
    }

    public function detect(DetectHoaxRequest $request): JsonResponse
    {
        $text = trim($request->validated('text'));
        $result = $this->inferenceService->predict($text);

        $history = DetectionHistory::create([
            'text' => $text,
            'verdict' => $result['verdict'],
            'hoax_probability' => $result['hoax_probability'],
            'fakta_probability' => $result['fakta_probability'],
            'confidence_label' => $result['confidence_label'],
            'word_count' => $result['word_count'],
            'top_keywords' => $result['top_keywords'],
            'tokens' => $result['tokens'],
            'top_influential_words' => $result['top_influential_words'],
        ]);

        return response()->json([
            'id' => $history->id,
            'text' => $history->text,
            'verdict' => $history->verdict,
            'hoax_probability' => $history->hoax_probability,
            'fakta_probability' => $history->fakta_probability,
            'confidence_label' => $history->confidence_label,
            'word_count' => $history->word_count,
            'top_keywords' => $history->top_keywords,
            'tokens' => $history->tokens,
            'top_influential_words' => $history->top_influential_words,
            'created_at' => $history->created_at?->toIso8601String(),
        ], 201);
    }

    public function examples(): JsonResponse
    {
        return response()->json([
            'examples' => [
                [
                    'id' => 1,
                    'label' => 'HOAKS',
                    'category' => 'Sentimen Sensasional',
                    'text' => 'VIRAL! Sebarkan sebelum dihapus, kode token listrik gratis PLN 3 bulan tanpa daftar klik link WhatsApp berikut sekarang!',
                ],
                [
                    'id' => 2,
                    'label' => 'FAKTA',
                    'category' => 'Rilis Jurnalistik Resmi',
                    'text' => 'Jakarta (ANTARA) - Kementerian Kesehatan meresmikan 15 rumah sakit rujukan baru di Jawa Timur, ujar Menteri Kesehatan saat ditemui di Jakarta, Rabu.',
                ],
                [
                    'id' => 3,
                    'label' => 'HOAKS',
                    'category' => 'Penipuan Bantuan',
                    'text' => 'HEBOH pemerintah bagikan dana bantuan subsidi UMKM Rp 500 ribu lewat akun Facebook ini, buruan daftar dan sebarkan ke grup WA!',
                ],
                [
                    'id' => 4,
                    'label' => 'FAKTA',
                    'category' => 'Informasi Kebijakan Publik',
                    'text' => 'Kementerian Perhubungan mencatat peningkatan jumlah penumpang kereta api selama masa libur panjang, dalam keterangan resmi yang diterima di Jakarta, Kamis.',
                ],
            ],
        ]);
    }

    public function analysis(): JsonResponse
    {
        return response()->json($this->inferenceService->getAnalysis());
    }

    public function wordcloud(Request $request): JsonResponse
    {
        $corpus = (string) $request->query('corpus', 'hoaks');
        $query = trim((string) $request->query('q', ''));

        return response()->json($this->inferenceService->getWordCloud($corpus, $query));
    }

    public function evaluation(): JsonResponse
    {
        return response()->json($this->inferenceService->getEvaluation());
    }

    public function histories(Request $request): JsonResponse
    {
        $filter = strtolower((string) $request->query('filter', 'all'));
        $query = trim((string) $request->query('q', ''));

        $counts = [
            'all' => DetectionHistory::count(),
            'hoaks' => DetectionHistory::where('verdict', 'HOAKS')->count(),
            'fakta' => DetectionHistory::where('verdict', 'FAKTA')->count(),
        ];

        $builder = DetectionHistory::query()->latest();

        if ($filter === 'hoaks') {
            $builder->where('verdict', 'HOAKS');
        } elseif ($filter === 'fakta') {
            $builder->where('verdict', 'FAKTA');
        }

        if ($query !== '') {
            $builder->where('text', 'like', '%' . $query . '%');
        }

        $items = $builder->limit(50)->get()->map(function (DetectionHistory $item) {
            return [
                'id' => $item->id,
                'text' => $item->text,
                'verdict' => $item->verdict,
                'hoax_probability' => $item->hoax_probability,
                'fakta_probability' => $item->fakta_probability,
                'confidence_label' => $item->confidence_label,
                'top_keywords' => $item->top_keywords ?? [],
                'word_count' => $item->word_count,
                'created_at' => $item->created_at?->toIso8601String(),
                'formatted_time' => $item->created_at?->timezone('Asia/Jakarta')->format('d M Y, H:i') . ' WIB',
            ];
        });

        return response()->json([
            'counts' => $counts,
            'items' => $items,
        ]);
    }

    public function showHistory(int $id): JsonResponse
    {
        $history = DetectionHistory::findOrFail($id);

        return response()->json([
            'id' => $history->id,
            'text' => $history->text,
            'verdict' => $history->verdict,
            'hoax_probability' => $history->hoax_probability,
            'fakta_probability' => $history->fakta_probability,
            'confidence_label' => $history->confidence_label,
            'word_count' => $history->word_count,
            'top_keywords' => $history->top_keywords ?? [],
            'tokens' => $history->tokens ?? [],
            'top_influential_words' => $history->top_influential_words ?? [],
            'created_at' => $history->created_at?->toIso8601String(),
        ]);
    }

    public function destroyHistory(int $id): JsonResponse
    {
        $history = DetectionHistory::findOrFail($id);
        $history->delete();

        return response()->json(['message' => 'Riwayat berhasil dihapus.']);
    }

    public function clearHistories(): JsonResponse
    {
        DetectionHistory::truncate();

        return response()->json(['message' => 'Semua riwayat berhasil dihapus.']);
    }
}


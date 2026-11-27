<?php

namespace Tests\Feature;

use App\Models\DetectionHistory;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class HoaxApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_health_endpoint_returns_ok(): void
    {
        $response = $this->getJson('/api/v1/health');
        $response->assertStatus(200)
            ->assertJson([
                'status' => 'ok',
                'service' => 'CekKabar API',
            ]);
    }

    public function test_examples_endpoint_returns_list(): void
    {
        $response = $this->getJson('/api/v1/examples');
        $response->assertStatus(200)
            ->assertJsonStructure([
                'examples' => [
                    ['id', 'label', 'category', 'text'],
                ],
            ]);
    }

    public function test_detect_validation_rejects_short_text(): void
    {
        $response = $this->postJson('/api/v1/detect', ['text' => 'pendek']);
        $response->assertStatus(422)
            ->assertJsonValidationErrors(['text']);
    }

    public function test_detect_and_history_lifecycle(): void
    {
        $payload = [
            'text' => 'VIRAL! Sebarkan sebelum dihapus, pemerintah bagikan uang gratis Rp 500 ribu lewat link WhatsApp!',
        ];

        $detectRes = $this->postJson('/api/v1/detect', $payload);
        $detectRes->assertStatus(201)
            ->assertJsonStructure([
                'id',
                'text',
                'verdict',
                'hoax_probability',
                'fakta_probability',
                'confidence_label',
                'tokens',
                'top_influential_words',
            ]);

        $id = $detectRes->json('id');
        $this->assertDatabaseHas('detection_histories', ['id' => $id]);

        // List histories
        $listRes = $this->getJson('/api/v1/histories');
        $listRes->assertStatus(200)
            ->assertJsonPath('counts.all', 1);

        // Show single history
        $showRes = $this->getJson('/api/v1/histories/' . $id);
        $showRes->assertStatus(200)
            ->assertJsonPath('id', $id);

        // Delete history
        $delRes = $this->deleteJson('/api/v1/histories/' . $id);
        $delRes->assertStatus(200);
        $this->assertEquals(0, DetectionHistory::count());
    }

    public function test_analysis_wordcloud_and_evaluation_endpoints(): void
    {
        $this->getJson('/api/v1/analysis')
            ->assertStatus(200)
            ->assertJsonStructure(['vocab_size', 'top_hoax_words', 'top_fakta_words']);

        $this->getJson('/api/v1/wordcloud?corpus=hoaks')
            ->assertStatus(200)
            ->assertJsonStructure(['corpus', 'words']);

        $this->getJson('/api/v1/evaluation')
            ->assertStatus(200)
            ->assertJsonStructure(['accuracy', 'precision', 'recall', 'f1_score', 'confusion_matrix', 'per_class']);
    }
}


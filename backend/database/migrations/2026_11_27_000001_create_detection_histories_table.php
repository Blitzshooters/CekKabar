<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('detection_histories', function (Blueprint $table) {
            $table->id();
            $table->text('text');
            $table->string('verdict', 16); // HOAKS | FAKTA
            $table->float('hoax_probability');
            $table->float('fakta_probability');
            $table->string('confidence_label', 64);
            $table->integer('word_count')->default(0);
            $table->json('top_keywords')->nullable();
            $table->json('tokens')->nullable();
            $table->json('top_influential_words')->nullable();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('detection_histories');
    }
};


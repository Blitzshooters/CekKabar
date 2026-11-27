<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class DetectionHistory extends Model
{
    protected $fillable = [
        'text',
        'verdict',
        'hoax_probability',
        'fakta_probability',
        'confidence_label',
        'word_count',
        'top_keywords',
        'tokens',
        'top_influential_words',
    ];

    protected $casts = [
        'hoax_probability' => 'float',
        'fakta_probability' => 'float',
        'word_count' => 'integer',
        'top_keywords' => 'array',
        'tokens' => 'array',
        'top_influential_words' => 'array',
    ];
}


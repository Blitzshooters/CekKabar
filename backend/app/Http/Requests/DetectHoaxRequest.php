<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class DetectHoaxRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'text' => ['required', 'string', 'min:10', 'max:5000'],
        ];
    }

    public function messages(): array
    {
        return [
            'text.required' => 'Teks berita tidak boleh kosong.',
            'text.min' => 'Teks berita minimal 10 karakter agar dapat dianalisis.',
            'text.max' => 'Teks berita maksimal 5.000 karakter.',
        ];
    }
}


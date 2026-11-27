<?php

use App\Http\Controllers\Api\HoaxController;
use Illuminate\Support\Facades\Route;

Route::prefix('v1')->group(function () {
    Route::get('/health', [HoaxController::class, 'health']);
    Route::post('/detect', [HoaxController::class, 'detect']);
    Route::get('/examples', [HoaxController::class, 'examples']);
    Route::get('/analysis', [HoaxController::class, 'analysis']);
    Route::get('/wordcloud', [HoaxController::class, 'wordcloud']);
    Route::get('/evaluation', [HoaxController::class, 'evaluation']);

    Route::get('/histories', [HoaxController::class, 'histories']);
    Route::get('/histories/{id}', [HoaxController::class, 'showHistory']);
    Route::delete('/histories/{id}', [HoaxController::class, 'destroyHistory']);
    Route::delete('/histories', [HoaxController::class, 'clearHistories']);
});


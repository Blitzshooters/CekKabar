<?php

namespace App\Services;

use Illuminate\Support\Facades\Cache;
use RuntimeException;
use Symfony\Component\Process\Process;

class HoaxInferenceService
{
    private string $pythonBin;
    private string $scriptPath;

    public function __construct()
    {
        $this->pythonBin = env('PYTHON_BIN', 'python');
        $this->scriptPath = base_path('ml/infer.py');
    }

    public function predict(string $text): array
    {
        $payload = json_encode(['text' => $text], JSON_UNESCAPED_UNICODE);
        return $this->runPython(['predict'], $payload);
    }

    public function getAnalysis(): array
    {
        return Cache::remember('cekkabar_analysis_v1', 3600, function () {
            return $this->runPython(['analysis']);
        });
    }

    public function getWordCloud(string $corpus = 'hoaks', string $query = ''): array
    {
        $corpus = in_array($corpus, ['hoaks', 'fakta'], true) ? $corpus : 'hoaks';
        $cacheKey = 'cekkabar_wc_v2_' . $corpus . '_' . md5($query);

        return Cache::remember($cacheKey, 1800, function () use ($corpus, $query) {
            $args = ['wordcloud', $corpus];
            if ($query !== '') {
                $args[] = $query;
            }
            return $this->runPython($args);
        });
    }

    public function getEvaluation(): array
    {
        return Cache::remember('cekkabar_evaluation_v1', 3600, function () {
            return $this->runPython(['evaluation']);
        });
    }

    private function runPython(array $args, ?string $stdin = null): array
    {
        $command = array_merge([$this->pythonBin, $this->scriptPath], $args);

        // Pass essential Windows environment variables so Python subprocess
        // can load Winsock/asyncio correctly when called from PHP web server.
        $env = array_merge($_ENV, [
            'PYTHONUNBUFFERED' => '1',
            'PYTHONDONTWRITEBYTECODE' => '1',
            'PYTHONIOENCODING' => 'utf-8',
            'SystemRoot'   => env('SystemRoot', 'C:\\Windows'),
            'SystemDrive'  => env('SystemDrive', 'C:'),
            'TEMP'         => env('TEMP', sys_get_temp_dir()),
            'TMP'          => env('TMP', sys_get_temp_dir()),
            'PATH'         => env('PATH', ''),
            'USERPROFILE'  => env('USERPROFILE', ''),
            'APPDATA'      => env('APPDATA', ''),
            'LOCALAPPDATA' => env('LOCALAPPDATA', ''),
            'WINDIR'       => env('WINDIR', 'C:\\Windows'),
        ]);

        $process = new Process($command, base_path(), $env, $stdin, 60);
        $process->run();

        if (! $process->isSuccessful()) {
            throw new RuntimeException(
                'ML inference failed: ' . ($process->getErrorOutput() ?: $process->getOutput())
            );
        }

        $decoded = json_decode($process->getOutput(), true);
        if (! is_array($decoded)) {
            throw new RuntimeException('Invalid JSON output from ML inference.');
        }

        if (isset($decoded['error'])) {
            throw new RuntimeException($decoded['error']);
        }

        return $decoded;
    }
}


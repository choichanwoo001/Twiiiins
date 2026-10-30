param([string]$Repository)
$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$translationFile = Join-Path $projectRoot 'backend/.env.translation.local'
if (!(Test-Path -LiteralPath $translationFile)) { throw 'Create backend/.env.translation.local from its example first.' }
$githubCli = Get-Command gh -ErrorAction SilentlyContinue
if (!$githubCli) { throw 'GitHub CLI (gh) is required and must be authenticated with repository secret access.' }
$values = @{}
foreach ($line in Get-Content -LiteralPath $translationFile) {
    if ($line -match '^\s*(GEMINI_API_KEY|GEMINI_MODEL|TRANSLATION_ENABLED|TRANSLATION_TIMEOUT_SECONDS)\s*=\s*(.*?)\s*$') {
        $values[$Matches[1]] = $Matches[2].Trim('"', "'")
    }
}
if (!$values['GEMINI_API_KEY']) { throw 'GEMINI_API_KEY is empty. No secrets were changed.' }
foreach ($name in @('GEMINI_MODEL', 'TRANSLATION_ENABLED', 'TRANSLATION_TIMEOUT_SECONDS')) {
    if (!$values[$name]) { throw "$name is required. No secrets were changed." }
}
Push-Location $projectRoot
try {
    foreach ($name in @('GEMINI_API_KEY', 'GEMINI_MODEL', 'TRANSLATION_ENABLED', 'TRANSLATION_TIMEOUT_SECONDS')) {
        $arguments = @('secret', 'set', $name)
        if ($Repository) { $arguments += @('--repo', $Repository) }
        # Secret values go over stdin, never command arguments, stdout or a tracked file.
        $processInfo = [System.Diagnostics.ProcessStartInfo]::new()
        $processInfo.FileName = $githubCli.Source
        $processInfo.UseShellExecute = $false
        $processInfo.CreateNoWindow = $true
        $processInfo.RedirectStandardInput = $true
        foreach ($argument in $arguments) { $processInfo.ArgumentList.Add($argument) }
        $process = [System.Diagnostics.Process]::Start($processInfo)
        try {
            $process.StandardInput.Write($values[$name])
            $process.StandardInput.Close()
            $process.WaitForExit()
            if ($process.ExitCode -ne 0) { throw "Failed to register $name. Check GitHub CLI authentication and permissions." }
        } finally { $process.Dispose() }
        Write-Output "Registered $name"
    }
} finally { Pop-Location; $values.Clear() }

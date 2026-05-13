param(
    [string]$TrackerPath = "C:\Users\Admin\AndroidStudioProjects\ModProjectTracker\docs\phase-tracker.json",
    [switch]$DryRun,
    [switch]$FailOnMissingMarker = $true
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Convert-Lines {
    param([string[]]$Lines)
    return ($Lines -join [Environment]::NewLine)
}

function Build-PhaseTable {
    param($Tracker)
    $lines = @()
    $lines += "| Phase | Mo ta | Trang thai |"
    $lines += "|---|---|---|"
    foreach ($phase in $Tracker.phases) {
        $lines += "| $($phase.id) | $($phase.name) | $($phase.status) |"
    }
    return Convert-Lines $lines
}

function Build-BulletList {
    param(
        [string]$Title,
        $Items
    )
    $lines = @($Title)
    foreach ($item in $Items) {
        $lines += "- $item"
    }
    return Convert-Lines $lines
}

function Build-NumberList {
    param(
        [string]$Title,
        $Items
    )
    $lines = @($Title)
    $index = 1
    foreach ($item in $Items) {
        $lines += "$index. $item"
        $index++
    }
    return Convert-Lines $lines
}

function Build-Gates {
    param($Tracker)
    $lines = @()
    foreach ($gate in $Tracker.phaseGates) {
        $lines += "### $($gate.phase)"
        foreach ($condition in $gate.conditions) {
            $mark = if ($condition.done) { "x" } else { " " }
            $lines += "- [$mark] $($condition.text)"
        }
        $lines += ""
    }
    if ($lines.Count -gt 0 -and $lines[-1] -eq "") {
        $lines = $lines[0..($lines.Count - 2)]
    }
    return Convert-Lines $lines
}

function Assert-GatePolicy {
    param($Tracker)

    foreach ($gate in $Tracker.phaseGates) {
        if ($gate.phase -notmatch 'Phase\s+(\d+)') {
            continue
        }

        $phaseId = $Matches[1]
        $phase = $Tracker.phases | Where-Object { $_.id -eq $phaseId } | Select-Object -First 1
        if (-not $phase) {
            continue
        }

        $openConditions = @($gate.conditions | Where-Object { -not $_.done })
        $phaseStatus = if ($null -ne $phase.status) { [string]$phase.status } else { '' }
        $normalizedStatus = $phaseStatus.Trim().ToLowerInvariant()
        if ($openConditions.Count -gt 0 -and ($normalizedStatus -eq 'done' -or $normalizedStatus -eq 'completed' -or $normalizedStatus -eq 'hoan thanh')) {
            $openText = ($openConditions | ForEach-Object { $_.text }) -join '; '
            throw "Phase $phaseId is marked '$($phase.status)' but gate '$($gate.phase)' still has open items: $openText"
        }
    }
}

function Replace-MarkerBlock {
    param(
        [string]$FilePath,
        [string]$StartMarker,
        [string]$EndMarker,
        [string]$Body,
        [switch]$DryRun,
        [switch]$FailOnMissingMarker
    )

    $content = Get-Content -Raw -Path $FilePath -Encoding UTF8
    $escapedStart = [regex]::Escape($StartMarker)
    $escapedEnd = [regex]::Escape($EndMarker)
    $pattern = "(?s)($escapedStart\r?\n)(.*?)(\r?\n$escapedEnd)"

    if (-not [regex]::IsMatch($content, $pattern)) {
        if ($FailOnMissingMarker) {
            throw "Missing marker block in ${FilePath}: $StartMarker ... $EndMarker"
        }
        return $false
    }

    $normalizedBody = $Body.TrimEnd()
    $replacement = '$1' + $normalizedBody + '$3'
    $updated = [regex]::Replace($content, $pattern, $replacement)

    if ($updated -ne $content) {
        if (-not $DryRun) {
            Set-Content -Path $FilePath -Value $updated -Encoding UTF8
        }
        return $true
    }

    return $false
}

$tracker = Get-Content -Raw -Path $TrackerPath -Encoding UTF8 | ConvertFrom-Json
$repoRoot = Split-Path -Parent (Split-Path -Parent $TrackerPath)

Assert-GatePolicy -Tracker $tracker

$phaseTable = Build-PhaseTable -Tracker $tracker
$progressBullets = Build-BulletList -Title "### Current progress" -Items $tracker.currentProgress
$completedBullets = Build-BulletList -Title "### Completed" -Items $tracker.completedFeatures
$inProgressBullets = Build-BulletList -Title "### In progress" -Items $tracker.inProgressFeatures
$openGapsBullets = Build-BulletList -Title "### Open gaps" -Items $tracker.openGaps
$nextMilestones = Build-NumberList -Title "### Next milestones" -Items $tracker.nextMilestones
$risks = Build-BulletList -Title "### Risk / assumption" -Items $tracker.riskAssumptions
$gates = Build-Gates -Tracker $tracker

$rootBody = @(
    "_Auto generated from `docs/phase-tracker.json` on $($tracker.lastUpdated)._",
    "",
    "**Overall status:** $($tracker.overallStatus)",
    "",
    $phaseTable,
    "",
    $progressBullets,
    "",
    $nextMilestones
) -join [Environment]::NewLine

$appBody = @(
    "_Auto generated from `docs/phase-tracker.json` on $($tracker.lastUpdated)._",
    "",
    "**Module status:** $($tracker.overallStatus)",
    "",
    $completedBullets,
    "",
    $inProgressBullets,
    "",
    $openGapsBullets
) -join [Environment]::NewLine

$gateBody = @(
    "_Auto generated from `docs/phase-tracker.json` on $($tracker.lastUpdated)._",
    "",
    "## Phase matrix (synced)",
    "",
    $phaseTable,
    "",
    "## Gate checklist",
    "",
    $gates
) -join [Environment]::NewLine

$sectionsBody = @(
    "_Auto generated from `docs/phase-tracker.json` on $($tracker.lastUpdated)._",
    "",
    "## Executive Summary (synced)",
    "- Overall status: $($tracker.overallStatus)",
    "",
    $progressBullets,
    "",
    $openGapsBullets,
    "",
    $nextMilestones,
    "",
    $risks
) -join [Environment]::NewLine

$changes = 0
$changes += [int](Replace-MarkerBlock -FilePath (Join-Path $repoRoot "README.md") -StartMarker "<!-- AUTO_PHASE_PROGRESS:START -->" -EndMarker "<!-- AUTO_PHASE_PROGRESS:END -->" -Body $rootBody -DryRun:$DryRun -FailOnMissingMarker:$FailOnMissingMarker)
$changes += [int](Replace-MarkerBlock -FilePath (Join-Path $repoRoot "app\README.md") -StartMarker "<!-- AUTO_PHASE_APP_PROGRESS:START -->" -EndMarker "<!-- AUTO_PHASE_APP_PROGRESS:END -->" -Body $appBody -DryRun:$DryRun -FailOnMissingMarker:$FailOnMissingMarker)
$changes += [int](Replace-MarkerBlock -FilePath (Join-Path $repoRoot "docs\README_PHASE_STATUS.md") -StartMarker "<!-- AUTO_PHASE_GATE:START -->" -EndMarker "<!-- AUTO_PHASE_GATE:END -->" -Body $gateBody -DryRun:$DryRun -FailOnMissingMarker:$FailOnMissingMarker)
$changes += [int](Replace-MarkerBlock -FilePath (Join-Path $repoRoot "docs\README_PROGRESS_SECTIONS.md") -StartMarker "<!-- AUTO_PROGRESS_SECTIONS:START -->" -EndMarker "<!-- AUTO_PROGRESS_SECTIONS:END -->" -Body $sectionsBody -DryRun:$DryRun -FailOnMissingMarker:$FailOnMissingMarker)

$mode = if ($DryRun) { "DRY-RUN" } else { "WRITE" }
Write-Host "[$mode] Updated marker blocks: $changes"



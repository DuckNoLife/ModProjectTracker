# Migration Summary: AI Assistant Prompt and Transport Updates

## Date: April 21, 2026

## Overview

The AI assistant flow has been updated to preload role instructions and live project/expense context before each user question.

## Scope of updates

### `app/src/main/java/com/example/modprojecttracker/activity/AiAssistantActivity.java`

- Builds `finalPromptToSend` with:
  - `R.string.ai_preload_role_prompt`
  - context summary from local database (`projects`, lifecycle totals, and recent expenses)
  - `USER_QUESTION`
- Adds debug payload logging under `BuildConfig.DEBUG`.

### `app/src/main/java/com/example/modprojecttracker/ai/GeminiService.java`

- Uses direct REST calls via `OkHttp` to `generativelanguage.googleapis.com`.
- Keeps retry/backoff and cooldown handling for rate limits.
- Uses `gemini-2.5-flash` as default model (configurable via `GEMINI_MODEL`).

### `app/src/main/res/values/strings.xml`

- Stores the preload role prompt as `ai_preload_role_prompt` (non-translatable).

## Current AI behavior

- The assistant does not send raw user text alone.
- It sends a composed payload that includes policy/rules + current app data snapshot + user request.
- Current data scope is admin-visible data, not strict per-account isolation.

## Verification checklist

- `:app:processDebugResources` succeeds after moving prompt text to `strings.xml`.
- `:app:compileDebugJavaWithJavac` succeeds after AI flow changes.
- Debug logs show the final composed payload when `BuildConfig.DEBUG` is enabled.


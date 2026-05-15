# AI Context Builder

AI Context Builder is a small IntelliJ Platform plugin that helps developers prepare structured IDE-aware context for AI coding agents.

## Motivation

Coding agents often fail not because the model is weak, but because the context is incomplete, noisy, or disconnected from the actual IDE state.

This plugin collects selected code and file metadata directly from the IDE and turns it into a clean, reusable Markdown prompt.

## Features

- Add selected code from the editor popup
- Automatically update the AI Context Tool Window
- Inspect saved context and selected code previews
- Preserve file path, language, and selected line range
- Add a user task for the AI agent
- Copy a full Markdown AI prompt
- Copy raw IDE context without instructions
- Remove the last item or clear all context
- Display rough context size and token estimate
- Works offline without API keys

## Usage

1. Select code in the editor.
2. Right-click and choose **Add Selection to AI Context**.
3. Open the **AI Context** Tool Window.
4. Review the collected code snippets.
5. Enter the task for the AI agent.
6. Click **Copy Prompt**.
7. Paste the prompt into your preferred AI coding assistant.

## Architecture

Editor Action -> Project Service -> Prompt Builder -> Tool Window -> Clipboard

## Current Limitations

- Context is stored only in memory.
- No LLM API integration.
- No PSI-based symbol extraction.
- No Git diff support.
- No per-item remove button yet.
- No automatic source code modification.

## Future Ideas

- Add clipboard stacktrace as context
- Include surrounding function/class using PSI
- Add Git diff support
- Add optional AI summary via a pluggable AiClient abstraction

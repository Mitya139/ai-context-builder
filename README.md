# AI Context Builder

AI Context Builder is a small IntelliJ Platform plugin that helps developers prepare structured IDE-aware context for AI coding agents.

## Motivation

Coding agents often fail not because the model is weak, but because the context is incomplete, noisy, or disconnected from the actual IDE state.

This plugin explores a small but practical part of that problem: collecting selected code and file metadata directly from the IDE and turning it into a clean, reusable Markdown prompt.

## Features

- Add selected code from the editor to AI context
- Store file path, project name, file type, and selected code
- View collected context in a dedicated Tool Window
- Generate and copy a Markdown prompt for external AI coding agents
- Works offline without API keys

## Architecture

Editor Action -> Project Service -> Prompt Builder -> Tool Window -> Clipboard

## Current Limitations

- Context is stored only in memory
- No LLM API integration yet
- No PSI-based symbol extraction yet
- No Git diff support yet
- No automatic source code modification

## Future Ideas

- Add clipboard stacktrace as context
- Add a user task input field
- Include surrounding function/class using PSI
- Add Git diff support
- Add optional AI summary via a pluggable AiClient abstraction

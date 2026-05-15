# AI Context Builder

AI Context Builder is a small IntelliJ Platform plugin that helps developers prepare focused, high-quality IDE context for AI coding agents.

Instead of attaching entire files or guessing what the AI needs, the plugin lets you collect exact code snippets, inspect them, write a task, and run an AI-powered context readiness check.

The readiness check uses the selected snippets plus a lightweight project outline to estimate whether the current context is ready, partial, or insufficient for the task.

## Motivation

AI coding agents often fail because the prompt has the wrong context: too little code, too much noise, or missing adjacent files. This plugin focuses on context quality before the prompt leaves the IDE.

## Features

- Add selected code from the editor popup
- Inspect collected snippets as collapsible context items
- Copy or remove individual context items
- Preserve file path, language, and selected line range
- Enter a user task for the AI agent
- Copy a full Markdown AI prompt
- Copy raw IDE context without instructions
- Display rough context size and token estimates
- Run an AI Context Readiness Check
- Fall back to a mock readiness report when no API key is configured

## How It Works

1. Select code in the editor.
2. Right-click and choose **Add Selection to AI Context**.
3. Open the **AI Context** Tool Window.
4. Review the collected snippets.
5. Enter the task for the AI agent.
6. Click **Check Context Readiness** to estimate whether the context is enough.
7. Click **Copy Prompt** and paste it into your preferred AI coding assistant.

## AI Context Readiness Check

The readiness check sends only:

- the user task;
- the selected context snippets;
- a project outline containing file paths only.

It does not send full project contents. Missing context suggestions are candidates based on selected snippets, project file paths, and the user task.

## Configuration

The current MVP reads AI configuration from environment variables:

```text
OPENAI_API_KEY   required for real AI readiness checks
OPENAI_BASE_URL  optional, default https://api.openai.com
OPENAI_MODEL     optional, default gpt-4.1-mini
```

If `OPENAI_API_KEY` is not set, the plugin uses a mock AI client so the UI can still be tested.

## Architecture

Editor Action -> Project Service -> Prompt Builder -> Tool Window -> Clipboard

Readiness Check -> Project Outline Provider -> Readiness Prompt Builder -> AI Client -> Result Panel

## Current Limitations

- Context is stored only in memory.
- The plugin does not send full project contents to the LLM.
- Missing context suggestions are candidate suggestions based on selected snippets, project file paths, and the user task.
- The plugin does not modify source code.
- The plugin is not a full AI chat assistant.
- The current MVP reads API configuration from environment variables rather than a settings UI.
- No PSI-based symbol extraction yet.
- No Git diff support yet.

## Future Ideas

- Add suggested files to context directly from the readiness report
- Add IntelliJ PasswordSafe-based settings
- Add PSI-based symbol names for selected snippets
- Add Git diff review for AI-generated changes
- Add test failure / stacktrace triage

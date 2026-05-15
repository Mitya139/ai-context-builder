# AI Context Builder

AI Context Builder is a small IntelliJ Platform plugin for preparing focused, high-quality IDE context for AI coding agents.

Instead of attaching entire files or guessing what the AI needs, the plugin lets you collect exact code snippets, merge overlapping selections, inspect the final context, write a task, and run an AI-powered context readiness check.

## Motivation

AI coding agents often fail because the prompt has the wrong context: too little code, too much noise, or missing adjacent files. AI Context Builder focuses on context quality before the prompt leaves the IDE.

## Features

- Add focused code snippets from the editor popup
- Add the current file from the editor popup without manually selecting it
- Automatically merge overlapping, adjacent, or duplicate selections from the same file
- Inspect collected context in a compact master-detail UI
- Copy or remove the selected context item
- Preserve relative file path, language, and selected line range
- Write a user task directly in the Tool Window
- Copy a full Markdown prompt for an external AI coding agent
- Copy raw IDE context without extra instructions
- Run an AI-powered Context Readiness Check
- Configure OpenAI-compatible access in IDE Settings
- Fall back to a mock AI client when API settings are missing

## How It Works

1. Select code in the editor, or place the caret in a file you want to add completely.
2. Right-click and choose **Add Selection to AI Context** or **Add Current File to AI Context**.
3. Open the **AI Context** Tool Window.
4. Review the compact context list and selected item details.
5. Enter the task for the AI agent.
6. Click **Check Context Readiness** to estimate whether the context is enough.
7. Click **Copy Prompt** and paste it into your preferred AI coding assistant.

## AI Context Readiness Check

The readiness check sends only:

- the user task;
- the selected context snippets;
- a project outline containing file paths only.

It does not send full project contents. Selected snippets and project outline entries use project-relative paths where possible, avoiding local absolute paths in prompts. Missing context suggestions are candidates based on selected snippets, project file paths, and the user task.

## Configuration

Open:

```text
Settings / Preferences -> Tools -> AI Context Builder
```

Available fields:

- **Provider**: OpenAI-compatible
- **Base URL**: defaults to `https://api.openai.com`
- **Model**: defaults to `gpt-4.1-mini`
- **API Key**: stored through IntelliJ PasswordSafe, not in the normal persistent settings file
- **Use mock AI client only**: useful for demos and offline testing

If no API key is configured in Settings, the plugin falls back to the mock AI client. Environment variables remain as a compatibility fallback:

```text
OPENAI_API_KEY
OPENAI_BASE_URL
OPENAI_MODEL
```

Settings values take precedence over environment variables.

## Architecture

Editor Action -> Context Storage Service -> Merge Decider -> Prompt Builder -> Tool Window -> Clipboard

Readiness Check Runner -> Project Outline Provider -> Readiness Prompt Builder -> AI Client -> Result Panel

Project Outline Provider uses the IntelliJ project model and VFS through `ProjectRootManager.fileIndex.iterateContent`, so excluded roots and project content are respected by the IDE. A small filesystem collector is kept as a fallback and as a testable component.

Settings -> Persistent State Service -> PasswordSafe API Key Store -> AI Client Factory

AI Client -> OpenAI-compatible Chat Completions request/response DTOs -> Gson serialization

## Current Limitations

- Context is stored only in memory.
- The plugin does not send full project contents to the AI.
- Missing context suggestions are candidate suggestions based on selected snippets, project file paths, and the user task.
- The plugin does not edit source code.
- The plugin is not a full AI chat assistant.
- No PSI-based symbol extraction yet.
- No Git diff support yet.

## Development

Run tests:

```text
./gradlew test
```

Run the standard Gradle check:

```text
./gradlew check
```

Run IntelliJ plugin verification before sharing a release build:

```text
./gradlew verifyPlugin
```

## Future Ideas

- Add suggested files directly from readiness results
- Add PSI-derived symbol names to context items
- Add AI review for generated diffs
- Add test failure / stacktrace triage

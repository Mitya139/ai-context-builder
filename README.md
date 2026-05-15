# AI Context Builder

AI Context Builder is a small IntelliJ Platform plugin for collecting focused IDE context for external AI coding agents.

The plugin lets a developer add selected code or the current file from the editor, inspect the collected snippets in a Tool Window, write a task, copy a structured Markdown prompt, and optionally run an AI-based context readiness check.

## Main Capabilities

- Add selected editor code to AI context.
- Add the current file without manually selecting it.
- Merge duplicate, adjacent, and overlapping selections from the same file.
- Keep project-relative file paths, language names, and line ranges.
- Inspect, copy, remove, or clear collected context items.
- Build a Markdown prompt for an external coding agent.
- Copy raw IDE context without extra instructions.
- Run a context readiness check through an OpenAI-compatible API.
- Use a mock AI client when no API key is configured.

## How To Use

1. Select code in the editor, or open a file you want to add completely.
2. Right-click in the editor.
3. Choose **Add Selection to AI Context** or **Add Current File to AI Context**.
4. Open the **AI Context** Tool Window.
5. Review the collected snippets and enter the task.
6. Use **Copy Prompt**, **Copy Raw Context**, or **Check Context Readiness**.

## AI Configuration

Open:

```text
Settings / Preferences -> Tools -> AI Context Builder
```

Available settings:

- **Provider**: OpenAI-compatible.
- **Base URL**: defaults to `https://api.openai.com`.
- **Model**: defaults to `gpt-4.1-mini`.
- **API Key**: stored through IntelliJ PasswordSafe.
- **Use mock AI client only**: forces offline mock responses.

If no API key is configured in Settings, the plugin falls back to a mock readiness response. Environment variables are supported as a fallback:

```text
OPENAI_API_KEY
OPENAI_BASE_URL
OPENAI_MODEL
```

Settings values take precedence over environment variables.

## Data Sent To AI

The readiness check sends only:

- the user task;
- selected context snippets;
- a project outline containing file paths only.

It does not send full project contents. Project paths are formatted relative to the project root when possible.

Unit tests do not call any real AI API and do not require an API key.

## Architecture

The implementation is split into small layers:

- `actions`: editor popup actions and conversion from editor selection to context items.
- `context`: in-memory context storage and merge rules.
- `project`: project outline collection and path filtering.
- `prompt`: Markdown prompt generation.
- `readiness`: readiness prompt construction and AI check orchestration.
- `ai`: OpenAI-compatible client, mock client, and testable HTTP transport abstraction.
- `settings`: persistent settings and PasswordSafe API key storage.
- `ui`: Swing Tool Window UI.

High-level flow:

```text
Editor Action -> Context Storage -> Prompt Builder -> Tool Window -> Clipboard

Readiness Check -> Project Outline -> Readiness Prompt -> AI Client -> Result Panel
```

## Limitations

- Context is stored in memory only.
- The plugin does not edit source files.
- Project outline suggestions are based on file paths, not full file contents.

## Development

Run tests:

```text
./gradlew test
```

Run the standard Gradle check:

```text
./gradlew check
```

Run IntelliJ plugin verification:

```text
./gradlew verifyPlugin
```

GitHub Actions runs `./gradlew test` on pushes and pull requests.

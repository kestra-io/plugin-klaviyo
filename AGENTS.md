# Kestra Klaviyo Plugin

## What

description = 'Plugin Klaviyo for Kestra Exposes 7 plugin components (tasks, triggers, and/or conditions).

## Why

Enables Kestra workflows to interact with Klaviyo, allowing orchestration of Klaviyo-based operations as part of data pipelines and automation workflows.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `klaviyo`

Infrastructure dependencies (Docker Compose services):

- `app`

### Key Plugin Classes

- `io.kestra.plugin.klaviyo.campaign.Get`
- `io.kestra.plugin.klaviyo.campaign.GetRecipientCount`
- `io.kestra.plugin.klaviyo.campaign.messages.Get`
- `io.kestra.plugin.klaviyo.campaign.messages.GetCampaign`
- `io.kestra.plugin.klaviyo.campaign.messages.GetImages`
- `io.kestra.plugin.klaviyo.jobs.GetRecipient`
- `io.kestra.plugin.klaviyo.jobs.GetSendJob`

### Project Structure

```
plugin-klaviyo/
├── src/main/java/io/kestra/plugin/klaviyo/jobs/
├── src/test/java/io/kestra/plugin/klaviyo/jobs/
├── build.gradle
└── README.md
```

### Important Commands

```bash
# Build the plugin
./gradlew shadowJar

# Run tests
./gradlew test

# Build without tests
./gradlew shadowJar -x test
```

### Configuration

All tasks and triggers accept standard Kestra plugin properties. Credentials should use
`{{ secret('SECRET_NAME') }}` — never hardcode real values.

## Agents

**IMPORTANT:** This is a Kestra plugin repository (prefixed by `plugin-`, `storage-`, or `secret-`). You **MUST** delegate all coding tasks to the `kestra-plugin-developer` agent. Do NOT implement code changes directly — always use this agent.

# Kestra Klaviyo Plugin

## What

- Provides plugin components under `io.kestra.plugin.klaviyo`.
- Includes classes such as `GetRecipientCount`, `Get`, `GetSendJob`, `GetRecipient`.

## Why

- This plugin integrates Kestra with Klaviyo Campaign.
- It provides tasks that fetch Klaviyo campaign details and recipient counts.

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

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines

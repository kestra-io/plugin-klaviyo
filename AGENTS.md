# Kestra Klaviyo Plugin

## What

- Provides plugin components under `io.kestra.plugin.klaviyo`.
- Includes classes such as `GetRecipientCount`, `Get`, `GetSendJob`, `GetRecipient`.

## Why

- What user problem does this solve? Teams need to interact with Klaviyo campaign APIs from orchestrated workflows instead of relying on manual console work, ad hoc scripts, or disconnected schedulers.
- Why would a team adopt this plugin in a workflow? It keeps Klaviyo steps in the same Kestra flow as upstream preparation, approvals, retries, notifications, and downstream systems.
- What operational/business outcome does it enable? It reduces manual handoffs and fragmented tooling while improving reliability, traceability, and delivery speed for processes that depend on Klaviyo.

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

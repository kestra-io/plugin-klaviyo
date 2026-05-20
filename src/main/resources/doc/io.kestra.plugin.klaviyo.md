# How to use the Klaviyo plugin

Retrieve Klaviyo campaign and job data from Kestra flows.

## Authentication

Set `apiKey` (required) to your Klaviyo private API key. Optionally override `baseUrl` (default `https://a.klaviyo.com/api`). Store secrets in [secrets](https://kestra.io/docs/concepts/secret) and apply connection properties globally with [plugin defaults](https://kestra.io/docs/workflow-components/plugin-defaults).

## Common properties

All tasks inherit `fetchType` (default `FETCH`) to control output shape: `FETCH` returns all rows in `rows`, `FETCH_ONE` returns a single row in `row`, `STORE` writes to internal storage and returns a `uri`, and `NONE` returns nothing.

## Tasks

### Campaigns

`campaign.Get` fetches details for one or more campaigns — set `campaignIds` (required, list of campaign IDs).

`campaign.GetRecipientCount` fetches the recipient count for one or more campaigns — set `campaignIds` (required).

### Campaign messages

`campaign.messages.Get` fetches details for one or more campaign messages — set `messageIds` (required, list of message IDs).

`campaign.messages.GetCampaign` fetches the parent campaign for one or more messages — set `messageIds` (required).

`campaign.messages.GetImages` fetches the images associated with one or more campaign messages — set `messageIds` (required).

### Jobs

`jobs.GetSendJob` fetches the send job status for one or more jobs — set `jobIds` (required, list of job IDs).

`jobs.GetRecipient` fetches recipient data for one or more jobs — set `jobIds` (required).

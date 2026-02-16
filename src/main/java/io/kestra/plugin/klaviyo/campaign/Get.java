package io.kestra.plugin.klaviyo.campaign;

import com.fasterxml.jackson.databind.JsonNode;
import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.common.FetchType;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.klaviyo.AbstractKlaviyoTask;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuperBuilder
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
@Schema(
    title = "Fetch campaigns by ID",
    description = "Retrieves Klaviyo campaigns by ID, using fetchType (default FETCH) to control whether one row, all rows, or a stored file is produced; waits ~1s between API calls."
)
@Plugin(
    examples = {
        @Example(
            title = "Get a single campaign",
            full = true,
            code = """
                id: klaviyo_get_campaign
                namespace: company.team

                tasks:
                  - id: get_campaign
                    type: io.kestra.plugin.klaviyo.campaign.Get
                    apiKey: "{{ secret('KLAVIYO_API_KEY') }}"
                    campaignIds:
                      - "campaign_id_1"
                    fetchType: FETCH_ONE
                """
        )
    }
)
public class Get extends AbstractKlaviyoTask implements RunnableTask<AbstractKlaviyoTask.Output> {

    @Schema(title = "Campaign IDs", description = "Klaviyo campaign IDs to fetch; order is preserved in the output.")
    @NotNull
    protected Property<List<String>> campaignIds;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();

        String rApiKey = runContext.render(this.apiKey).as(String.class).orElseThrow();
        String rBaseUrl = runContext.render(this.baseUrl).as(String.class).orElseThrow();
        List<String> rCampaignIds = runContext.render(this.campaignIds).asList(String.class);
        FetchType rFetchType = runContext.render(this.fetchType).as(FetchType.class).orElse(FetchType.FETCH);

        long size = 0L;
        List<Map<String, Object>> allCampaigns = new ArrayList<>();

        try (HttpClient httpClient = HttpClient.builder()
            .runContext(runContext)
            .build()) {

            for (String campaignId : rCampaignIds) {
                induceDelay();

                String url = rBaseUrl + "/campaigns/" + campaignId;

                HttpRequest request = HttpRequest.builder()
                    .uri(URI.create(url))
                    .method("GET")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/vnd.api+json")
                    .addHeader("Authorization", "Klaviyo-API-Key " + rApiKey)
                    .addHeader("revision", getApiVersion())
                    .build();

                HttpResponse<String> response = httpClient.request(request, String.class);

                if (response.getStatus().getCode() != 200) {
                    throw new RuntimeException(
                        "Failed to retrieve campaign " + campaignId + ": " +
                            response.getStatus().getCode() + " - " + response.getBody());
                }

                JsonNode responseJson = JacksonMapper.ofJson().readTree(response.getBody());
                JsonNode dataNode = responseJson.get("data");

                if (dataNode != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> campaign = JacksonMapper.ofJson().convertValue(dataNode, Map.class);
                    allCampaigns.add(campaign);
                    size++;
                }
            }

            Output output = applyFetchStrategy(rFetchType, allCampaigns, runContext);
            logger.info("Successfully retrieved {} campaign(s)", size);

            return Output.builder()
                .size(size)
                .row(output.getRow())
                .rows(output.getRows())
                .uri(output.getUri())
                .build();
        }
    }
}

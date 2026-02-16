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
    title = "Estimate recipients for campaigns",
    description = "Retrieves Klaviyo recipient estimations for the provided campaign IDs; fetchType (default FETCH) controls row vs. file output and the task waits ~1s between API calls."
)
@Plugin(
    examples = {
        @Example(
            title = "Get recipient estimation for a single campaign",
            full = true,
            code = """
                id: klaviyo_get_recipient_estimation
                namespace: company.team

                tasks:
                  - id: get_estimation
                    type: io.kestra.plugin.klaviyo.campaign.GetRecipientCount
                    apiKey: "{{ secret('KLAVIYO_API_KEY') }}"
                    campaignIds:
                      - "campaign_id_1"
                    fetchType: FETCH_ONE
                """
        ),
        @Example(
            title = "Get recipient estimations for multiple campaigns",
            code = """
                - id: get_estimations
                  type: io.kestra.plugin.klaviyo.campaign.GetRecipientCount
                  apiKey: "{{ secret('KLAVIYO_API_KEY') }}"
                  campaignIds:
                    - "campaign_id_1"
                    - "campaign_id_2"
                  fetchType: FETCH
                """
        )
    }
)
public class GetRecipientCount extends AbstractKlaviyoTask implements RunnableTask<AbstractKlaviyoTask.Output> {

    @Schema(title = "Campaign IDs", description = "Klaviyo campaign IDs to estimate recipients for; preserves input order.")
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
        List<Map<String, Object>> allEstimations = new ArrayList<>();

        try (HttpClient httpClient = HttpClient.builder()
            .runContext(runContext)
            .build()) {

            for (String campaignId : rCampaignIds) {
                induceDelay();

                String url = rBaseUrl + "/campaign-recipient-estimations/" + campaignId;

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
                        "Failed to retrieve recipient estimation for campaign " + campaignId + ": " +
                            response.getStatus().getCode() + " - " + response.getBody());
                }

                JsonNode responseJson = JacksonMapper.ofJson().readTree(response.getBody());
                JsonNode dataNode = responseJson.get("data");

                if (dataNode != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> estimation = JacksonMapper.ofJson().convertValue(dataNode, Map.class);
                    allEstimations.add(estimation);
                    size++;
                }
            }

            Output output = applyFetchStrategy(rFetchType, allEstimations, runContext);
            logger.info("Successfully retrieved {} recipient estimation(s)", size);

            return Output.builder()
                .size(size)
                .row(output.getRow())
                .rows(output.getRows())
                .uri(output.getUri())
                .build();
        }
    }
}

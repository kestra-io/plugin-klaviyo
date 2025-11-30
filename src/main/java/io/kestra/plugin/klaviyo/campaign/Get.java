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
import io.kestra.core.serializers.FileSerde;
import io.kestra.core.serializers.JacksonMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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
    title = "Retrieve campaigns from Klaviyo",
    description = "Returns the campaign details for the given campaign IDs"
)
@Plugin(examples = {
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
})
public class Get extends AbstractCampaignTask implements RunnableTask<AbstractCampaignTask.Output> {

    @Schema(title = "List of campaign IDs", description = "Campaign IDs to retrieve")
    @NotNull
    protected Property<List<String>> campaignIds;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();

        String rApiKey = runContext.render(this.apiKey).as(String.class).orElseThrow();
        String rBaseUrl = runContext.render(this.baseUrl).as(String.class).orElseThrow();
        List<String> rCampaignIds = runContext.render(this.campaignIds).asList(String.class);
        FetchType rFetchType = runContext.render(this.fetchType).as(FetchType.class).orElse(FetchType.FETCH);

        Output.OutputBuilder output = Output.builder();
        long size = 0L;
        List<Map<String, Object>> allCampaigns = new ArrayList<>();

        try (HttpClient httpClient = HttpClient.builder()
            .runContext(runContext)
            .build()) {

            for (String campaignId : rCampaignIds) {
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

            switch (rFetchType) {
                case FETCH_ONE -> {
                    Map<String, Object> result = allCampaigns.isEmpty() ? null : allCampaigns.getFirst();
                    output.row(result);
                }
                case STORE -> {
                    File tempFile = runContext.workingDir().createTempFile(".ion").toFile();
                    try (OutputStream fileOutputStream = new BufferedOutputStream(
                        new FileOutputStream(tempFile), FileSerde.BUFFER_SIZE)) {
                        for (Map<String, Object> campaign : allCampaigns) {
                            FileSerde.write(fileOutputStream, campaign);
                        }
                    }
                    output.uri(runContext.storage().putFile(tempFile));
                }
                case FETCH -> output.rows(allCampaigns);
                case NONE -> {
                }
            }

            output.size(size);
            logger.info("Successfully retrieved {} campaign(s)", size);

            return output.build();
        }
    }
}

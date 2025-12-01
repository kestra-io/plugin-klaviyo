package io.kestra.plugin.klaviyo.campaign.messages;

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
    title = "Retrieve images for campaign messages with channel mobile_push",
    description = "Returns the related images for the given campaign message IDs whose channel is mobile_push"
)
@Plugin(examples = {
    @Example(
        title = "Get image for a single message",
        full = true,
        code = """
            id: klaviyo_get_image_for_message
            namespace: company.team

            tasks:
              - id: get_image
                type: io.kestra.plugin.klaviyo.campaign.messages.GetImages
                apiKey: "{{ secret('KLAVIYO_API_KEY') }}"
                messageIds:
                  - "message_id_1"
                fetchType: FETCH_ONE
            """
    ),
    @Example(
        title = "Get images for multiple messages",
        code = """
            - id: get_images
              type: io.kestra.plugin.klaviyo.campaign.messages.GetImages
              apiKey: "{{ secret('KLAVIYO_API_KEY') }}"
              messageIds:
                - "message_id_1"
                - "message_id_2"
              fetchType: FETCH
            """
    )
})
public class GetImages extends AbstractKlaviyoTask implements RunnableTask<AbstractKlaviyoTask.Output> {

    @Schema(title = "List of message IDs with mobile_push channel", description = "Campaign message IDs for which to retrieve related images")
    @NotNull
    protected Property<List<String>> messageIds;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();

        String rApiKey = runContext.render(this.apiKey).as(String.class).orElseThrow();
        String rBaseUrl = runContext.render(this.baseUrl).as(String.class).orElseThrow();
        List<String> rMessageIds = runContext.render(this.messageIds).asList(String.class);
        FetchType rFetchType = runContext.render(this.fetchType).as(FetchType.class).orElse(FetchType.FETCH);

        long size = 0L;
        List<Map<String, Object>> allImages = new ArrayList<>();

        try (HttpClient httpClient = HttpClient.builder()
            .runContext(runContext)
            .build()) {

            for (String messageId : rMessageIds) {
                induceDelay();

                String url = rBaseUrl + "/campaign-messages/" + messageId + "/image";

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
                        "Failed to retrieve image for message " + messageId + ": " +
                            response.getStatus().getCode() + " - " + response.getBody());
                }

                JsonNode responseJson = JacksonMapper.ofJson().readTree(response.getBody());
                JsonNode dataNode = responseJson.get("data");

                if (dataNode != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> image = JacksonMapper.ofJson().convertValue(dataNode, Map.class);
                    allImages.add(image);
                    size++;
                }
            }

            Output output = applyFetchStrategy(rFetchType, allImages, runContext);
            logger.info("Successfully retrieved {} image(s) for message(s)", size);

            return Output.builder()
                .size(size)
                .row(output.getRow())
                .rows(output.getRows())
                .uri(output.getUri())
                .build();
        }
    }

}

package io.kestra.plugin.klaviyo.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.klaviyo.constants.Constants;
import io.kestra.plugin.klaviyo.models.requests.SendCampaignRequest;
import io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse;
import io.kestra.plugin.klaviyo.models.responses.success.SendCampaignSuccessResponse;
import io.kestra.plugin.klaviyo.utils.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.slf4j.Logger;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Send a Klaviyo campaign",
    description = "Triggers sending of a Klaviyo campaign using its campaign ID."
)
@Plugin(
    examples = {
        @Example(
            title = "Send a campaign",
            code = {
                "privateApiKey: \"<YOUR_API_KEY>\"",
                "revision: \"2025-07-15\"",
                "campaignId: \"abcd1234\""
            }
        )
    }
)
public class SendCampaign extends Task implements RunnableTask<SendCampaign.Output> {
    @Schema(title = "Klaviyo Private API Key")
    private Property<String> privateApiKey;

    @Schema(title = "Revision of the request", description = "Defaults to 2025-07-15")
    private Property<String> revision;

    @Schema(title = "Campaign ID to send")
    private Property<String> campaignId;

    @Override
    public SendCampaign.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        // render inputs
        String renderedApiKey = runContext.render(privateApiKey).as(String.class).orElseThrow();
        String renderedRevision = runContext.render(revision).as(String.class).orElse(Constants.DEFAULT_REVISION);
        String renderedCampaignId = runContext.render(campaignId).as(String.class).orElseThrow();

        // build request body
        SendCampaignRequest requestPayload = SendCampaignRequest.builder()
            .data(SendCampaignRequest.Data.builder()
                .type(Constants.CAMPAIGN_SEND_JOB)
                .id(renderedCampaignId)
                .build())
            .build();

        String jsonBody = objectMapper.writeValueAsString(requestPayload);

        ClassicHttpRequest request = Utils.getHttpRequest(
            Constants.CAMPAIGN_SEND_URL,
            jsonBody,
            renderedRevision,
            renderedApiKey
        );
        HttpRequest httpRequest = HttpRequest.from(request);

        // execute
        HttpResponse httpResponse = httpClient.request(httpRequest);
        int statusCode = httpResponse.getStatus().getCode();
        if (statusCode != 202) {
            ErrorResponse errorResponse = objectMapper.convertValue(httpResponse.getBody(), ErrorResponse.class);
            logger.error("Failed to send campaign: {}", errorResponse);
            return Output.builder()
                .status(Constants.ERROR)
                .statusCode(statusCode)
                .errorResponse(errorResponse)
                .build();
        }

        // success
        SendCampaignSuccessResponse success =
            objectMapper.convertValue(httpResponse.getBody(), SendCampaignSuccessResponse.class);
        logger.info("Campaign send job created successfully with ID: {}", success.getData().getId());

        return Output.builder()
            .status(Constants.SUCCESS)
            .statusCode(statusCode)
            .sendCampaignSuccessResponse(success)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "Status of the send campaign")
        private final String status;

        @Schema(title = "Status code of the response")
        private final Integer statusCode;

        @Schema(title = "Response of the send campaign - success case")
        private final SendCampaignSuccessResponse sendCampaignSuccessResponse;

        @Schema(title = "Response of the send campaign - error case")
        private final ErrorResponse errorResponse;
    }
}


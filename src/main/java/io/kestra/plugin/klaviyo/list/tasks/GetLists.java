package io.kestra.plugin.klaviyo.list.tasks;

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
import io.kestra.plugin.klaviyo.list.models.KlaviyoListGetErrorResponse;
import io.kestra.plugin.klaviyo.list.models.KlaviyoListGetSuccessResponse;
import io.kestra.plugin.klaviyo.utils.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;
import org.slf4j.Logger;

import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get Klaviyo Lists",
    description = "Fetches all lists from Klaviyo."
)
@Plugin(
    examples = {
        @Example(
            title = "Get all lists",
            code = {
                "privateApiKey: \"<YOUR_PRIVATE_API_KEY>\""
            }
        )
    }
)
public class GetLists extends Task implements RunnableTask<GetLists.Output> {
    @Schema(
        title = "Klaviyo Private API Key",
        description = "Klaviyo Private API Key"
    )
    private Property<String> privateApiKey;

    @Schema(
        title = "Revision of the API",
        description = "Revision of the API request"
    )
    private Property<String> revision;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        String renderedPrivateApiKey = runContext.render(privateApiKey).as(String.class).orElseThrow();
        String renderedRevision = runContext.render(revision).as(String.class).orElse("2025-07-15");

        logger.info("Fetching lists from Klaviyo...");

        ClassicHttpRequest request = new HttpGet(Constants.LIST_URL);
        request.addHeader(new BasicHeader("Accept", "application/vnd.api+json"));
        request.addHeader(new BasicHeader("Revision", renderedRevision));
        request.addHeader(new BasicHeader("Authorization", "Klaviyo-API-Key " + renderedPrivateApiKey));

        HttpRequest httpRequest = HttpRequest.from(request);
        HttpResponse httpResponse = httpClient.request(httpRequest);

        Object responseBody = httpResponse.getBody();

        // Try error first
        if (responseBody instanceof java.util.Map && ((java.util.Map<?, ?>) responseBody).containsKey(Constants.ERRORS)) {
            KlaviyoListGetErrorResponse errorResponse =
                objectMapper.convertValue(responseBody, KlaviyoListGetErrorResponse.class);

            KlaviyoListGetErrorResponse.ErrorDetail firstError = errorResponse.getErrors().get(0);
            logger.error("Failed to fetch lists: {}", objectMapper.writeValueAsString(errorResponse));

            return Output.builder()
                .status(Constants.ERROR)
                .errorMessage(firstError.getDetail())
                .errorTitle(firstError.getTitle())
                .errorCode(firstError.getCode())
                .build();
        }

        // Success
        KlaviyoListGetSuccessResponse successResponse =
            objectMapper.convertValue(responseBody, KlaviyoListGetSuccessResponse.class);

        if (successResponse.getData() != null) {
            List<String> listIds = successResponse.getData().stream()
                .map(KlaviyoListGetSuccessResponse.DataNode::getId)
                .toList();

            logger.info("Fetched {} lists from Klaviyo", listIds.size());

            return Output.builder()
                .status(Constants.SUCCESS)
                .listIds(listIds)
                .build();
        }

        logger.error("Unexpected response when fetching lists: {}", objectMapper.writeValueAsString(responseBody));
        return Output.builder().status(Constants.ERROR).build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "Status of the operation")
        private final String status;

        @Schema(title = "List of IDs of the fetched lists")
        private final List<String> listIds;

        @Schema(title = "Error message if failed")
        private final String errorMessage;

        @Schema(title = "Error title if failed")
        private final String errorTitle;

        @Schema(title = "Error code if failed")
        private final String errorCode;
    }
}


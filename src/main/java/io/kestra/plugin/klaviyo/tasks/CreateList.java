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
import io.kestra.plugin.klaviyo.models.requests.ListCreateRequest;
import io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse;
import io.kestra.plugin.klaviyo.models.responses.success.ListCreateSuccessResponse;
import io.kestra.plugin.klaviyo.utils.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.slf4j.Logger;

import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Create a Klaviyo list",
    description = "Creates a new list in Klaviyo."
)
@Plugin(
    examples = {
        @Example(
            title = "Create a list",
            code = {
                "name: \"Newsletter Subscribers\""
            }
        )
    }
)
public class CreateList extends Task implements RunnableTask<CreateList.Output> {
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

    @Schema(
        title = "Name of the list",
        description = "Name of the list to create"
    )
    private Property<String> name;

    @Override
    public CreateList.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        String renderedPrivateApiKey = runContext.render(privateApiKey).as(String.class).orElse(null);
        String renderedRevision = runContext.render(revision).as(String.class).orElse("2025-07-15");
        String renderedName = runContext.render(name).as(String.class).orElse(null);

        logger.info("Creating list in Klaviyo...");

        // attributes
        ListCreateRequest.Attributes attributes = ListCreateRequest.Attributes.builder()
            .name(renderedName)
            .build();

        // full request
        ListCreateRequest listCreateRequest = ListCreateRequest.builder()
            .data(ListCreateRequest.Data.builder()
                .type(Constants.LIST)
                .attributes(attributes)
                .build())
            .build();

        String listCreateRequestJson = objectMapper.writeValueAsString(listCreateRequest);

        ClassicHttpRequest request = Utils.getHttpRequest(Constants.LIST_URL, listCreateRequestJson, renderedRevision, renderedPrivateApiKey);
        HttpRequest httpRequest = HttpRequest.from(request);

        // response
        HttpResponse httpResponse = httpClient.request(httpRequest);
        int statusCode = httpResponse.getStatus().getCode();
        if (statusCode != 201) {
            ErrorResponse errorResponse = objectMapper.convertValue(httpResponse.getBody(), ErrorResponse.class);
            List<ErrorResponse.ErrorDetail> errorDetails = errorResponse.getErrors();
            ErrorResponse.ErrorDetail errorDetail = errorDetails.getFirst();
            logger.error("Failed to create list in Klaviyo: {}", objectMapper.writeValueAsString(errorDetails));
            return Output.builder()
                .status(Constants.ERROR)
                .errorMessage(errorDetail.getDetail())
                .errorStatusCode(statusCode)
                .errorTitle(errorDetail.getTitle())
                .errorCode(errorDetail.getCode())
                .build();
        }

        // success response
        ListCreateSuccessResponse listCreateSuccessResponse = objectMapper.convertValue(httpResponse.getBody(), ListCreateSuccessResponse.class);
        ListCreateSuccessResponse.Data dataNode = listCreateSuccessResponse.getData();
        if (dataNode != null) {
            ListCreateSuccessResponse.Attributes attrs = dataNode.getAttributes();
            if (attrs != null) {
                logger.info("List created successfully in Klaviyo with ID: {}", dataNode.getId());
                return Output.builder()
                    .status(Constants.SUCCESS)
                    .listId(dataNode.getId())
                    .name(attrs.getName())
                    .created(attrs.getCreated())
                    .updated(attrs.getUpdated())
                    .build();
            }
        }

        logger.error("Failed to create list in Klaviyo. Response: {}", objectMapper.writeValueAsString(httpResponse.getBody()));
        return Output.builder().build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "Status of the list creation operation")
        private final String status;

        @Schema(title = "ID of the created list")
        private final String listId;

        @Schema(title = "Name of the created list")
        private final String name;

        @Schema(title = "Creation timestamp of the list")
        private final String created;

        @Schema(title = "Last updated timestamp of the list")
        private final String updated;

        @Schema(title = "Error message if list creation failed")
        private final String errorMessage;

        @Schema(title = "HTTP status code if list creation failed")
        private final int errorStatusCode;

        @Schema(title = "Error title if list creation failed")
        private final String errorTitle;

        @Schema(title = "Error code if list creation failed")
        private final String errorCode;
    }
}

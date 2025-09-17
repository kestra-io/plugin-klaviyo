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
                "privateApiKey: \"pk_xxxxx\"",
                "revision: \"2025-07-15\"",
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
        title = "Attributes of the list",
        description = "Attributes of the list"
    )
    private Property<Attributes> attributes;

    @SuperBuilder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Attributes of the list")
    public static class Attributes {
        @Schema(
            title = "Name of the list",
            description = "Name of the list to create"
        )
        private Property<String> name;
    }

    @Override
    public CreateList.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        String renderedPrivateApiKey = runContext.render(privateApiKey).as(String.class).orElse(null);
        String renderedRevision = runContext.render(revision).as(String.class).orElse(Constants.DEFAULT_REVISION);
        Attributes renderedAttributes = runContext.render(attributes).as(Attributes.class).orElse(null);

        logger.info("Creating list in Klaviyo...");

        // attributes
        ListCreateRequest.Attributes attributes = renderedAttributes != null ? ListCreateRequest.Attributes.builder()
            .name(renderedAttributes.getName() != null ? renderedAttributes.getName().toString() : null)
            .build() : null;

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
            logger.error("Failed to create list in Klaviyo. Status Code: {}, Error: {}", statusCode, objectMapper.writeValueAsString(errorResponse));
            return Output.builder()
                .status(Constants.ERROR)
                .statusCode(statusCode)
                .errorResponse(errorResponse)
                .build();
        }

        // success response
        ListCreateSuccessResponse listCreateSuccessResponse = objectMapper.convertValue(httpResponse.getBody(), ListCreateSuccessResponse.class);
        logger.info("List created successfully in Klaviyo with ID: {}", listCreateSuccessResponse.getData().getId());
        return Output.builder()
            .status(Constants.SUCCESS)
            .statusCode(statusCode)
            .listCreateSuccessResponse(listCreateSuccessResponse)
            .errorResponse(null)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "Status of the list creation")
        private final String status;

        @Schema(title = "Status code of the response")
        private final Integer statusCode;

        @Schema(title = "Response of the list creation - success case")
        private final ListCreateSuccessResponse listCreateSuccessResponse;

        @Schema(title = "Response of the list creation - error case")
        private final ErrorResponse errorResponse;
    }
}

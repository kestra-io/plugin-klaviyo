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
import io.kestra.plugin.klaviyo.models.requests.AddProfilesToListRequest;
import io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse;
import io.kestra.plugin.klaviyo.utils.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Add profiles to a Klaviyo list",
    description = "Associates one or more profiles with an existing Klaviyo list."
)
@Plugin(
    examples = {
        @Example(
            title = "Add profiles to a list",
            code = {
                "privateApiKey: \"pk_xxxxx\"",
                "revision: \"2025-07-15\"",
                "listId: \"YnP9Es\"",
                "profileIds:",
                "  - \"01H23A4BCD5EFGH678IJK9LMNO\"",
                "  - \"01H23A4BCD5EFGH678IJK9PQRS\""
            }
        )
    }
)
public class AddProfilesToList extends Task implements RunnableTask<AddProfilesToList.Output> {
    @Schema(
        title = "Klaviyo Private API Key",
        description = "Klaviyo Private API Key"
    )
    private Property<String> privateApiKey;

    @Schema(
        title = "Revision of the request",
        description = "API revision date"
    )
    private Property<String> revision;

    @Schema(
        title = "ID of the Klaviyo list",
        description = "The List ID where profiles will be added"
    )
    private Property<String> listId;

    @Schema(
        title = "Profile IDs to add",
        description = "List of Klaviyo profile IDs to associate with the list"
    )
    private Property<List<String>> profileIds;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        String renderedPrivateApiKey = runContext.render(privateApiKey).as(String.class).orElse(null);
        String renderedRevision = runContext.render(revision).as(String.class).orElse("2025-07-15");
        String renderedListId = runContext.render(listId).as(String.class).orElseThrow();
        List<String> renderedProfileIds = runContext.render(profileIds).asList(String.class);

        logger.info("Adding {} profiles to list {}", renderedProfileIds.size(), renderedListId);

        // request body
        AddProfilesToListRequest requestPayload = AddProfilesToListRequest.builder()
            .data(renderedProfileIds.stream()
            .map(id -> AddProfilesToListRequest.Data.builder()
                    .type(Constants.PROFILE)
                    .id(id)
                    .build())
                .collect(Collectors.toList()))
            .build();

        String requestJson = objectMapper.writeValueAsString(requestPayload);

        String url = Constants.LIST_URL + "/" + renderedListId + "/relationships/profiles";
        ClassicHttpRequest request = Utils.getHttpRequest(url, requestJson, renderedRevision, renderedPrivateApiKey);
        HttpRequest httpRequest = HttpRequest.from(request);

        // response
        HttpResponse httpResponse = httpClient.request(httpRequest);
        int statusCode = httpResponse.getStatus().getCode();

        if (statusCode != 204) {
            ErrorResponse errorResponse = objectMapper.convertValue(httpResponse.getBody(), ErrorResponse.class);
            logger.error("Failed to add profiles to list {}: {}", renderedListId, errorResponse);
            return Output.builder()
                .status(Constants.ERROR)
                .statusCode(statusCode)
                .errorResponse(errorResponse)
                .build();
        }

        // success
        logger.info("Successfully added profiles to list {}", renderedListId);

        return Output.builder()
            .status(Constants.SUCCESS)
            .statusCode(statusCode)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "Status of the profile addition to list")
        private final String status;

        @Schema(title = "Status code of the response")
        private final Integer statusCode;

        @Schema(title = "Response of the profile addition - error case")
        private final ErrorResponse errorResponse;
    }
}


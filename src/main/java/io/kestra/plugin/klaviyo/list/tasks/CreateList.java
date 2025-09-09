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
import io.kestra.plugin.klaviyo.list.models.KlaviyoListCreateSuccessResponse;
import io.kestra.plugin.klaviyo.list.models.KlaviyoListCreateErrorResponse;
import io.kestra.plugin.klaviyo.utils.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Create a Klaviyo List",
    description = "Creates a new list in Klaviyo."
)
@Plugin(
    examples = {
        @Example(
            title = "Create a list with a name",
            code = {
                "privateApiKey: \"<YOUR_PRIVATE_API_KEY>\"",
                "name: \"My Customers\""
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
        description = "The name of the list to be created"
    )
    private Property<String> name;

    @Override
    public CreateList.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        String renderedPrivateApiKey = runContext.render(privateApiKey).as(String.class).orElseThrow();
        String renderedRevision = runContext.render(revision).as(String.class).orElse("2025-07-15");
        String renderedName = runContext.render(name).as(String.class).orElseThrow();

        logger.info("Creating list '{}' in Klaviyo...", renderedName);

        // attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", renderedName);

        // data
        Map<String, Object> data = new HashMap<>();
        data.put(Constants.TYPE, "list");
        data.put(Constants.ATTRIBUTES, attributes);

        // request body
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("data", data);

        String requestBodyJson = objectMapper.writeValueAsString(requestBodyMap);

        ClassicHttpRequest request = new HttpPost(Constants.LIST_URL);
        request.setEntity(new StringEntity(requestBodyJson, ContentType.APPLICATION_JSON));
        request.addHeader(new BasicHeader("Content-Type", "application/vnd.api+json"));
        request.addHeader(new BasicHeader("Accept", "application/vnd.api+json"));
        request.addHeader(new BasicHeader("Revision", renderedRevision));
        request.addHeader(new BasicHeader("Authorization", "Klaviyo-API-Key " + renderedPrivateApiKey));

        HttpRequest httpRequest = HttpRequest.from(request);

        HttpResponse httpResponse = httpClient.request(httpRequest);
        Object responseBody = httpResponse.getBody();
        Map<String, Object> responseMap = objectMapper.convertValue(responseBody, Map.class);

        // error response
        if (responseMap.containsKey(Constants.ERRORS)) {
            KlaviyoListCreateErrorResponse klaviyoListCreateErrorResponse = objectMapper.convertValue(responseBody, KlaviyoListCreateErrorResponse.class);
            List<KlaviyoListCreateErrorResponse.ErrorDetail> errorDetails = klaviyoListCreateErrorResponse.getErrors();
            KlaviyoListCreateErrorResponse.ErrorDetail errorDetail = errorDetails.get(0);
            logger.error("Failed to create list in Klaviyo: {}", objectMapper.writeValueAsString(errorDetails));
            return Output.builder()
                .status(Constants.ERROR)
                .errorMessage(errorDetail.getDetail())
                .errorTitle(errorDetail.getTitle())
                .errorCode(errorDetail.getCode())
                .build();
        }

        // success response
        KlaviyoListCreateSuccessResponse klaviyoListCreateSuccessResponse = objectMapper.convertValue(responseBody, KlaviyoListCreateSuccessResponse.class);
        KlaviyoListCreateSuccessResponse.DataNode dataNode = klaviyoListCreateSuccessResponse.getData();

        if (dataNode != null) {
            KlaviyoListCreateSuccessResponse.Attributes attrs = dataNode.getAttributes();
            if (attrs != null) {
                logger.info("List created successfully in Klaviyo with ID: {}", dataNode.getId());
                return Output.builder()
                    .status(Constants.SUCCESS)
                    .listId(dataNode.getId())
                    .name(attrs.getName())
                    .created(attrs.getCreated())
                    .updated(attrs.getUpdated())
                    .optInProcess(attrs.getOptInProcess())
                    .build();
            }
        }

        logger.error("Failed to create list in Klaviyo. Response: {}", objectMapper.writeValueAsString(responseBody));
        return Output.builder().status(Constants.ERROR).build();
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

        @Schema(title = "Creation date of the list")
        private final String created;

        @Schema(title = "Last update date of the list")
        private final String updated;

        @Schema(title = "Opt-in process type of the list")
        private final String optInProcess;

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

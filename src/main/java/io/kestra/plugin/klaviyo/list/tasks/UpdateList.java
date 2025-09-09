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
import io.kestra.plugin.klaviyo.list.models.KlaviyoListUpdateErrorResponse;
import io.kestra.plugin.klaviyo.list.models.KlaviyoListUpdateSuccessResponse;
import io.kestra.plugin.klaviyo.utils.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Update a Klaviyo List",
    description = "Updates the name of an existing Klaviyo list."
)
@Plugin(
    examples = {
        @Example(
            title = "Update list name",
            code = {
                "privateApiKey: \"<YOUR_PRIVATE_API_KEY>\"",
                "listId: \"<LIST_ID>\"",
                "name: \"Updated Customers\""
            }
        )
    }
)
public class UpdateList extends Task implements RunnableTask<UpdateList.Output> {
    @Schema(title = "Klaviyo Private API Key")
    private Property<String> privateApiKey;

    @Schema(title = "Revision of the API")
    private Property<String> revision;

    @Schema(title = "ID of the list to update")
    private Property<String> listId;

    @Schema(title = "New name of the list")
    private Property<String> name;

    @Override
    public UpdateList.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        String renderedPrivateApiKey = runContext.render(privateApiKey).as(String.class).orElseThrow();
        String renderedRevision = runContext.render(revision).as(String.class).orElse("2025-07-15");
        String renderedListId = runContext.render(listId).as(String.class).orElseThrow();
        String renderedName = runContext.render(name).as(String.class).orElseThrow();

        logger.info("Updating list '{}' in Klaviyo...", renderedListId);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", renderedName);

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.TYPE, Constants.LIST);
        data.put(Constants.ID, renderedListId);
        data.put(Constants.ATTRIBUTES, attributes);

        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put(Constants.DATA, data);

        String requestBodyJson = objectMapper.writeValueAsString(requestBodyMap);

        ClassicHttpRequest request = new HttpPatch(Constants.LIST_URL + "/" + renderedListId);
        request.setEntity(new StringEntity(requestBodyJson, ContentType.APPLICATION_JSON));
        request.addHeader(new BasicHeader("Content-Type", "application/vnd.api+json"));
        request.addHeader(new BasicHeader("Accept", "application/vnd.api+json"));
        request.addHeader(new BasicHeader("Revision", renderedRevision));
        request.addHeader(new BasicHeader("Authorization", "Klaviyo-API-Key " + renderedPrivateApiKey));

        HttpRequest httpRequest = HttpRequest.from(request);

        HttpResponse httpResponse = httpClient.request(httpRequest);
        Object responseBody = httpResponse.getBody();
        Map<String, Object> responseMap = objectMapper.convertValue(responseBody, Map.class);

        // Error response
        if (responseMap.containsKey(Constants.ERRORS)) {
            KlaviyoListUpdateErrorResponse errorResponse = objectMapper.convertValue(responseBody, KlaviyoListUpdateErrorResponse.class);
            KlaviyoListUpdateErrorResponse.ErrorDetail errorDetail = errorResponse.getErrors().get(0);
            logger.error("Failed to update list: {}", objectMapper.writeValueAsString(errorResponse.getErrors()));
            return Output.builder()
                .status(Constants.ERROR)
                .errorMessage(errorDetail.getDetail())
                .errorTitle(errorDetail.getTitle())
                .errorCode(errorDetail.getCode())
                .build();
        }

        // Success response
        KlaviyoListUpdateSuccessResponse successResponse = objectMapper.convertValue(responseBody, KlaviyoListUpdateSuccessResponse.class);
        KlaviyoListUpdateSuccessResponse.DataNode dataNode = successResponse.getData();

        if (dataNode != null && dataNode.getAttributes() != null) {
            KlaviyoListUpdateSuccessResponse.Attributes attrs = dataNode.getAttributes();
            logger.info("List updated successfully with ID: {}", dataNode.getId());
            return Output.builder()
                .status(Constants.SUCCESS)
                .listId(dataNode.getId())
                .name(attrs.getName())
                .created(attrs.getCreated())
                .updated(attrs.getUpdated())
                .optInProcess(attrs.getOptInProcess())
                .build();
        }

        logger.error("Failed to update list. Response: {}", objectMapper.writeValueAsString(responseBody));
        return Output.builder().status(Constants.ERROR).build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        private final String status;
        private final String listId;
        private final String name;
        private final String created;
        private final String updated;
        private final String optInProcess;
        private final String errorMessage;
        private final String errorTitle;
        private final String errorCode;
        private final int errorStatusCode;
    }
}


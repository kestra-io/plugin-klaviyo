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
import io.kestra.plugin.klaviyo.models.requests.TemplateCreateRequest;
import io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse;
import io.kestra.plugin.klaviyo.models.responses.success.TemplateCreateSuccessResponse;
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
    title = "Create a Klaviyo template",
    description = "Creates a new email template in Klaviyo."
)
@Plugin(
    examples = {
        @Example(
            title = "Create a template with HTML and text",
            code = {
                "privateApiKey: \"pk_xxxxx\"",
                "name: \"Welcome Template\"",
                "html: \"<html><body><h1>Welcome!</h1></body></html>\"",
                "text: \"Welcome!\""
            }
        )
    }
)
public class CreateTemplate extends Task implements RunnableTask<CreateTemplate.Output> {
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
        title = "Name of the template",
        description = "Display name of the email template"
    )
    private Property<String> name;

    @Schema(
        title = "Editor type",
        description = "Type of editor used for the template"
    )
    private Property<String> editorType;

    @Schema(
        title = "HTML content of the template",
        description = "HTML version of the email template"
    )
    private Property<String> html;

    @Schema(
        title = "Text content of the template",
        description = "Plain text version of the email template"
    )
    private Property<String> text;

    @Schema(
        title = "AMP content of the template",
        description = "AMP content of the email template"
    )
    private Property<String> amp;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        String renderedPrivateApiKey = runContext.render(privateApiKey).as(String.class).orElse(null);
        String renderedRevision = runContext.render(revision).as(String.class).orElse("2025-07-15");
        String renderedName = runContext.render(name).as(String.class).orElseThrow();
        String renderedEditorType = runContext.render(editorType).as(String.class).orElse(null);
        String renderedHtml = runContext.render(html).as(String.class).orElse(null);
        String renderedText = runContext.render(text).as(String.class).orElse(null);
        String renderedAmp = runContext.render(amp).as(String.class).orElse(null);

        logger.info("Creating template in Klaviyo with name: {}", renderedName);

        // attributes
        TemplateCreateRequest.Attributes attributes = TemplateCreateRequest.Attributes.builder()
            .name(renderedName)
            .editor_type(renderedEditorType)
            .html(renderedHtml)
            .text(renderedText)
            .amp(renderedAmp)
            .build();

        TemplateCreateRequest requestPayload = TemplateCreateRequest.builder()
            .data(TemplateCreateRequest.Data.builder()
                .type(Constants.TEMPLATE)
                .attributes(attributes)
                .build())
            .build();

        String requestJson = objectMapper.writeValueAsString(requestPayload);

        ClassicHttpRequest request = Utils.getHttpRequest(Constants.TEMPLATE_URL, requestJson, renderedRevision, renderedPrivateApiKey);
        HttpRequest httpRequest = HttpRequest.from(request);

        // response
        HttpResponse httpResponse = httpClient.request(httpRequest);
        int statusCode = httpResponse.getStatus().getCode();
        if (statusCode != 201) {
            ErrorResponse errorResponse = objectMapper.convertValue(httpResponse.getBody(), ErrorResponse.class);
            List<ErrorResponse.ErrorDetail> errorDetails = errorResponse.getErrors();
            ErrorResponse.ErrorDetail errorDetail = errorDetails.getFirst();
            logger.error("Failed to create template: {}", objectMapper.writeValueAsString(errorDetails));
            return Output.builder()
                .status(Constants.ERROR)
                .errorMessage(errorDetail.getDetail())
                .errorStatusCode(statusCode)
                .errorTitle(errorDetail.getTitle())
                .errorCode(errorDetail.getCode())
                .build();
        }

        // success
        TemplateCreateSuccessResponse successResponse = objectMapper.convertValue(httpResponse.getBody(), TemplateCreateSuccessResponse.class);
        TemplateCreateSuccessResponse.Data dataNode = successResponse.getData();
        if(dataNode != null) {
            logger.info("Template created successfully with ID: {}", dataNode.getId());

            return Output.builder()
                .status(Constants.SUCCESS)
                .templateId(dataNode.getId())
                .name(dataNode.getAttributes().getName())
                .editor_type(dataNode.getAttributes().getEditorType())
                .html(dataNode.getAttributes().getHtml())
                .text(dataNode.getAttributes().getText())
                .amp(dataNode.getAttributes().getAmp())
                .build();
        }

        logger.error("Failed to create template. Response: {}", objectMapper.writeValueAsString(httpResponse.getBody()));
        return Output.builder().build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "Status of the template creation")
        private final String status;

        @Schema(title = "ID of the created template")
        private final String templateId;

        @Schema(title = "Name of the created template")
        private final String name;

        @Schema(title = "Editor type of the created template")
        private final String editor_type;

        @Schema(title = "HTML content of the created template")
        private final String html;

        @Schema(title = "Text content of the created template")
        private final String text;

        @Schema(title = "AMP content of the created template")
        private final String amp;

        @Schema(title = "Error message if creation failed")
        private final String errorMessage;

        @Schema(title = "HTTP status code if creation failed")
        private final int errorStatusCode;

        @Schema(title = "Error title if creation failed")
        private final String errorTitle;

        @Schema(title = "Error code if creation failed")
        private final String errorCode;
    }
}

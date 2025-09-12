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
                "revision: \"2025-07-15\"",
                "name: \"Welcome Template\"",
                "editorType: \"html\"",
                "html: \"<html><body><h1>Welcome!</h1></body></html>\"",
                "text: \"Welcome!\"",
                "amp: \"<html amp4email><head><meta charset=\\\"utf-8\\\"><script async src=\\\"https://cdn.ampproject.org/v0.js\\\"></script><style amp4email-boilerplate>body{visibility:hidden}</style><style amp-custom>h1{color:blue}</style></head><body><h1>Welcome!</h1></body></html>\""
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
        title = "Attributes of the template",
        description = "Attributes of the template"
    )
    private Property<Attributes> attributes;

    @SuperBuilder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Attributes of the template")
    public static class Attributes {

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
    }

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        String renderedPrivateApiKey = runContext.render(privateApiKey).as(String.class).orElse(null);
        String renderedRevision = runContext.render(revision).as(String.class).orElse(Constants.DEFAULT_REVISION);
        Attributes renderedAttributes = runContext.render(attributes).as(Attributes.class).orElse(null);

        logger.info("Creating template in Klaviyo with name: {}", renderedAttributes.getName().toString());

        // attributes
        TemplateCreateRequest.Attributes attributes = renderedAttributes != null ? TemplateCreateRequest.Attributes.builder()
            .name(renderedAttributes.getName() != null ? renderedAttributes.getName().toString() : null)
            .editor_type(renderedAttributes.getEditorType() != null ? renderedAttributes.getEditorType().toString() : null)
            .html(renderedAttributes.getHtml() != null ? renderedAttributes.getHtml().toString() : null)
            .text(renderedAttributes.getText() != null ? renderedAttributes.getText().toString() : null)
            .amp(renderedAttributes.getAmp() != null ? renderedAttributes.getAmp().toString() : null)
            .build() : null;

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
            return Output.builder()
                .status(Constants.ERROR)
                .statusCode(statusCode)
                .errorResponse(errorResponse)
                .build();
        }

        // success
        TemplateCreateSuccessResponse templateCreateSuccessResponse = objectMapper.convertValue(httpResponse.getBody(), TemplateCreateSuccessResponse.class);
        logger.info("Template created successfully in Klaviyo with ID: {}", templateCreateSuccessResponse.getData().getId());
        return Output.builder()
            .status(Constants.SUCCESS)
            .statusCode(statusCode)
            .templateCreateSuccessResponse(templateCreateSuccessResponse)
            .errorResponse(null)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "Status of the template creation")
        private final String status;

        @Schema(title = "Status code of the response")
        private final Integer statusCode;

        @Schema(title = "Response of the template creation - success case")
        private final TemplateCreateSuccessResponse templateCreateSuccessResponse;

        @Schema(title = "Response of the template creation - error case")
        private final ErrorResponse errorResponse;
    }
}

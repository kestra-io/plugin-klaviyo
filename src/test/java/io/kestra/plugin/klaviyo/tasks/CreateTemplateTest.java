package io.kestra.plugin.klaviyo.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.klaviyo.constants.Constants;
import io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse;
import io.kestra.plugin.klaviyo.models.responses.success.TemplateCreateSuccessResponse;
import io.kestra.plugin.klaviyo.utils.Utils;
import jakarta.inject.Inject;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@KestraTest
@ExtendWith(MockitoExtension.class)
class CreateTemplateTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run_success() throws Exception {
        CreateTemplate.Attributes attributes = CreateTemplate.Attributes.builder()
            .name(Property.of("Welcome Email"))
            .editorType(Property.of("custom"))
            .html(Property.of("<h1>Hello, kestra!</h1>"))
            .text(Property.of("Hello, kestra!"))
            .amp(Property.of("<html amp4email><head><meta charset=\"utf-8\">"))
            .build();

        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "attributes", attributes
            )
        );

        CreateTemplate task = CreateTemplate.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .attributes(Property.of(attributes))
            .build();

        TemplateCreateSuccessResponse templateCreateSuccessResponse = TemplateCreateSuccessResponse.builder()
            .data(
                TemplateCreateSuccessResponse.Data.builder()
                    .id("template_123")
                    .type(Constants.TEMPLATE)
                    .attributes(
                        TemplateCreateSuccessResponse.Attributes.builder()
                            .name("Welcome Email")
                            .editorType("custom")
                            .html("<h1>Hello, kestra!</h1>")
                            .text("Hello, kestra!")
                            .amp("<html amp4email><head><meta charset=\"utf-8\">")
                            .build()
                    )
                    .build()
            )
            .build();

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.TEMPLATE_URL));

        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class)) {
            HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
            HttpResponse mockResponse = Mockito.mock(HttpResponse.class);

            Mockito.when(mockResponse.getStatus()).thenReturn(HttpResponse.Status.builder().code(201).build());
            Mockito.when(mockResponse.getBody()).thenReturn(templateCreateSuccessResponse);

            utilsMockedStatic.when(() -> Utils.getMapper()).thenReturn(mockMapper);
            utilsMockedStatic.when(() -> Utils.getHttpRequest(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString()
            )).thenReturn(mockClassicHttpRequest);
            utilsMockedStatic.when(() -> Utils.getHttpClient(runContext)).thenReturn(mockedHttpClient);
            Mockito.when(mockedHttpClient.request(any(HttpRequest.class))).thenReturn(mockResponse);

            CreateTemplate.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.SUCCESS);
            assert output.getStatusCode() == 201;
            assert output.getTemplateCreateSuccessResponse() != null;
            assert output.getTemplateCreateSuccessResponse().getData().getId().equals("template_123");
            assert output.getErrorResponse() == null;
        }
    }

    @Test
    void run_error() throws Exception {
        CreateTemplate.Attributes attributes = CreateTemplate.Attributes.builder()
            .name(Property.of("Welcome Email"))
            .editorType(Property.of("custom"))
            .html(Property.of("<h1>Hello, kestra!</h1>"))
            .text(Property.of("Hello, kestra!"))
            .amp(Property.of("<html amp4email><head><meta charset=\"utf-8\">"))
            .build();

        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "attributes", attributes
            )
        );

        CreateTemplate task = CreateTemplate.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .attributes(Property.of(attributes))
            .build();

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.TEMPLATE_URL));

        ErrorResponse errorResponse = ErrorResponse.builder()
            .errors(
                java.util.List.of(
                    ErrorResponse.ErrorDetail.builder()
                        .id("invalid_api_key")
                        .code("401")
                        .title("Invalid API Key")
                        .detail("The provided API key is invalid.")
                        .source(
                            ErrorResponse.Source.builder()
                                .pointer("/data/attributes/api_key")
                                .build()
                        )
                        .build()
                )
            )
            .build();

        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class)) {
            HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
            HttpResponse mockResponse = Mockito.mock(HttpResponse.class);

            Mockito.when(mockResponse.getStatus()).thenReturn(HttpResponse.Status.builder().code(401).build());
            Mockito.when(mockResponse.getBody()).thenReturn(errorResponse);

            utilsMockedStatic.when(() -> Utils.getMapper()).thenReturn(mockMapper);
            utilsMockedStatic.when(() -> Utils.getHttpRequest(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString()
            )).thenReturn(mockClassicHttpRequest);
            utilsMockedStatic.when(() -> Utils.getHttpClient(runContext)).thenReturn(mockedHttpClient);
            Mockito.when(mockedHttpClient.request(any(HttpRequest.class))).thenReturn(mockResponse);

            CreateTemplate.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.ERROR);
            assert output.getStatusCode() == 401;
            assert output.getTemplateCreateSuccessResponse() == null;
            assert output.getErrorResponse() != null;
            assert output.getErrorResponse().getErrors().getFirst().getTitle().equals("Invalid API Key");
        }
    }
}

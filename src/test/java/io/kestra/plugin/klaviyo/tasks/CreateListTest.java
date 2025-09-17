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
import io.kestra.plugin.klaviyo.models.responses.success.ListCreateSuccessResponse;
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
class CreateListTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run_success() throws Exception {
        CreateList.Attributes attributes= CreateList.Attributes.builder()
            .name(Property.of("Newsletter Subscribers"))
            .build();

        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "attributes", attributes
            )
        );

        CreateList task = CreateList.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .attributes(Property.of(attributes))
            .build();

        ListCreateSuccessResponse listCreateSuccessResponse = ListCreateSuccessResponse.builder()
            .data(
                ListCreateSuccessResponse.Data.builder()
                    .id("12345")
                    .type(Constants.LIST)
                    .attributes(
                        ListCreateSuccessResponse.Attributes.builder()
                            .name("Newsletter Subscribers")
                            .build()
                    )
                    .links(
                        Map.of(
                            "self", "https://a.klaviyo.com/api/v2/lists/12345"
                        )
                    )
                    .build()
            )
            .build();

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.LIST_URL));

        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class)) {
            HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
            HttpResponse mockResponse = Mockito.mock(HttpResponse.class);

            Mockito.when(mockResponse.getStatus()).thenReturn(HttpResponse.Status.builder().code(201).build());
            Mockito.when(mockResponse.getBody()).thenReturn(listCreateSuccessResponse);

            utilsMockedStatic.when(() -> Utils.getMapper()).thenReturn(mockMapper);
            utilsMockedStatic.when(()-> Utils.getHttpRequest(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(mockClassicHttpRequest);
            utilsMockedStatic.when(() -> Utils.getHttpClient(runContext)).thenReturn(mockedHttpClient);
            Mockito.when(mockedHttpClient.request(any(HttpRequest.class))).thenReturn(mockResponse);

            CreateList.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.SUCCESS);
            assert output.getStatusCode() == 201;
            assert output.getListCreateSuccessResponse() != null;
            assert output.getListCreateSuccessResponse().getData().getId().equals("12345");
            assert output.getErrorResponse() == null;
        }
    }

    @Test
    void run_error() throws Exception {
        CreateList.Attributes attributes = CreateList.Attributes.builder()
            .name(Property.of("Newsletter Subscribers"))
            .build();

        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "attributes", attributes
            )
        );

        CreateList task = CreateList.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .attributes(Property.of(attributes))
            .build();

        // Mock error response
        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.LIST_URL));

        // Create a mock error response object
        ErrorResponse errorResponse =
            io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse.builder()
                .errors(
                    java.util.List.of(
                        io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse.ErrorDetail.builder()
                            .id("invalid_api_key")
                            .code("401")
                            .title("Invalid API Key")
                            .detail("The provided API key is invalid.")
                            .source(
                                io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse.Source.builder()
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

            CreateList.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.ERROR);
            assert output.getStatusCode() == 401;
            assert output.getListCreateSuccessResponse() == null;
            assert output.getErrorResponse() != null;
            assert output.getErrorResponse().getErrors().getFirst().getTitle().equals("Invalid API Key");
        }
    }
}
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
import io.kestra.plugin.klaviyo.models.requests.AddProfilesToListRequest;
import io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse;
import io.kestra.plugin.klaviyo.utils.Utils;
import jakarta.inject.Inject;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@KestraTest
@ExtendWith(MockitoExtension.class)
class AddProfilesToListTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run_success() throws Exception {
        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "listId", "list_123",
                "profileIds", List.of("profile_123", "profile_456")
            )
        );

        AddProfilesToList task = AddProfilesToList.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .listId(Property.of("{{ listId }}"))
            .profileIds(Property.of(List.of("{{ profileIds }}")))
            .build();

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.LIST_URL));

        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class)) {
            HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
            HttpResponse mockResponse = Mockito.mock(HttpResponse.class);

            Mockito.when(mockResponse.getStatus()).thenReturn(HttpResponse.Status.builder().code(204).build());

            utilsMockedStatic.when(() -> Utils.getMapper()).thenReturn(mockMapper);
            utilsMockedStatic.when(() -> Utils.getHttpRequest(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString()
            )).thenReturn(mockClassicHttpRequest);
            utilsMockedStatic.when(() -> Utils.getHttpClient(runContext)).thenReturn(mockedHttpClient);
            Mockito.when(mockedHttpClient.request(any(HttpRequest.class))).thenReturn(mockResponse);

            AddProfilesToList.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.SUCCESS);
            assert output.getStatusCode() == 204;
            assert output.getErrorResponse() == null;
        }
    }

    @Test
    void run_error() throws Exception {
        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "listId", "list_123",
                "profileIds", List.of("profile_123", "profile_456")
            )
        );

        AddProfilesToList task = AddProfilesToList.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .listId(Property.of("{{ listId }}"))
            .profileIds(Property.of(List.of("{{ profileIds }}")))
            .build();

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.LIST_URL));

        ErrorResponse errorResponse = ErrorResponse.builder()
            .errors(
                List.of(
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

            AddProfilesToList.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.ERROR);
            assert output.getStatusCode() == 401;
            assert output.getErrorResponse() != null;
            assert output.getErrorResponse().getErrors().getFirst().getTitle().equals("Invalid API Key");
        }
    }
}
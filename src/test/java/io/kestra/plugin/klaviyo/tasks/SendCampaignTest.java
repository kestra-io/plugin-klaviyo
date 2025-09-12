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
import io.kestra.plugin.klaviyo.models.responses.success.SendCampaignSuccessResponse;
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
class SendCampaignTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run_success() throws Exception {
        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "campaignId", "cmp_123"
            )
        );

        SendCampaign task = SendCampaign.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .campaignId(Property.of("{{ campaignId }}"))
            .build();

        SendCampaignSuccessResponse successResponse = SendCampaignSuccessResponse.builder()
            .data(
                SendCampaignSuccessResponse.Data.builder()
                    .id("send_job_123")
                    .type(Constants.CAMPAIGN_SEND_JOB)
                    .attributes(
                        SendCampaignSuccessResponse.Attributes.builder()
                            .status("scheduled")
                            .build()
                    )
                    .build()
            )
            .build();

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.CAMPAIGN_SEND_URL));

        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class)) {
            HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
            HttpResponse mockResponse = Mockito.mock(HttpResponse.class);

            Mockito.when(mockResponse.getStatus()).thenReturn(HttpResponse.Status.builder().code(202).build());
            Mockito.when(mockResponse.getBody()).thenReturn(successResponse);

            utilsMockedStatic.when(() -> Utils.getMapper()).thenReturn(mockMapper);
            utilsMockedStatic.when(() -> Utils.getHttpRequest(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString()
            )).thenReturn(mockClassicHttpRequest);
            utilsMockedStatic.when(() -> Utils.getHttpClient(runContext)).thenReturn(mockedHttpClient);
            Mockito.when(mockedHttpClient.request(any(HttpRequest.class))).thenReturn(mockResponse);

            SendCampaign.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.SUCCESS);
            assert output.getStatusCode() == 202;
            assert output.getSendCampaignSuccessResponse() != null;
            assert output.getErrorResponse() == null;
        }
    }

    @Test
    void run_error() throws Exception {
        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "campaignId", "cmp_123"
            )
        );

        SendCampaign task = SendCampaign.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .campaignId(Property.of("{{ campaignId }}"))
            .build();

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

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.CAMPAIGN_SEND_URL));

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

            SendCampaign.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.ERROR);
            assert output.getStatusCode() == 401;
            assert output.getSendCampaignSuccessResponse() == null;
            assert output.getErrorResponse() != null;
            assert output.getErrorResponse().getErrors().getFirst().getTitle().equals("Invalid API Key");
        }
    }
}

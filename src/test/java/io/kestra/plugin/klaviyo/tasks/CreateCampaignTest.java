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
import io.kestra.plugin.klaviyo.models.responses.success.CampaignCreateSuccessResponse;
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
class CreateCampaignTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run_success() throws Exception {
        // Build attributes for the campaign
        CreateCampaign.Attributes attributes = CreateCampaign.Attributes.builder()
            .name(Property.of("September Promo Campaign"))
            .audiences(Property.of(CreateCampaign.Audiences.builder()
                .included(Property.of(List.of("aud_1", "aud_2")))
                .excluded(Property.of(List.of("aud_3")))
                .build()))
            .campaignMessages(Property.of(List.of(
                CreateCampaign.CampaignMessageData.builder()
                    .attributes(Property.of(CreateCampaign.CampaignMessageAttributes.builder()
                        .definition(Property.of(CreateCampaign.Definition.builder()
                            .channel(Property.of("email"))
                            .label(Property.of("Promo Email"))
                            .content(Property.of(CreateCampaign.Content.builder()
                                .subject(Property.of("Big Sale!"))
                                .preview_text(Property.of("Don't miss out"))
                                .from_email(Property.of("promo@example.com"))
                                .from_label(Property.of("Promo Team"))
                                .build()))
                            .build()))
                        .build()))
                    .relationships(Property.of(CreateCampaign.CampaignMessageRelationships.builder()
                        .image(Property.of(CreateCampaign.ImageRelationship.builder()
                            .data(Property.of(CreateCampaign.ImageData.builder()
                                .id(Property.of("img_123"))
                                .build()))
                            .build()))
                        .build()))
                    .build()
            )))
            .send_strategy(Property.of(CreateCampaign.SendStrategy.builder()
                .method(Property.of("immediate"))
                .build()))
            .send_options(Property.of(CreateCampaign.SendOptions.builder()
                .use_smart_sending(Property.of(true))
                .build()))
            .tracking_options(Property.of(CreateCampaign.TrackingOptions.builder()
                .add_tracking_params(Property.of(true))
                .is_tracking_clicks(Property.of(true))
                .is_tracking_opens(Property.of(true))
                .build()))
            .build();

        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "attributes", attributes
            )
        );

        CreateCampaign task = CreateCampaign.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .attributes(Property.of(attributes))
            .build();

        CampaignCreateSuccessResponse campaignCreateSuccessResponse = CampaignCreateSuccessResponse.builder()
            .data(
                CampaignCreateSuccessResponse.Data.builder()
                    .id("camp_123")
                    .type(Constants.CAMPAIGN)
                    .attributes(
                        CampaignCreateSuccessResponse.Attributes.builder()
                            .name("September Promo Campaign")
                            .status("draft")
                            .build()
                    )
                    .build()
            )
            .build();

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.CAMPAIGN_URL));

        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class)) {
            HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
            HttpResponse mockResponse = Mockito.mock(HttpResponse.class);

            Mockito.when(mockResponse.getStatus()).thenReturn(HttpResponse.Status.builder().code(201).build());
            Mockito.when(mockResponse.getBody()).thenReturn(campaignCreateSuccessResponse);

            utilsMockedStatic.when(() -> Utils.getMapper()).thenReturn(mockMapper);
            utilsMockedStatic.when(() -> Utils.getHttpRequest(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString()
            )).thenReturn(mockClassicHttpRequest);
            utilsMockedStatic.when(() -> Utils.getHttpClient(runContext)).thenReturn(mockedHttpClient);
            Mockito.when(mockedHttpClient.request(any(HttpRequest.class))).thenReturn(mockResponse);

            CreateCampaign.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.SUCCESS);
            assert output.getStatusCode() == 201;
            assert output.getCampaignCreateSuccessResponse() != null;
            assert output.getCampaignCreateSuccessResponse().getData().getId().equals("camp_123");
            assert output.getErrorResponse() == null;
        }
    }

    @Test
    void run_error() throws Exception {
        CreateCampaign.Attributes attributes = CreateCampaign.Attributes.builder()
            .name(Property.of("September Promo Campaign"))
            .build();

        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "attributes", attributes
            )
        );

        CreateCampaign task = CreateCampaign.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .attributes(Property.of(attributes))
            .build();

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.CAMPAIGN_URL));

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

            CreateCampaign.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.ERROR);
            assert output.getStatusCode() == 401;
            assert output.getCampaignCreateSuccessResponse() == null;
            assert output.getErrorResponse() != null;
            assert output.getErrorResponse().getErrors().getFirst().getTitle().equals("Invalid API Key");
        }
    }
}

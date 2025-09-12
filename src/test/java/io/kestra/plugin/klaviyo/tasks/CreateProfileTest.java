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
import io.kestra.plugin.klaviyo.models.responses.success.ProfileCreateSuccessResponse;
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
class CreateProfileTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run_success() throws Exception {
        CreateProfile.Attributes attributes = CreateProfile.Attributes.builder()
            .email(Property.of("user@example.com"))
            .phoneNumber(Property.of("+1234567890"))
            .firstName(Property.of("John"))
            .lastName(Property.of("Doe"))
            .organization(Property.of("Acme Inc"))
            .title(Property.of("Manager"))
            .location(Property.of(
                CreateProfile.Location.builder()
                    .address1(Property.of("123 Main St"))
                    .address2(Property.of("Suite 100"))
                    .city(Property.of("Metropolis"))
                    .region(Property.of("NY"))
                    .country(Property.of("USA"))
                    .zip(Property.of("12345"))
                    .build()
            ))
            .image(Property.of("https://example.com/image.jpg"))
            .build();

        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "attributes", attributes
            )
        );

        CreateProfile task = CreateProfile.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .attributes(Property.of(attributes))
            .build();

        ProfileCreateSuccessResponse profileCreateSuccessResponse = ProfileCreateSuccessResponse.builder()
            .data(
                ProfileCreateSuccessResponse.Data.builder()
                    .id("profile_123")
                    .type(Constants.PROFILE)
                    .attributes(
                        ProfileCreateSuccessResponse.Attributes.builder()
                            .email("user@example.com")
                            .phone_number("+1234567890")
                            .first_name("John")
                            .last_name("Doe")
                            .organization("Acme Inc")
                            .title("Manager")
                            .location(ProfileCreateSuccessResponse.Location.builder()
                                .address1("123 Main St")
                                .address2("Suite 100")
                                .city("Metropolis")
                                .region("NY")
                                .country("USA")
                                .zip("12345")
                                .build()
                            )
                            .image("https://example.com/image.jpg")
                            .build()
                    )
                    .build()
            )
            .build();

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.PROFILE_URL));

        try (MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic(Utils.class)) {
            HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
            HttpResponse mockResponse = Mockito.mock(HttpResponse.class);

            Mockito.when(mockResponse.getStatus()).thenReturn(HttpResponse.Status.builder().code(201).build());
            Mockito.when(mockResponse.getBody()).thenReturn(profileCreateSuccessResponse);

            utilsMockedStatic.when(() -> Utils.getMapper()).thenReturn(mockMapper);
            utilsMockedStatic.when(() -> Utils.getHttpRequest(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString()
            )).thenReturn(mockClassicHttpRequest);
            utilsMockedStatic.when(() -> Utils.getHttpClient(runContext)).thenReturn(mockedHttpClient);
            Mockito.when(mockedHttpClient.request(any(HttpRequest.class))).thenReturn(mockResponse);

            CreateProfile.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.SUCCESS);
            assert output.getStatusCode() == 201;
            assert output.getProfileCreateSuccessResponse() != null;
            assert output.getProfileCreateSuccessResponse().getData().getId().equals("profile_123");
            assert output.getProfileCreateSuccessResponse().getData().getAttributes().getEmail().equals("user@example.com");
            assert output.getProfileCreateSuccessResponse().getData().getAttributes().getPhone_number().equals("+1234567890");
            assert output.getErrorResponse() == null;
        }
    }

    @Test
    void run_error() throws Exception {
        CreateProfile.Attributes attributes = CreateProfile.Attributes.builder()
            .email(Property.of("user@example.com"))
            .phoneNumber(Property.of("+1234567890"))
            .firstName(Property.of("John"))
            .lastName(Property.of("Doe"))
            .organization(Property.of("Acme Inc"))
            .title(Property.of("Manager"))
            .location(Property.of(CreateProfile.Location.builder()
                .address1(Property.of("123 Main St"))
                .address2(Property.of("Suite 100"))
                .city(Property.of("Metropolis"))
                .region(Property.of("NY"))
                .country(Property.of("USA"))
                .zip(Property.of("12345"))
                .build()
            ))
            .image(Property.of("https://example.com/image.jpg"))
            .build();

        RunContext runContext = runContextFactory.of(
            Map.of(
                "private_api_key", "pk_xxxxx",
                "revision", "2025-07-15",
                "attributes", attributes
            )
        );

        CreateProfile task = CreateProfile.builder()
            .privateApiKey(Property.of("{{ private_api_key }}"))
            .revision(Property.of("{{ revision }}"))
            .attributes(Property.of(attributes))
            .build();

        ObjectMapper mockMapper = new ObjectMapper();
        ClassicHttpRequest mockClassicHttpRequest = Mockito.mock(ClassicHttpRequest.class);
        Mockito.when(mockClassicHttpRequest.getUri()).thenReturn(new java.net.URI(Constants.PROFILE_URL));

        ErrorResponse errorResponse =
            ErrorResponse.builder()
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

            CreateProfile.Output output = task.run(runContext);
            assert output.getStatus().equals(Constants.ERROR);
            assert output.getStatusCode() == 401;
            assert output.getProfileCreateSuccessResponse() == null;
            assert output.getErrorResponse() != null;
            assert output.getErrorResponse().getErrors().getFirst().getTitle().equals("Invalid API Key");
        }
    }
}
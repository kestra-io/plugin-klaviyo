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
import io.kestra.plugin.klaviyo.models.requests.ProfileCreateRequest;
import io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse;
import io.kestra.plugin.klaviyo.models.responses.success.ProfileCreateSuccessResponse;
import io.kestra.plugin.klaviyo.utils.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
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
    title = "Create a Klaviyo profile",
    description = "Creates a new profile in Klaviyo or updates it if it already exists."
)
@Plugin(
    examples = {
        @Example(
            title = "Create a profile with all fields",
            code = {
                "email: \"user@example.com\"",
                "phoneNumber: \"+919999999999\"",
                "externalId: \"cust-001\"",
                "firstName: \"John\"",
                "lastName: \"Doe\"",
                "organization: \"Acme Inc.\"",
                "locale: \"en-US\"",
                "title: \"Engineering Manager\"",
                "image: \"https://cdn.example.com/u/john.jpg\"",
                "location:",
                "  address1: \"123 MG Road\"",
                "  address2: \"Indiranagar\"",
                "  city: \"Bengaluru\"",
                "  country: \"IN\"",
                "  region: \"KA\"",
                "  zip: \"560038\"",
                "  timezone: \"Asia/Kolkata\"",
                "properties:",
                "  plan: \"premium\"",
                "  source: \"website\""
            }
        )
    }
)
public class CreateProfile extends Task implements RunnableTask<CreateProfile.Output> {
    @Schema(
        title = "Klaviyo Private API Key",
        description = "Klaviyo Private API Key"
    )
    private Property<String> privateApiKey;

    @Schema(
        title = "Revision of the profile",
        description = "Revision of the profile"
    )
    private Property<String> revision;

    @Schema(
        title = "Email of the profile",
        description = "Email of the profile"
    )
    private Property<String> email;

    @Schema(
        title = "Phone number of the profile",
        description = "Phone number of the profile"
    )
    private Property<String> phoneNumber;

    @Schema(
        title = "External ID for the profile",
        description = "External ID for the profile"
    )
    private Property<String> externalId;

    @Schema(
        title = "First name of the profile",
        description = "First name of the profile"
    )
    private Property<String> firstName;

    @Schema(
        title = "Last name of the profile",
        description = "Last name of the profile"
    )
    private Property<String> lastName;

    @Schema(
        title = "Organization of the profile",
        description = "Organization of the profile"
    )
    private Property<String> organization;

    @Schema(
        title = "Locale of the profile",
        description = "Locale of the profile"
    )
    private Property<String> locale;

    @Schema(
        title = "Title of the profile",
        description = "Title of the profile"
    )
    private Property<String> title;

    @Schema(
        title = "Image URL for the profile",
        description = "Image URL for the profile"
    )
    private Property<String> image;

    @Schema(
        title = "Location details",
        description = "Location details of the profile."
    )
    private Property<ProfileCreateSuccessResponse.Location> location;

    @Schema(
        title = "Custom properties of the profile",
        description = "Custom properties of the profile"
    )
    private Property<Map<String, Object>> properties;

    @Override
    public CreateProfile.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        String renderedPrivateApiKey = runContext.render(privateApiKey).as(String.class).orElse(null);
        String renderedRevision = runContext.render(revision).as(String.class).orElse("2025-07-15");
        String renderedEmail = runContext.render(email).as(String.class).orElse(null);
        String renderedPhone = runContext.render(phoneNumber).as(String.class).orElse(null);
        String renderedExternalId = runContext.render(externalId).as(String.class).orElse(null);
        String renderedFirstName = runContext.render(firstName).as(String.class).orElse(null);
        String renderedLastName = runContext.render(lastName).as(String.class).orElse(null);
        String renderedOrganization = runContext.render(organization).as(String.class).orElse(null);
        String renderedLocale = runContext.render(locale).as(String.class).orElse(null);
        String renderedTitle = runContext.render(title).as(String.class).orElse(null);
        String renderedImage = runContext.render(image).as(String.class).orElse(null);
        ProfileCreateSuccessResponse.Location renderedLocation = runContext.render(location).as(ProfileCreateSuccessResponse.Location.class).orElse(null);
        Map<String, Object> renderedProperties = runContext.render(properties).asMap(String.class, Object.class);

        logger.info("Creating profile in Klaviyo...");

        // attributes
        ProfileCreateRequest.Attributes attributes = ProfileCreateRequest.Attributes.builder()
            .email(renderedEmail)
            .phone_number(renderedPhone)
            .external_id(renderedExternalId)
            .first_name(renderedFirstName)
            .last_name(renderedLastName)
            .organization(renderedOrganization)
            .locale(renderedLocale)
            .title(renderedTitle)
            .image(renderedImage)
            .location(renderedLocation != null ? ProfileCreateRequest.Location.builder()
                .address1(renderedLocation.getAddress1())
                .address2(renderedLocation.getAddress2())
                .city(renderedLocation.getCity())
                .country(renderedLocation.getCountry())
                .latitude(renderedLocation.getLatitude())
                .longitude(renderedLocation.getLongitude())
                .region(renderedLocation.getRegion())
                .zip(renderedLocation.getZip())
                .timezone(renderedLocation.getTimezone())
                .ip(renderedLocation.getIp())
                .build() : null)
            .properties(renderedProperties != null ? new HashMap<>(renderedProperties) : null)
            .build();

        ProfileCreateRequest profileCreateRequest = ProfileCreateRequest.builder()
                    .data(ProfileCreateRequest.Data.builder()
                        .type(Constants.PROFILE)
                        .attributes(attributes)
                        .build())
                    .build();

        String profileCreateRequestJson = objectMapper.writeValueAsString(profileCreateRequest);

        ClassicHttpRequest request = Utils.getHttpRequest(Constants.PROFILE_URL, profileCreateRequestJson, renderedRevision, renderedPrivateApiKey);
        HttpRequest httpRequest = HttpRequest.from(request);

        // response
        HttpResponse httpResponse = httpClient.request(httpRequest);
        int statusCode = httpResponse.getStatus().getCode();
        if (statusCode!=201) {
            ErrorResponse errorResponse = objectMapper.convertValue(httpResponse.getBody(), ErrorResponse.class);
            List<ErrorResponse.ErrorDetail> errorDetails = errorResponse.getErrors();
            ErrorResponse.ErrorDetail errorDetail = errorDetails.getFirst();
            logger.error("Failed to create/update profile in Klaviyo: {}", objectMapper.writeValueAsString(errorDetails));
            return Output.builder()
                .status(Constants.ERROR)
                .errorMessage(errorDetail.getDetail())
                .errorStatusCode(statusCode)
                .errorTitle(errorDetail.getTitle())
                .errorCode(errorDetail.getCode())
                .build();
        }

        // success response
        ProfileCreateSuccessResponse profileCreateSuccessResponse = objectMapper.convertValue(httpResponse.getBody(), ProfileCreateSuccessResponse.class);
        ProfileCreateSuccessResponse.Data dataNode = profileCreateSuccessResponse.getData();
        if (dataNode != null) {
            ProfileCreateSuccessResponse.Attributes attrs = dataNode.getAttributes();
            if (attrs != null) {
                logger.info("Profile created/updated successfully in Klaviyo with ID: {}", dataNode.getId());
                return Output.builder()
                    .status(Constants.SUCCESS)
                    .profileId(dataNode.getId())
                    .email(attrs.getEmail())
                    .phoneNumber(attrs.getPhone_number())
                    .externalId(attrs.getExternal_id())
                    .firstName(attrs.getFirst_name())
                    .lastName(attrs.getLast_name())
                    .organization(attrs.getOrganization())
                    .locale(attrs.getLocale())
                    .title(attrs.getTitle())
                    .image(attrs.getImage())
                    .created(attrs.getCreated())
                    .updated(attrs.getUpdated())
                    .lastEventDate(attrs.getLast_event_date())
                    .location(attrs.getLocation())
                    .properties(attrs.getProperties())
                    .build();
            }
        }

        logger.error("Failed to create/update profile in Klaviyo. Response: {}", objectMapper.writeValueAsString(httpResponse.getBody()));
        return Output.builder().build();
    }


    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "Status of the profile creation/update operation")
        private final String status;

        @Schema(title = "ID of the created/updated profile")
        private final String profileId;

        @Schema(title = "email of the created/updated profile")
        private final String email;

        @Schema(title = "phone number of the created/updated profile")
        private final String phoneNumber;

        @Schema(title = "external ID of the created/updated profile")
        private final String externalId;

        @Schema(title = "First name of the created/updated profile")
        private final String firstName;

        @Schema(title = "Last name of the created/updated profile")
        private final String lastName;

        @Schema(title = "Organization of the created/updated profile")
        private final String organization;

        @Schema(title = "Locale of the created/updated profile")
        private final String locale;

        @Schema(title = "Title of the created/updated profile")
        private final String title;

        @Schema(title = "Image URL of the created/updated profile")
        private final String image;

        @Schema(title = "Creation date of the created/updated profile")
        private final String created;

        @Schema(title = "Last update date of the created/updated profile")
        private final String updated;

        @Schema(title = "Date of the last event associated with the created/updated profile")
        private final String lastEventDate;

        @Schema(title = "Location details of the created/updated profile")
        private final ProfileCreateSuccessResponse.Location location;

        @Schema(title = "Custom properties of the created/updated profile")
        private final Map<String, Object> properties;

        @Schema(title = "Error message if the profile creation/update failed")
        private final String errorMessage;

        @Schema(title = "HTTP status code if the profile creation/update failed")
        private final int errorStatusCode;

        @Schema(title = "Error title if the profile creation/update failed")
        private final String errorTitle;

        @Schema(title = "Error code if the profile creation/update failed")
        private final String errorCode;
    }
}


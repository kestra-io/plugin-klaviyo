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
                "privateApiKey: \"pk_xxxxx\"",
                "revision: \"2025-07-15\"",
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
        title = "Attributes of the profile",
        description = "Attributes of the profile"
    )
    private Property<Attributes> attributes;

    @SuperBuilder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Attributes of the profile")
    public static class Attributes {
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
        private Property<Location> location;

        @Schema(
            title = "Custom properties of the profile",
            description = "Custom properties of the profile"
        )
        private Property<Map<String, Object>> properties;
    }

    @SuperBuilder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Location details of the profile")
    public static class Location {
        @Schema(
            title = "Address line 1",
            description = "Address line 1 of the profile"
        )
        private Property<String> address1;

        @Schema(
            title = "Address line 2",
            description = "Address line 2 of the profile"
        )
        private Property<String> address2;

        @Schema(
            title = "City",
            description = "City of the profile"
        )
        private Property<String> city;

        @Schema(
            title = "Country",
            description = "Country of the profile"
        )
        private Property<String> country;

        @Schema(
            title = "Latitude",
            description = "Latitude of the profile"
        )
        private Property<String> latitude;

        @Schema(
            title = "Longitude",
            description = "Longitude of the profile"
        )
        private Property<String> longitude;

        @Schema(
            title = "Region",
            description = "Region of the profile"
        )
        private Property<String> region;

        @Schema(
            title = "ZIP code",
            description = "ZIP code of the profile"
        )
        private Property<String> zip;

        @Schema(
            title = "Timezone",
            description = "Timezone of the profile"
        )
        private Property<String> timezone;

        @Schema(
            title = "IP address",
            description = "IP address of the profile"
        )
        private Property<String> ip;
    }

    @Override
    public CreateProfile.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        String renderedPrivateApiKey = runContext.render(privateApiKey).as(String.class).orElse(null);
        String renderedRevision = runContext.render(revision).as(String.class).orElse(Constants.DEFAULT_REVISION);
        Attributes renderedAttributes = runContext.render(attributes).as(Attributes.class).orElse(null);
        Location renderedLocation = renderedAttributes != null && renderedAttributes.getLocation() != null ? runContext.render(renderedAttributes.getLocation()).as(Location.class).orElse(null) : null;
        Map<String, Object> renderedProperties = renderedAttributes != null && renderedAttributes.getProperties() != null ? runContext.render(renderedAttributes.getProperties()).asMap(String.class, Object.class) : null;

        // attributes
        ProfileCreateRequest.Attributes attributes = renderedAttributes != null ? ProfileCreateRequest.Attributes.builder()
            .email(renderedAttributes.getEmail() != null ? renderedAttributes.getEmail().toString() : null)
            .phone_number(renderedAttributes.getPhoneNumber() != null ? renderedAttributes.getPhoneNumber().toString() : null)
            .external_id(renderedAttributes.getExternalId() != null ? renderedAttributes.getExternalId().toString() : null)
            .first_name(renderedAttributes.getFirstName() != null ? renderedAttributes.getFirstName().toString() : null)
            .last_name(renderedAttributes.getLastName() != null ? renderedAttributes.getLastName().toString() : null)
            .organization(renderedAttributes.getOrganization() != null ? renderedAttributes.getOrganization().toString() : null)
            .locale(renderedAttributes.getLocale() != null ? renderedAttributes.getLocale().toString() : null)
            .title(renderedAttributes.getTitle() != null ? renderedAttributes.getTitle().toString() : null)
            .image(renderedAttributes.getImage() != null ? renderedAttributes.getImage().toString() : null)
            .location(renderedLocation != null ? ProfileCreateRequest.Location.builder()
                .address1(renderedLocation.getAddress1() != null ? renderedLocation.getAddress1().toString() : null)
                .address2(renderedLocation.getAddress2() != null ? renderedLocation.getAddress2().toString() : null)
                .city(renderedLocation.getCity() != null ? renderedLocation.getCity().toString() : null)
                .country(renderedLocation.getCountry() != null ? renderedLocation.getCountry().toString() : null)
                .latitude(renderedLocation.getLatitude() != null ? renderedLocation.getLatitude().toString() : null)
                .longitude(renderedLocation.getLongitude() != null ? renderedLocation.getLongitude().toString() : null)
                .region(renderedLocation.getRegion() != null ? renderedLocation.getRegion().toString() : null)
                .zip(renderedLocation.getZip() != null ? renderedLocation.getZip().toString() : null)
                .timezone(renderedLocation.getTimezone() != null ? renderedLocation.getTimezone().toString() : null)
                .ip(renderedLocation.getIp() != null ? renderedLocation.getIp().toString() : null)
                .build() : null)
            .properties(renderedProperties)
            .build() : null;

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
            logger.error("Failed to create/update profile in Klaviyo. Status Code: {}, Error: {}", statusCode, objectMapper.writeValueAsString(errorResponse));
            return Output.builder()
                .status(Constants.ERROR)
                .statusCode(statusCode)
                .errorResponse(errorResponse)
                .build();
        }

        // success response
        ProfileCreateSuccessResponse profileCreateSuccessResponse = objectMapper.convertValue(httpResponse.getBody(), ProfileCreateSuccessResponse.class);
        logger.info("Profile created/updated successfully in Klaviyo with ID: {}", profileCreateSuccessResponse.getData().getId());
        return Output.builder()
            .status(Constants.SUCCESS)
            .statusCode(statusCode)
            .profileCreateSuccessResponse(profileCreateSuccessResponse)
            .build();
    }


    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "Status of the profile creation")
        private final String status;

        @Schema(title = "Status code of the response")
        private final Integer statusCode;

        @Schema(title = "Response of the profile creation - success case")
        private final ProfileCreateSuccessResponse profileCreateSuccessResponse;

        @Schema(title = "Response of the profile creation - error case")
        private final ErrorResponse errorResponse;
    }
}


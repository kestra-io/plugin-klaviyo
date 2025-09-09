package io.kestra.plugin.klaviyo.profile.tasks;

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
import io.kestra.plugin.klaviyo.profile.models.KlaviyoErrorResponse;
import io.kestra.plugin.klaviyo.profile.models.KlaviyoProfileResponse;
import io.kestra.plugin.klaviyo.profile.models.Location;
import io.kestra.plugin.klaviyo.profile.utils.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
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
        title = "Klaviyo Public API Key",
        description = "Klaviyo Public API Key"
    )
    private Property<String> publicApiKey;

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
    private Property<Location> location;

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

        String renderedPublicApiKey = runContext.render(publicApiKey).as(String.class).orElse(null);
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
        Location renderedLocation = runContext.render(location).as(Location.class).orElse(null);
        Map<String, Object> renderedProperties = runContext.render(properties).asMap(String.class, Object.class);

        logger.info("Creating profile in Klaviyo...");

        // attributes
        Map<String, Object> attributes = new HashMap<>();
        if (renderedEmail != null) attributes.put(Constants.EMAIL, renderedEmail);
        if (renderedPhone != null) attributes.put(Constants.PHONE_NUMBER, renderedPhone);
        if (renderedExternalId != null) attributes.put(Constants.EXTERNAL_ID, renderedExternalId);
        if (renderedFirstName != null) attributes.put(Constants.FIRST_NAME, renderedFirstName);
        if (renderedLastName != null) attributes.put(Constants.LAST_NAME, renderedLastName);
        if (renderedOrganization != null) attributes.put(Constants.ORGANIZATION, renderedOrganization);
        if (renderedLocale != null) attributes.put(Constants.LOCALE, renderedLocale);
        if (renderedTitle != null) attributes.put(Constants.TITLE, renderedTitle);
        if (renderedImage != null) attributes.put(Constants.IMAGE, renderedImage);
        if (renderedLocation != null) {
            Map<String, Object> locationMap = getStringObjectMap(renderedLocation);
            attributes.put(Constants.LOCATION, locationMap);
        }
        if (renderedProperties != null && !renderedProperties.isEmpty()) {
            Map<String, Object> propertiesMap = new HashMap<>(renderedProperties);
            attributes.put(Constants.PROPERTIES, propertiesMap);
        }

        // data
        Map<String, Object> data = new HashMap<>();
        data.put(Constants.TYPE, Constants.PROFILE);
        data.put(Constants.ATTRIBUTES, attributes);

        // request body
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put(Constants.DATA, data);

        String requestBodyJson = objectMapper.writeValueAsString(requestBodyMap);

        ClassicHttpRequest request = new HttpPost(Constants.PROFILE_URL);
        request.setEntity(new StringEntity(requestBodyJson, ContentType.APPLICATION_JSON));
        request.addHeader(new BasicHeader("Content-Type", "application/vnd.api+json"));
        request.addHeader(new BasicHeader("Accept", "application/vnd.api+json"));
        request.addHeader(new BasicHeader("Revision", renderedRevision));
        request.addHeader(new BasicHeader("Authorization", "Klaviyo-API-Key " + renderedPrivateApiKey));

        HttpRequest httpRequest = HttpRequest.from(request);

        // response
        HttpResponse httpResponse = httpClient.request(httpRequest);
        Object responseBody = httpResponse.getBody();
        Map<String, Object> responseMap = objectMapper.convertValue(responseBody, Map.class);

        // error response
        if(responseMap.containsKey(Constants.ERRORS)){
            KlaviyoErrorResponse klaviyoErrorResponse = objectMapper.convertValue(responseBody, KlaviyoErrorResponse.class);
            List<KlaviyoErrorResponse.ErrorDetail> errorDetails = klaviyoErrorResponse.getErrors();
            KlaviyoErrorResponse.ErrorDetail errorDetail = errorDetails.get(0);
            logger.error("Failed to create/update profile in Klaviyo: {}", objectMapper.writeValueAsString(errorDetails));
            return Output.builder()
                .status(Constants.ERROR)
                .errorMessage(errorDetail.getDetail())
                .errorStatusCode(errorDetail.getStatus())
                .errorTitle(errorDetail.getTitle())
                .errorCode(errorDetail.getCode())
                .build();
        }

        // success response
        KlaviyoProfileResponse klaviyoProfileResponse = objectMapper.convertValue(responseBody, KlaviyoProfileResponse.class);
        KlaviyoProfileResponse.DataNode dataNode = klaviyoProfileResponse.getData();
        if (dataNode != null) {
            KlaviyoProfileResponse.DataNode.Attributes attrs = dataNode.getAttributes();
            if (attrs != null) {
                logger.info("Profile created/updated successfully in Klaviyo with ID: {}", dataNode.getId());
                return Output.builder()
                    .status("success")
                    .profileId(dataNode.getId())
                    .email(attrs.getEmail())
                    .phoneNumber(attrs.getPhoneNumber())
                    .externalId(attrs.getExternalId())
                    .firstName(attrs.getFirstName())
                    .lastName(attrs.getLastName())
                    .organization(attrs.getOrganization())
                    .locale(attrs.getLocale())
                    .title(attrs.getTitle())
                    .image(attrs.getImage())
                    .created(attrs.getCreated())
                    .updated(attrs.getUpdated())
                    .lastEventDate(attrs.getLastEventDate())
                    .location(attrs.getLocation())
                    .properties(attrs.getProperties())
                    .build();
            }
        }
        logger.error("Failed to create/update profile in Klaviyo. Response: {}", objectMapper.writeValueAsString(responseBody));
        return Output.builder().build();
    }

    private static Map<String, Object> getStringObjectMap(Location renderedLocation) {
        Map<String, Object> locationMap = new java.util.HashMap<>();
        if (renderedLocation.getAddress1() != null) locationMap.put(Constants.ADDRESS1, renderedLocation.getAddress1());
        if (renderedLocation.getAddress2() != null) locationMap.put(Constants.ADDRESS2, renderedLocation.getAddress2());
        if (renderedLocation.getCity() != null) locationMap.put(Constants.CITY, renderedLocation.getCity());
        if (renderedLocation.getCountry() != null) locationMap.put(Constants.COUNTRY, renderedLocation.getCountry());
        if (renderedLocation.getLatitude() != null) locationMap.put(Constants.LATITUDE, renderedLocation.getLatitude());
        if (renderedLocation.getLongitude() != null) locationMap.put(Constants.LONGITUDE, renderedLocation.getLongitude());
        if (renderedLocation.getRegion() != null) locationMap.put(Constants.REGION, renderedLocation.getRegion());
        if (renderedLocation.getZip() != null) locationMap.put(Constants.ZIP, renderedLocation.getZip());
        if (renderedLocation.getTimezone() != null) locationMap.put(Constants.TIMEZONE, renderedLocation.getTimezone());
        if (renderedLocation.getIp() != null) locationMap.put(Constants.IP, renderedLocation.getIp());
        return locationMap;
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
        private final Location location;

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


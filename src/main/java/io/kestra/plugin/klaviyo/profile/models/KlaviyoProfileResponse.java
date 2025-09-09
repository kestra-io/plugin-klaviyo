package io.kestra.plugin.klaviyo.profile.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.kestra.plugin.klaviyo.constants.Constants;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlaviyoProfileResponse {
    private DataNode data;
    private Map<String, Object> links;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataNode {
        private String type;
        private String id;
        private Attributes attributes;
        private Map<String, Object> relationships;
        private Map<String, Object> links;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Attributes {
            @JsonProperty(Constants.EMAIL)
            private String email;
            @JsonProperty(Constants.PHONE_NUMBER)
            private String phoneNumber;
            @JsonProperty(Constants.EXTERNAL_ID)
            private String externalId;
            @JsonProperty(Constants.ANONYMOUS_ID)
            private String anonymousId;
            @JsonProperty(Constants.FIRST_NAME)
            private String firstName;
            @JsonProperty(Constants.LAST_NAME)
            private String lastName;
            private String organization;
            private String locale;
            private String title;
            private String image;
            private String created;
            private String updated;
            @JsonProperty(Constants.LAST_EVENT_DATE)
            private String lastEventDate;
            private Location location;
            private Map<String, Object> properties;
        }
    }
}
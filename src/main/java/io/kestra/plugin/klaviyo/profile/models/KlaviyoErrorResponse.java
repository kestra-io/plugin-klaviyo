package io.kestra.plugin.klaviyo.profile.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlaviyoErrorResponse {
    private List<ErrorDetail> errors;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorDetail {
        private String id;
        private int status;
        private String code;
        private String title;
        private String detail;
        private Source source;
        private Map<String, Object> links;
        private Meta meta;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Source {
            private String pointer;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Meta {
            @JsonProperty("duplicate_profile_id")
            private String duplicateProfileId;
        }
    }
}
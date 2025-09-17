package io.kestra.plugin.klaviyo.models.responses.success;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@SuperBuilder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCreateSuccessResponse {

    private Data data;

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Data {
        private String type;
        private String id;
        private Attributes attributes;
        private Map<String, String> links;
        private Relationships relationships;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Attributes {
        private String name;
        private String status;
        private Boolean archived;
        private Audiences audiences;

        @JsonProperty("send_options")
        private SendOptions sendOptions;

        @JsonProperty("send_strategy")
        private SendStrategy sendStrategy;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("tracking_options")
        private TrackingOptions trackingOptions;

        @JsonProperty("scheduled_at")
        private String scheduledAt;

        @JsonProperty("send_time")
        private String sendTime;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Audiences {
        private List<String> included;
        private List<String> excluded;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class SendOptions {
        @JsonProperty("use_smart_sending")
        private Boolean useSmartSending;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class SendStrategy {
        private String method;
        private String datetime;
        private Options options;

        @Getter
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        @ToString
        public static class Options {
            @JsonProperty("is_local")
            private Boolean isLocal;

            @JsonProperty("send_past_recipients_immediately")
            private Boolean sendPastRecipientsImmediately;
        }
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class TrackingOptions {
        @JsonProperty("add_tracking_params")
        private Boolean addTrackingParams;

        @JsonProperty("custom_tracking_params")
        private List<CustomTrackingParam> customTrackingParams;

        @JsonProperty("is_tracking_clicks")
        private Boolean isTrackingClicks;

        @JsonProperty("is_tracking_opens")
        private Boolean isTrackingOpens;

        @Getter
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        @ToString
        public static class CustomTrackingParam {
            private String type;
            private String value;
            private String name;
        }
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Relationships {
        @JsonProperty("campaign-messages")
        private RelationshipData campaignMessages;
        private RelationshipData tags;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class RelationshipData {
        private List<RelatedData> data;
        private Map<String, String> links;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class RelatedData {
        private String type;
        private String id;
    }
}

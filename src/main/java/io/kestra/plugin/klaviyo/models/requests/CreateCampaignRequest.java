package io.kestra.plugin.klaviyo.models.requests;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.List;

@SuperBuilder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampaignRequest {

    private Data data;

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Data {
        private String type;
        private Attributes attributes;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Attributes {
        private String name;
        private Audiences audiences;
        private List<CampaignMessageData> campaignMessages;
        private SendStrategy send_strategy;
        private SendOptions send_options;
        private TrackingOptions tracking_options;
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
    public static class CampaignMessageData {
        private String type; // "campaign-message"
        private CampaignMessageAttributes attributes;
        private CampaignMessageRelationships relationships;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class CampaignMessageAttributes {
        private Definition definition;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Definition {
        private String channel;
        private String label;
        private Content content;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Content {
        private String subject;
        private String preview_text;
        private String from_email;
        private String from_label;
        private String reply_to_email;
        private String cc_email;
        private String bcc_email;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class CampaignMessageRelationships {
        private ImageRelationship image;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ImageRelationship {
        private ImageData data;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ImageData {
        private String type; // "image"
        private String id;
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
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Options {
        private Boolean is_local;
        private Boolean send_past_recipients_immediately;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class SendOptions {
        private Boolean use_smart_sending;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class TrackingOptions {
        private Boolean add_tracking_params;
        private List<CustomTrackingParam> custom_tracking_params;
        private Boolean is_tracking_clicks;
        private Boolean is_tracking_opens;
    }

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

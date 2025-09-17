package io.kestra.plugin.klaviyo.models.responses.success;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileCreateSuccessResponse {
    private Data data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        private String id;
        private String type;
        private Attributes attributes;
        private Map<String, String> links;
        private Relationships relationships;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attributes {
        private String email;
        private String phone_number;
        private String external_id;
        private String first_name;
        private String last_name;
        private String organization;
        private String locale;
        private String title;
        private String image;
        private String created;
        private String updated;
        private String last_event_date;
        private Location location;
        private Map<String, Object> properties;
        private Subscriptions subscriptions;
        private PredictiveAnalytics predictive_analytics;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Location {
        private String address1;
        private String address2;
        private String city;
        private String country;
        private String latitude;
        private String longitude;
        private String region;
        private String zip;
        private String timezone;
        private String ip;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Subscriptions {
        private Channel email;
        private Sms sms;
        private MobilePush mobile_push;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Channel {
        private Marketing marketing;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Marketing {
        private Boolean can_receive_email_marketing;
        private String consent;
        private String consent_timestamp;
        private String last_updated;
        private String method;
        private String method_detail;
        private String custom_method_detail;
        private Boolean double_optin;
        private List<Suppression> suppression;
        private List<ListSuppression> list_suppressions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Suppression {
        private String reason;
        private String timestamp;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListSuppression {
        private String list_id;
        private String reason;
        private String timestamp;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Sms {
        private Marketing marketing;
        private Transactional transactional;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Transactional {
        private Boolean can_receive_sms_transactional;
        private String consent;
        private String consent_timestamp;
        private String method;
        private String method_detail;
        private String last_updated;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MobilePush {
        private Marketing marketing;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PredictiveAnalytics {
        private Double historic_clv;
        private Double predicted_clv;
        private Double total_clv;
        private Double historic_number_of_orders;
        private Double predicted_number_of_orders;
        private Double average_days_between_orders;
        private Double average_order_value;
        private Double churn_probability;
        private String expected_date_of_next_order;
        private List<String> ranked_channel_affinity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Relationships {
        private Related lists;
        private Related segments;
        private Related push_tokens;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Related {
        private List<RelatedData> data;
        private Map<String, String> links;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RelatedData {
        private String type;
        private String id;
    }
}

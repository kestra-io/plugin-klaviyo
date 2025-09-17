package io.kestra.plugin.klaviyo.models.requests;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCreateRequest {

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
        private String email;
        private String phone_number;
        private String external_id;
        private String first_name;
        private String last_name;
        private String organization;
        private String locale;
        private String title;
        private String image;
        private Location location;
        private Map<String, Object> properties;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
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
}

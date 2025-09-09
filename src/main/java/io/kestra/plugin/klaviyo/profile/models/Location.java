package io.kestra.plugin.klaviyo.profile.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location implements Serializable {
    private static final long serialVersionUID = 1L;
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

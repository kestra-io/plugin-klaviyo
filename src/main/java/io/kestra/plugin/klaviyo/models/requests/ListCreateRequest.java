package io.kestra.plugin.klaviyo.models.requests;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ListCreateRequest {

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
    }
}

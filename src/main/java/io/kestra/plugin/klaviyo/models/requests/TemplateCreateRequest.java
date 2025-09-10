package io.kestra.plugin.klaviyo.models.requests;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreateRequest {

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
        private String editor_type;
        private String html;
        private String text;
        private String amp;
    }
}


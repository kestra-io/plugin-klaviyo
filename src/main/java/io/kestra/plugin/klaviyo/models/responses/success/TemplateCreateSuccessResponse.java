package io.kestra.plugin.klaviyo.models.responses.success;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreateSuccessResponse {

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
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Attributes {
        private String name;

        @JsonProperty("editor_type")
        private String editorType;

        private String html;
        private String text;
        private String amp;

        private String created;
        private String updated;
    }
}


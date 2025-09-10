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
public class ListCreateSuccessResponse {

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
        private String created;
        private String updated;

        @JsonProperty("opt_in_process")
        private String optInProcess;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Relationships {
        private RelationshipData profiles;
        private RelationshipData tags;

        @JsonProperty("flow-triggers")
        private RelationshipData flowTriggers;
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

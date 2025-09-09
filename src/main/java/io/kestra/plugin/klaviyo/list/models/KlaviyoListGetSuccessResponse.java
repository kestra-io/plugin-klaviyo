package io.kestra.plugin.klaviyo.list.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KlaviyoListGetSuccessResponse {
    private List<DataNode> data;
    private Links links;
    private List<Included> included;

    @Data
    public static class DataNode {
        private String type;
        private String id;
        private Attributes attributes;
        private Links links;
        private Relationships relationships;
    }

    @Data
    public static class Attributes {
        private String name;
        private String created;
        private String updated;

        @JsonProperty("opt_in_process")
        private String optInProcess;
    }

    @Data
    public static class Links {
        private String self;
        private String first;
        private String last;
        private String prev;
        private String next;
    }

    @Data
    public static class Relationships {
        private RelationshipData profiles;
        private RelationshipData tags;

        @JsonProperty("flow-triggers")
        private RelationshipData flowTriggers;
    }

    @Data
    public static class RelationshipData {
        private List<RelationItem> data;
        private Links links;
    }

    @Data
    public static class RelationItem {
        private String type;
        private String id;
    }

    @Data
    public static class Included {
        private String type;
        private String id;
        private FlowAttributes attributes;
        private Links links;
    }

    @Data
    public static class FlowAttributes {
        private String name;
        private String status;
        private Boolean archived;
        private String created;
        private String updated;

        @JsonProperty("trigger_type")
        private String triggerType;
    }
}


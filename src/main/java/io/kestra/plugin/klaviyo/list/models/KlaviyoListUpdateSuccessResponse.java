package io.kestra.plugin.klaviyo.list.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KlaviyoListUpdateSuccessResponse {
    private DataNode data;

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
    }

    @Data
    public static class Relationships {
        private Profiles profiles;
        private Tags tags;
        private FlowTriggers flowTriggers;
    }

    @Data
    public static class Profiles {
        private List<ProfileData> data;
        private Links links;
    }

    @Data
    public static class ProfileData {
        private String type;
        private String id;
    }

    @Data
    public static class Tags {
        private List<TagData> data;
        private Links links;
    }

    @Data
    public static class TagData {
        private String type;
        private String id;
    }

    @Data
    public static class FlowTriggers {
        private List<FlowData> data;
        private Links links;
    }

    @Data
    public static class FlowData {
        private String type;
        private String id;
    }
}

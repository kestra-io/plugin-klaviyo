package io.kestra.plugin.klaviyo.list.models;

import java.util.List;
import lombok.Data;

@Data
public class KlaviyoListUpdateErrorResponse {
    private List<ErrorDetail> errors;

    @Data
    public static class ErrorDetail {
        private String id;
        private String code;
        private String title;
        private String detail;
        private Source source;
    }

    @Data
    public static class Source {
        private String pointer;
        private String parameter;
    }
}

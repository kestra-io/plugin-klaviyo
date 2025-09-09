package io.kestra.plugin.klaviyo.list.models;

import lombok.Data;
import java.util.List;

@Data
public class KlaviyoListCreateErrorResponse {
    private List<ErrorDetail> errors;

    @Data
    public static class ErrorDetail {
        private String id;
        private String code;
        private String title;
        private String detail;
        private ErrorSource source;
    }

    @Data
    public static class ErrorSource {
        private String pointer;
        private String parameter;
    }
}

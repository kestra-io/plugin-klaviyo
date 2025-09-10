package io.kestra.plugin.klaviyo.models.responses.error;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private List<ErrorDetail> errors;

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ErrorDetail {
        private String id;
        private String code;
        private String title;
        private String detail;
        private Source source;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Source {
        private String pointer;
        private String parameter;
    }
}


package io.kestra.plugin.klaviyo.campaign;

import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.models.tasks.common.FetchType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class AbstractCampaignTask extends Task {

    private static final String API_VERSION = "2025-10-15";

    @Schema(title = "Klaviyo private API Key", description = "The API key for authenticating with Klaviyo.")
    @NotNull
    protected Property<String> apiKey;

    @Schema(title = "Base URL", description = "The base URL for the Klaviyo API")
    @Builder.Default
    protected Property<String> baseUrl = Property.ofValue("https://a.klaviyo.com/api");

    @Schema(title = "The way you want to store the data", description = "FETCH_ONE output the first row, "
        + "FETCH output all rows, "
        + "STORE store all rows in a file, "
        + "NONE do nothing")
    @Builder.Default
    protected Property<FetchType> fetchType = Property.ofValue(FetchType.FETCH);

    protected String getApiVersion() {
        return API_VERSION;
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "The size of the rows fetched")
        private final Long size;

        @Schema(title = "The row data")
        private final Map<String, Object> row;

        @Schema(title = "The rows data")
        private final List<Map<String, Object>> rows;

        @Schema(title = "The URI of the stored data")
        private final URI uri;
    }
}

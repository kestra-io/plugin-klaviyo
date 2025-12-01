package io.kestra.plugin.klaviyo;

import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.models.tasks.common.FetchType;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.FileSerde;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.awaitility.Awaitility.await;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class AbstractKlaviyoTask extends Task {

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

    protected Output applyFetchStrategy(FetchType rFetchType, List<Map<String, Object>> data, RunContext runContext) throws IOException {

        Output.OutputBuilder output = Output.builder();

        switch (rFetchType) {
            case FETCH_ONE -> {
                Map<String, Object> result = data.isEmpty() ? null : data.getFirst();
                output.row(result);
            }
            case STORE -> {
                File tempFile = runContext.workingDir().createTempFile(".ion").toFile();
                try (OutputStream fileOutputStream = new BufferedOutputStream(
                    new FileOutputStream(tempFile), FileSerde.BUFFER_SIZE)) {
                    for (Map<String, Object> campaign : data) {
                        FileSerde.write(fileOutputStream, campaign);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                output.uri(runContext.storage().putFile(tempFile));
            }
            case FETCH -> output.rows(data);
            case NONE -> {
            }
        }

        return output.build();
    }

    protected void induceDelay() {
        await().pollDelay(Duration.ofSeconds(1)).until(() -> true);
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

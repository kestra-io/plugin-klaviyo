package io.kestra.plugin.klaviyo.campaign.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.common.FetchType;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.FileSerde;
import io.kestra.core.serializers.JacksonMapper;
import io.kestra.plugin.klaviyo.campaign.AbstractCampaignTask;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuperBuilder
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
@Schema(
    title = "Retrieve campaign send jobs from Klaviyo",
    description = "Get campaign send jobs by their IDs"
)
@Plugin(
    examples = {
        @Example(
            title = "Get a single campaign send job",
            full = true,
            code = """
                id: klaviyo_get_send_job
                namespace: company.team

                tasks:
                  - id: get_send_job
                    type: io.kestra.plugin.klaviyo.campaign.jobs.GetSendJob
                    apiKey: "{{ secret('KLAVIYO_API_KEY') }}"
                    jobIds:
                      - "job_id_1"
                    fetchType: FETCH_ONE
                """
        ),
        @Example(
            title = "Get multiple campaign send jobs",
            code = """
                - id: get_send_jobs
                  type: io.kestra.plugin.klaviyo.campaign.jobs.GetSendJob
                  apiKey: "{{ secret('KLAVIYO_API_KEY') }}"
                  jobIds:
                    - "job_id_1"
                    - "job_id_2"
                  fetchType: FETCH
                """
        )
    }
)
public class GetSendJob extends AbstractCampaignTask implements RunnableTask<AbstractCampaignTask.Output> {

    @Schema(title = "List of send job IDs", description = "Campaign send job IDs to retrieve")
    @NotNull
    protected Property<List<String>> jobIds;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();

        String rApiKey = runContext.render(this.apiKey).as(String.class).orElseThrow();
        String rBaseUrl = runContext.render(this.baseUrl).as(String.class).orElseThrow();
        List<String> rJobIds = runContext.render(this.jobIds).asList(String.class);
        FetchType rFetchType = runContext.render(this.fetchType).as(FetchType.class).orElse(FetchType.FETCH);

        Output.OutputBuilder output = Output.builder();
        long size = 0L;
        List<Map<String, Object>> allJobs = new ArrayList<>();

        try (HttpClient httpClient = HttpClient.builder()
            .runContext(runContext)
            .build()) {

            for (String jobId : rJobIds) {
                String url = rBaseUrl + "/campaign-send-jobs/" + jobId;

                HttpRequest request = HttpRequest.builder()
                    .uri(URI.create(url))
                    .method("GET")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/vnd.api+json")
                    .addHeader("Authorization", "Klaviyo-API-Key " + rApiKey)
                    .addHeader("revision", getApiVersion())
                    .build();

                HttpResponse<String> response = httpClient.request(request, String.class);

                if (response.getStatus().getCode() != 200) {
                    throw new RuntimeException(
                        "Failed to retrieve send job " + jobId + ": " +
                            response.getStatus().getCode() + " - " + response.getBody());
                }

                JsonNode responseJson = JacksonMapper.ofJson().readTree(response.getBody());
                JsonNode dataNode = responseJson.get("data");

                if (dataNode != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> job = JacksonMapper.ofJson().convertValue(dataNode, Map.class);
                    allJobs.add(job);
                    size++;
                }
            }

            switch (rFetchType) {
                case FETCH_ONE -> {
                    Map<String, Object> result = allJobs.isEmpty() ? null : allJobs.getFirst();
                    output.row(result);
                }
                case STORE -> {
                    File tempFile = runContext.workingDir().createTempFile(".ion").toFile();
                    try (OutputStream fileOutputStream = new BufferedOutputStream(
                        new FileOutputStream(tempFile), FileSerde.BUFFER_SIZE)) {
                        for (Map<String, Object> job : allJobs) {
                            FileSerde.write(fileOutputStream, job);
                        }
                    }
                    output.uri(runContext.storage().putFile(tempFile));
                }
                case FETCH -> output.rows(allJobs);
                case NONE -> {
                }
            }

            output.size(size);
            logger.info("Successfully retrieved {} send job(s)", size);

            return output.build();
        }
    }
}

package io.kestra.plugin.klaviyo.jobs;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.common.FetchType;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.klaviyo.campaign.AbstractCampaignTest;
import io.kestra.plugin.klaviyo.jobs.GetSendJob;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class GetSendJobTest extends AbstractCampaignTest {

    @Test
    void testFetchOne() throws Exception {
        RunContext runContext = runContextFactory.of();
        Logger logger = runContext.logger();

        GetSendJob task = GetSendJob.builder()
            .apiKey(Property.ofValue("test-api-key"))
            .baseUrl(Property.ofValue(server.getURI() + "/api"))
            .jobIds(Property.ofValue(List.of("job-001")))
            .fetchType(Property.ofValue(FetchType.FETCH_ONE))
            .build();

        GetSendJob.Output output = task.run(runContext);

        assertThat(output.getSize(), is(1L));
        assertThat(output.getRow(), is(notNullValue()));
        assertThat(output.getRows(), is(nullValue()));

        Map<String, Object> job = output.getRow();
        assertThat(job.get("type"), is("campaign-send-job"));
        assertThat(job.get("id"), is("job-001"));

        Map<String, Object> attributes = (Map<String, Object>) job.get("attributes");
        assertThat(attributes.get("status"), is("complete"));

        logger.info("Successfully retrieved send job: {}", job);
    }

    @Test
    void testFetchMultiple() throws Exception {
        RunContext runContext = runContextFactory.of();
        Logger logger = runContext.logger();

        GetSendJob task = GetSendJob.builder()
            .apiKey(Property.ofValue("test-api-key"))
            .baseUrl(Property.ofValue(server.getURI() + "/api"))
            .jobIds(Property.ofValue(List.of("job-001", "job-002")))
            .fetchType(Property.ofValue(FetchType.FETCH))
            .build();

        GetSendJob.Output output = task.run(runContext);

        assertThat(output.getSize(), is(2L));
        assertThat(output.getRows(), is(notNullValue()));
        assertThat(output.getRows().size(), is(2));

        for (Map<String, Object> job : output.getRows()) {
            assertThat(job.get("type"), is("campaign-send-job"));
            logger.info("Retrieved send job: {}", job.get("id"));
        }
    }
}

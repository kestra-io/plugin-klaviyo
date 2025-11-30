package io.kestra.plugin.klaviyo.campaign;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.common.FetchType;
import io.kestra.core.runners.RunContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class GetTest extends AbstractCampaignTest {
    @Test
    void testFetchOne() throws Exception {
        RunContext runContext = runContextFactory.of();
        Logger logger = runContext.logger();

        Get task = Get.builder()
            .apiKey(Property.ofValue("test-api-key"))
            .baseUrl(Property.ofValue(server.getURI() + "/api"))
            .campaignIds(Property.ofValue(List.of("campaign-001")))
            .fetchType(Property.ofValue(FetchType.FETCH_ONE))
            .build();

        Get.Output output = task.run(runContext);

        assertThat(output.getSize(), is(1L));
        assertThat(output.getRow(), is(notNullValue()));
        assertThat(output.getRows(), is(nullValue()));
        assertThat(output.getUri(), is(nullValue()));

        Map<String, Object> campaign = output.getRow();
        assertThat(campaign.get("type"), is("campaign"));
        assertThat(campaign.get("id"), is("campaign-001"));

        logger.info("Successfully retrieved campaign: {}", campaign);
    }

    @Test
    void testFetchMultiple() throws Exception {
        RunContext runContext = runContextFactory.of();
        Logger logger = runContext.logger();

        Get task = Get.builder()
            .apiKey(Property.ofValue("test-api-key"))
            .baseUrl(Property.ofValue(server.getURI() + "/api"))
            .campaignIds(Property.ofValue(List.of("campaign-001", "campaign-002", "campaign-003")))
            .fetchType(Property.ofValue(FetchType.FETCH))
            .build();

        Get.Output output = task.run(runContext);

        assertThat(output.getSize(), is(3L));
        assertThat(output.getRows(), is(notNullValue()));
        assertThat(output.getRows().size(), is(3));
        assertThat(output.getRow(), is(nullValue()));
        assertThat(output.getUri(), is(nullValue()));

        for (Map<String, Object> campaign : output.getRows()) {
            assertThat(campaign.get("type"), is("campaign"));
            assertThat(campaign.get("id"), is(notNullValue()));
            logger.info("Retrieved campaign: {}", campaign.get("id"));
        }
    }
}

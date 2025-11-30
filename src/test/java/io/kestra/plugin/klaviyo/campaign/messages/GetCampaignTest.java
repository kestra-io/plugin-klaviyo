package io.kestra.plugin.klaviyo.campaign.messages;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.common.FetchType;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.klaviyo.campaign.AbstractCampaignTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class GetCampaignTest extends AbstractCampaignTest {

    @Test
    void testFetchOne() throws Exception {
        RunContext runContext = runContextFactory.of();
        Logger logger = runContext.logger();

        GetCampaign task = GetCampaign.builder()
            .apiKey(Property.ofValue("test-api-key"))
            .baseUrl(Property.ofValue(server.getURI() + "/api"))
            .messageIds(Property.ofValue(List.of("msg-001")))
            .fetchType(Property.ofValue(FetchType.FETCH_ONE))
            .build();

        GetCampaign.Output output = task.run(runContext);

        assertThat(output.getSize(), is(1L));
        assertThat(output.getRow(), is(notNullValue()));
        assertThat(output.getRows(), is(nullValue()));

        Map<String, Object> campaign = output.getRow();
        assertThat(campaign.get("type"), is("campaign"));
        assertThat(campaign.get("id"), is("campaign_msg-001"));

        logger.info("Successfully retrieved campaign for message: {}", campaign);
    }

    @Test
    void testFetchMultiple() throws Exception {
        RunContext runContext = runContextFactory.of();
        Logger logger = runContext.logger();

        GetCampaign task = GetCampaign.builder()
            .apiKey(Property.ofValue("test-api-key"))
            .baseUrl(Property.ofValue(server.getURI() + "/api"))
            .messageIds(Property.ofValue(List.of("msg-001", "msg-002")))
            .fetchType(Property.ofValue(FetchType.FETCH))
            .build();

        GetCampaign.Output output = task.run(runContext);

        assertThat(output.getSize(), is(2L));
        assertThat(output.getRows(), is(notNullValue()));
        assertThat(output.getRows().size(), is(2));

        for (Map<String, Object> campaign : output.getRows()) {
            assertThat(campaign.get("type"), is("campaign"));
            logger.info("Retrieved campaign: {}", campaign.get("id"));
        }
    }
}

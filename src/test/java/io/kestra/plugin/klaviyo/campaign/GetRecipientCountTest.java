package io.kestra.plugin.klaviyo.campaign;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.common.FetchType;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.klaviyo.AbstractKlaviyoTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class GetRecipientCountTest extends AbstractKlaviyoTest {

    @Test
    void testFetchOne() throws Exception {
        RunContext runContext = runContextFactory.of();
        Logger logger = runContext.logger();

        GetRecipientCount task = GetRecipientCount.builder()
            .apiKey(Property.ofValue("test-api-key"))
            .baseUrl(Property.ofValue(server.getURI() + "/api"))
            .campaignIds(Property.ofValue(List.of("campaign-001")))
            .fetchType(Property.ofValue(FetchType.FETCH_ONE))
            .build();

        GetRecipientCount.Output output = task.run(runContext);

        assertThat(output.getSize(), is(1L));
        assertThat(output.getRow(), is(notNullValue()));
        assertThat(output.getRows(), is(nullValue()));

        Map<String, Object> estimation = output.getRow();
        assertThat(estimation.get("type"), is("campaign-recipient-estimation"));
        assertThat(estimation.get("id"), is("campaign-001"));

        Map<String, Object> attributes = (Map<String, Object>) estimation.get("attributes");
        assertThat(attributes.get("estimated_recipient_count"), is(1000));

        logger.info("Successfully retrieved recipient estimation: {}", estimation);
    }

    @Test
    void testFetchMultiple() throws Exception {
        RunContext runContext = runContextFactory.of();
        Logger logger = runContext.logger();

        GetRecipientCount task = GetRecipientCount.builder()
            .apiKey(Property.ofValue("test-api-key"))
            .baseUrl(Property.ofValue(server.getURI() + "/api"))
            .campaignIds(Property.ofValue(List.of("campaign-001", "campaign-002")))
            .fetchType(Property.ofValue(FetchType.FETCH))
            .build();

        GetRecipientCount.Output output = task.run(runContext);

        assertThat(output.getSize(), is(2L));
        assertThat(output.getRows(), is(notNullValue()));
        assertThat(output.getRows().size(), is(2));

        for (Map<String, Object> estimation : output.getRows()) {
            assertThat(estimation.get("type"), is("campaign-recipient-estimation"));
            logger.info("Retrieved estimation for campaign: {}", estimation.get("id"));
        }
    }
}

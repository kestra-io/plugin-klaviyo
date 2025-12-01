package io.kestra.plugin.klaviyo.campaign.messages;

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
class GetImagesTest extends AbstractKlaviyoTest {

    @Test
    void testFetchOne() throws Exception {
        RunContext runContext = runContextFactory.of();
        Logger logger = runContext.logger();

        GetImages task = GetImages.builder()
            .apiKey(Property.ofValue("test-api-key"))
            .baseUrl(Property.ofValue(server.getURI() + "/api"))
            .messageIds(Property.ofValue(List.of("msg-001")))
            .fetchType(Property.ofValue(FetchType.FETCH_ONE))
            .build();

        GetImages.Output output = task.run(runContext);

        assertThat(output.getSize(), is(1L));
        assertThat(output.getRow(), is(notNullValue()));
        assertThat(output.getRows(), is(nullValue()));

        Map<String, Object> image = output.getRow();
        assertThat(image.get("type"), is("image"));
        assertThat(image.get("id"), is("image_msg-001"));

        Map<String, Object> attributes = (Map<String, Object>) image.get("attributes");
        assertThat(attributes.get("name"), is("Test Image"));
        assertThat(attributes.get("format"), is("png"));

        logger.info("Successfully retrieved image for message: {}", image);
    }

    @Test
    void testFetchMultiple() throws Exception {
        RunContext runContext = runContextFactory.of();
        Logger logger = runContext.logger();

        GetImages task = GetImages.builder()
            .apiKey(Property.ofValue("test-api-key"))
            .baseUrl(Property.ofValue(server.getURI() + "/api"))
            .messageIds(Property.ofValue(List.of("msg-001", "msg-002")))
            .fetchType(Property.ofValue(FetchType.FETCH))
            .build();

        GetImages.Output output = task.run(runContext);

        assertThat(output.getSize(), is(2L));
        assertThat(output.getRows(), is(notNullValue()));
        assertThat(output.getRows().size(), is(2));

        for (Map<String, Object> image : output.getRows()) {
            assertThat(image.get("type"), is("image"));
            logger.info("Retrieved image: {}", image.get("id"));
        }
    }
}

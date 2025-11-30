package io.kestra.plugin.klaviyo.campaign;

import io.kestra.core.runners.RunContextFactory;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractCampaignTest {
    @Inject
    protected RunContextFactory runContextFactory;

    @Inject
    protected EmbeddedServer server;

    @BeforeEach
    void setUp() {
        if (!server.isRunning()) {
            server.start();
        }
    }

}

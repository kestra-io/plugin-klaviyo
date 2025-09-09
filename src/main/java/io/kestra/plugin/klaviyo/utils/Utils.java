package io.kestra.plugin.klaviyo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.runners.RunContext;

public class Utils {
    private static volatile ObjectMapper objectMapper;
    private static volatile HttpClient httpClient;
    private Utils() {

    }

    public static ObjectMapper getMapper() {
        if (objectMapper == null) {
            synchronized (io.kestra.plugin.klaviyo.profile.utils.Utils.class) {
                if (objectMapper == null) {
                    objectMapper = new ObjectMapper();
                }
            }
        }
        return objectMapper;
    }

    public static HttpClient getHttpClient(RunContext runContext) {
        if (httpClient == null) {
            synchronized (io.kestra.plugin.klaviyo.profile.utils.Utils.class) {
                if (httpClient == null) {
                    try {
                        httpClient = HttpClient.builder().runContext(runContext).build();
                    } catch (io.kestra.core.exceptions.IllegalVariableEvaluationException e) {
                        throw new RuntimeException("Failed to build HttpClient", e);
                    }
                }
            }
        }
        return httpClient;
    }
}
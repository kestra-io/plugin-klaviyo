package io.kestra.plugin.klaviyo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.klaviyo.constants.Constants;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;

public class Utils {
    private static volatile ObjectMapper objectMapper;
    private static volatile HttpClient httpClient;
    private Utils() {

    }

    public static ObjectMapper getMapper() {
        if (objectMapper == null) {
            synchronized (io.kestra.plugin.klaviyo.utils.Utils.class) {
                if (objectMapper == null) {
                    objectMapper = new ObjectMapper();
                }
            }
        }
        return objectMapper;
    }

    public static HttpClient getHttpClient(RunContext runContext) {
        if (httpClient == null) {
            synchronized (io.kestra.plugin.klaviyo.utils.Utils.class) {
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

    public static ClassicHttpRequest getHttpRequest(String url, String requestBodyJson, String renderedRevision, String renderedPrivateApiKey) {
        ClassicHttpRequest request = new HttpPost(url);
        request.setEntity(new StringEntity(requestBodyJson, ContentType.APPLICATION_JSON));
        request.addHeader(new BasicHeader("Content-Type", "application/vnd.api+json"));
        request.addHeader(new BasicHeader("Accept", "application/vnd.api+json"));
        request.addHeader(new BasicHeader("Revision", renderedRevision));
        request.addHeader(new BasicHeader("Authorization", "Klaviyo-API-Key " + renderedPrivateApiKey));
        return request;
    }
}
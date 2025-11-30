package io.kestra.plugin.klaviyo.campaign;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;

@Controller("/api")
public class MockKlaviyoServer {

    @Get(value = "/campaigns/{id}", produces = "application/vnd.api+json")
    public HttpResponse<String> getCampaign(String id, @Header("Authorization") String auth) {
        if (!auth.startsWith("Klaviyo-API-Key")) {
            return HttpResponse.unauthorized();
        }

        String response = String.format("""
            {
              "data": {
                "type": "campaign",
                "id": "%s",
                "attributes": {
                  "name": "Test Campaign",
                  "status": "Draft",
                  "archived": false
                }
              }
            }
            """, id);
        return HttpResponse.ok(response);
    }

    @Get(value = "/campaign-recipient-estimations/{id}", produces = "application/vnd.api+json")
    public HttpResponse<String> getRecipientEstimation(String id, @Header("Authorization") String auth) {
        if (!auth.startsWith("Klaviyo-API-Key")) {
            return HttpResponse.unauthorized();
        }

        String response = String.format("""
            {
              "data": {
                "type": "campaign-recipient-estimation",
                "id": "%s",
                "attributes": {
                  "estimated_recipient_count": 1000
                }
              }
            }
            """, id);
        return HttpResponse.ok(response);
    }

    @Get(value = "/campaign-send-jobs/{id}", produces = "application/vnd.api+json")
    public HttpResponse<String> getSendJob(String id, @Header("Authorization") String auth) {
        if (!auth.startsWith("Klaviyo-API-Key")) {
            return HttpResponse.unauthorized();
        }

        String response = String.format("""
            {
              "data": {
                "type": "campaign-send-job",
                "id": "%s",
                "attributes": {
                  "status": "complete"
                }
              }
            }
            """, id);
        return HttpResponse.ok(response);
    }

    @Get(value = "/campaign-recipient-estimation-jobs/{id}", produces = "application/vnd.api+json")
    public HttpResponse<String> getRecipientJob(String id, @Header("Authorization") String auth) {
        if (!auth.startsWith("Klaviyo-API-Key")) {
            return HttpResponse.unauthorized();
        }

        String response = String.format("""
            {
              "data": {
                "type": "campaign-recipient-estimation-job",
                "id": "%s",
                "attributes": {
                  "status": "complete"
                }
              }
            }
            """, id);
        return HttpResponse.ok(response);
    }

    @Get(value = "/campaign-messages/{id}", produces = "application/vnd.api+json")
    public HttpResponse<String> getMessage(String id, @Header("Authorization") String auth) {
        if (!auth.startsWith("Klaviyo-API-Key")) {
            return HttpResponse.unauthorized();
        }

        String response = String.format("""
            {
              "data": {
                "type": "campaign-message",
                "id": "%s",
                "attributes": {
                  "channel": "email",
                  "label": "Test Message"
                }
              }
            }
            """, id);
        return HttpResponse.ok(response);
    }

    @Get(value = "/campaign-messages/{id}/campaign", produces = "application/vnd.api+json")
    public HttpResponse<String> getCampaignForMessage(String id, @Header("Authorization") String auth) {
        if (!auth.startsWith("Klaviyo-API-Key")) {
            return HttpResponse.unauthorized();
        }

        String response = String.format("""
            {
              "data": {
                "type": "campaign",
                "id": "campaign_%s",
                "attributes": {
                  "name": "Campaign for Message %s",
                  "status": "Draft"
                }
              }
            }
            """, id, id);
        return HttpResponse.ok(response);
    }

    @Get(value = "/campaign-messages/{id}/image", produces = "application/vnd.api+json")
    public HttpResponse<String> getImageForMessage(String id, @Header("Authorization") String auth) {
        if (!auth.startsWith("Klaviyo-API-Key")) {
            return HttpResponse.unauthorized();
        }

        String response = String.format("""
            {
              "data": {
                "type": "image",
                "id": "image_%s",
                "attributes": {
                  "name": "Test Image",
                  "image_url": "https://example.com/image.png",
                  "format": "png",
                  "size": 12345
                }
              }
            }
            """, id);
        return HttpResponse.ok(response);
    }
}
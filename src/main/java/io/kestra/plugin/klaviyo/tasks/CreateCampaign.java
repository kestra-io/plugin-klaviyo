package io.kestra.plugin.klaviyo.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.klaviyo.constants.Constants;
import io.kestra.plugin.klaviyo.models.requests.CreateCampaignRequest;
import io.kestra.plugin.klaviyo.models.responses.error.ErrorResponse;
import io.kestra.plugin.klaviyo.models.responses.success.CampaignCreateSuccessResponse;
import io.kestra.plugin.klaviyo.utils.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Create a Klaviyo campaign",
    description = "Creates a new campaign in Klaviyo with message, audiences, and tracking options."
)
@Plugin(
    examples = {
        @Example(
            title = "Create a simple campaign",
            code = {
                "privateApiKey: \"<KLAVIYO_API_KEY>\"",
                "revision: \"2025-07-15\"",
                "name: \"September Promo Campaign\"",
                "sendStrategy:",
                "  method: \"immediate\"",
                "sendOptions:",
                "  use_smart_sending: true"
            }
        )
    }
)
public class CreateCampaign extends Task implements RunnableTask<CreateCampaign.Output> {
    @Schema(
        title = "Klaviyo Private API Key",
        description = "Klaviyo Private API Key"
    )
    private Property<String> privateApiKey;

    @Schema(
        title = "Revision version of the API",
        description = "Revision of the API request"
    )
    private Property<String> revision;

    @Schema(
        title = "Attributes of the list",
        description = "Attributes of the list"
    )
    private Property<Attributes> attributes;

    @SuperBuilder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Attributes of the list")
    public static class Attributes {
        @Schema(
            title = "Name of the list",
            description = "Name of the list to create"
        )
        private Property<String> name;


        @Schema(
            title = "Audiences for the campaign",
            description = "Defines the included and excluded audience IDs for the campaign"
        )
        private Property<Audiences> audiences;

        @Schema(
            title = "Messages for the campaign",
            description = "List of messages associated with the campaign"
        )
        private Property<List<CampaignMessageData>> campaignMessages;

        @Schema(
            title = "Send strategy of the campaign",
            description = "Defines how and when the campaign is sent"
        )
        private Property<SendStrategy> send_strategy;

        @Schema(
            title = "Send options of the campaign",
            description = "Additional options for sending the campaign"
        )
        private Property<SendOptions> send_options;

        @Schema(
            title = "Tracking options of the campaign",
            description = "Options for tracking the campaign's performance"
        )
        private Property<TrackingOptions> tracking_options;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Audiences for the campaign")
    public static class Audiences {
        @Schema(
            title = "List of included audience IDs",
            description = "IDs of audiences to include in the campaign"
        )
        private Property<List<String>> included;

        @Schema(
            title = "List of excluded audience IDs",
            description = "IDs of audiences to exclude from the campaign"
        )
        private Property<List<String>> excluded;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Campaign message data")
    public static class CampaignMessageData {
        @Schema(
            title = "Attributes of the campaign message",
            description = "Attributes defining the content and settings of the campaign message"
        )
        private Property<CampaignMessageAttributes> attributes;

        @Schema(
            title = "Relationships of the campaign message",
            description = "Relationships linking to other resources like images"
        )
        private Property<CampaignMessageRelationships> relationships;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Attributes of the campaign message")
    public static class CampaignMessageAttributes {
        @Schema(
            title = "Definition of the campaign message",
            description = "Defines the channel, label, and content of the campaign message"
        )
        private Property<Definition> definition;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Relationships of the campaign message")
    public static class CampaignMessageRelationships {
        @Schema(
            title = "Image relationship for the campaign message",
            description = "Relationship linking to an image resource"
        )
        private Property<ImageRelationship> image;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Definition of the campaign message")
    public static class Definition {
        private Property<String> channel;
        private Property<String> label;
        private Property<Content> content;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Content of the campaign message")
    public static class Content {
        @Schema(
            title = "Subject of the email",
            description = "Subject line for the email campaign"
        )
        private Property<String> subject;

        @Schema(
            title = "Preview text of the email",
            description = "Preview text shown in email clients"
        )
        private Property<String> preview_text;

        @Schema(
            title = "From email address",
            description = "Email address that the campaign is sent from"
        )
        private Property<String> from_email;

        @Schema(
            title = "From label",
            description = "Name that appears as the sender of the email"
        )
        private Property<String> from_label;

        @Schema(
            title = "Reply-to email address",
            description = "Email address for replies to the campaign"
        )
        private Property<String> reply_to_email;

        @Schema(
            title = "CC email address",
            description = "Email address to be CC'd on the campaign"
        )
        private Property<String> cc_email;

        @Schema(
            title = "BCC email address",
            description = "Email address to be BCC'd on the campaign"
        )
        private Property<String> bcc_email;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Image relationship for the campaign message")
    public static class ImageRelationship {
        private Property<ImageData> data;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Image data for the campaign message")
    public static class ImageData {
        @Schema(
            title = "id of the image",
            description = "ID of the image resource"
        )
        private Property<String> id;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Send strategy of the campaign")
    public static class SendStrategy {
        @Schema(
            title = "Method of sending the campaign",
            description = "Method can be 'immediate' or 'scheduled'"
        )
        private Property<String> method;

        @Schema(
            title = "Datetime for scheduled send",
            description = "Datetime in ISO 8601 format when the campaign should be sent if method is 'scheduled'"
        )
        private Property<String> datetime;

        @Schema(
            title = "Options for the send strategy",
            description = "Additional options for sending the campaign"
        )
        private Property<Options> options;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Options for the send strategy")
    public static class Options {
        @Schema(
            title = "Is local time option",
            description = "Whether to use local time for scheduling"
        )
        private Property<Boolean> is_local;

        @Schema(
            title = "Send past recipients immediately option",
            description = "Whether to send to past recipients immediately"
        )
        private Property<Boolean> send_past_recipients_immediately;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Send options of the campaign")
    public static class SendOptions {
        @Schema(
            title = "Use smart sending option",
            description = "Whether to enable smart sending to avoid sending emails to recipients who have recently received emails"
        )
        private Property<Boolean> use_smart_sending;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Tracking options of the campaign")
    public static class TrackingOptions {
        @Schema(
            title = "Add tracking parameters option",
            description = "Whether to add tracking parameters to links in the campaign"
        )
        private Property<Boolean> add_tracking_params;

        @Schema(
            title = "Custom tracking parameters",
            description = "List of custom tracking parameters to include in the campaign links"
        )
        private Property<List<CustomTrackingParam>> custom_tracking_params;

        @Schema(
            title = "Is tracking clicks option",
            description = "Whether to track link clicks in the campaign"
        )
        private Property<Boolean> is_tracking_clicks;

        @Schema(
            title = "Is tracking opens option",
            description = "Whether to track email opens in the campaign"
        )
        private Property<Boolean> is_tracking_opens;
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "Custom tracking parameter")
    public static class CustomTrackingParam {
        @Schema(
            title = "Type of the tracking parameter",
            description = "Type is always 'custom_tracking_param'"
        )
        private Property<String> type;

        @Schema(
            title = "Value of the tracking parameter",
            description = "Value of the custom tracking parameter"
        )
        private Property<String> value;

        @Schema(
            title = "Name of the tracking parameter",
            description = "Name/key of the custom tracking parameter"
        )
        private Property<String> name;
    }

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        ObjectMapper objectMapper = Utils.getMapper();
        HttpClient httpClient = Utils.getHttpClient(runContext);

        // render inputs
        String renderedApiKey = runContext.render(privateApiKey).as(String.class).orElse(null);
        String renderedRevision = runContext.render(revision).as(String.class).orElse(Constants.DEFAULT_REVISION);
        Attributes renderedAttributes = runContext.render(attributes).as(Attributes.class).orElse(null);

        logger.info("Creating campaign in Klaviyo...");

        // render nested properties
        Audiences renderedAudiences = runContext.render(renderedAttributes.getAudiences()).as(Audiences.class).orElse(null);
        List<CampaignMessageData> renderedMessages = runContext.render(renderedAttributes.getCampaignMessages())
            .asList(CampaignMessageData.class);
        List<CreateCampaignRequest.CampaignMessageData> campaignMessageDataList = renderedMessages.stream().map(message -> {
            try {
                CampaignMessageAttributes renderedMessageAttributes = runContext.render(message.getAttributes()).as(CampaignMessageAttributes.class).orElse(null);
                Definition renderedDefinition = runContext.render(renderedMessageAttributes.getDefinition()).as(Definition.class).orElse(null);
                Content renderedContent = runContext.render(renderedDefinition.getContent()).as(Content.class).orElse(null);
                CampaignMessageRelationships renderedMessageRelationships = runContext.render(message.getRelationships()).as(CampaignMessageRelationships.class).orElse(null);
                CreateCampaignRequest.Definition definition = CreateCampaignRequest.Definition.builder()
                    .channel(renderedDefinition.getChannel() != null ? renderedDefinition.getChannel().toString() : null)
                    .label(renderedDefinition.getLabel() != null ? renderedDefinition.getLabel().toString() : null)
                    .content(CreateCampaignRequest.Content.builder()
                        .subject(renderedContent.getSubject() != null ? renderedContent.getSubject().toString() : null)
                        .preview_text(renderedContent.getPreview_text() != null ? renderedContent.getPreview_text().toString() : null)
                        .from_email(renderedContent.getFrom_email() != null ? renderedContent.getFrom_email().toString() : null)
                        .from_label(renderedContent.getFrom_label() != null ? renderedContent.getFrom_label().toString() : null)
                        .reply_to_email(renderedContent.getReply_to_email() != null ? renderedContent.getReply_to_email().toString() : null)
                        .cc_email(renderedContent.getCc_email() != null ? renderedContent.getCc_email().toString() : null)
                        .bcc_email(renderedContent.getBcc_email() != null ? renderedContent.getBcc_email().toString() : null)
                        .build())
                    .build();
                CampaignMessageRelationships renderedCampaignMessageRelationships = runContext.render(message.getRelationships()).as(CampaignMessageRelationships.class).orElse(null);
                ImageRelationship renderedImageRelationship = runContext.render(renderedCampaignMessageRelationships.getImage()).as(ImageRelationship.class).orElse(null);
                ImageData renderedImageData = runContext.render(renderedImageRelationship.getData()).as(ImageData.class).orElse(null);
                CreateCampaignRequest.ImageData imageData = CreateCampaignRequest.ImageData.builder()
                    .type(Constants.IMAGE)
                    .id(renderedImageData.getId().toString())
                    .build();
                CreateCampaignRequest.CampaignMessageAttributes messageAttributes = CreateCampaignRequest.CampaignMessageAttributes.builder()
                    .definition(definition)
                    .build();
                CreateCampaignRequest.CampaignMessageRelationships messageRelationships = CreateCampaignRequest.CampaignMessageRelationships.builder()
                    .image(CreateCampaignRequest.ImageRelationship.builder()
                        .data(imageData)
                        .build())
                    .build();
                return CreateCampaignRequest.CampaignMessageData.builder()
                    .type(Constants.CAMPAIGN_MESSAGE)
                    .attributes(messageAttributes)
                    .relationships(messageRelationships)
                    .build();
            } catch (IllegalVariableEvaluationException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toUnmodifiableList());
        SendStrategy renderedSendStrategy = runContext.render(renderedAttributes.getSend_strategy()).as(SendStrategy.class).orElse(null);
        Options renderedOptions = renderedSendStrategy != null ? runContext.render(renderedSendStrategy.getOptions()).as(Options.class).orElse(null) : null;
        Boolean renderedIsLocal = renderedOptions != null ? runContext.render(renderedOptions.getIs_local()).as(Boolean.class).orElse(null) : null;
        Boolean renderedSendPastRecipientsImmediately = renderedOptions != null ? runContext.render(renderedOptions.getSend_past_recipients_immediately()).as(Boolean.class).orElse(null) : null;
        SendOptions renderedSendOptions = runContext.render(renderedAttributes.getSend_options()).as(SendOptions.class).orElse(null);
        Boolean renderedUseSmartSending = renderedSendOptions != null ? runContext.render(renderedSendOptions.getUse_smart_sending()).as(Boolean.class).orElse(null) : null;
        TrackingOptions renderedTrackingOptions = runContext.render(renderedAttributes.getTracking_options()).as(TrackingOptions.class).orElse(null);
        Boolean renderedAddTrackingParams = renderedTrackingOptions != null ? runContext.render(renderedTrackingOptions.getAdd_tracking_params()).as(Boolean.class).orElse(null) : null;
        List<CustomTrackingParam> renderedCustomTrackingParams = renderedTrackingOptions != null ? runContext.render(renderedTrackingOptions.getCustom_tracking_params()).asList(CustomTrackingParam.class) : null;
        List<CreateCampaignRequest.CustomTrackingParam> customTrackingParamList = renderedCustomTrackingParams != null ? renderedCustomTrackingParams.stream().map(param -> {
            return CreateCampaignRequest.CustomTrackingParam.builder()
                .type(param.getType().toString())
                .name(param.getName().toString())
                .value(param.getValue().toString())
                .build();
        }).collect(Collectors.toUnmodifiableList()) : null;
        Boolean renderedIsTrackingClicks = renderedTrackingOptions != null ? runContext.render(renderedTrackingOptions.getIs_tracking_clicks()).as(Boolean.class).orElse(null) : null;
        Boolean renderedIsTrackingOpens = renderedTrackingOptions != null ? runContext.render(renderedTrackingOptions.getIs_tracking_opens()).as(Boolean.class).orElse(null) : null;

        // build request
        CreateCampaignRequest.Attributes attributes = CreateCampaignRequest.Attributes.builder()
            .name(renderedAttributes.getName().toString())
            .audiences(CreateCampaignRequest.Audiences.builder()
                .included(renderedAudiences != null ? runContext.render(renderedAudiences.getIncluded()).asList(String.class) : null)
                .excluded(renderedAudiences != null ? runContext.render(renderedAudiences.getExcluded()).asList(String.class) : null)
                .build())
            .campaignMessages(campaignMessageDataList)
            .send_strategy(renderedSendStrategy != null ? CreateCampaignRequest.SendStrategy.builder()
                .method(renderedSendStrategy.getMethod().toString())
                .datetime(renderedSendStrategy.getDatetime() != null ? renderedSendStrategy.getDatetime().toString() : null)
                .options(renderedOptions != null ? CreateCampaignRequest.Options.builder()
                    .is_local(renderedIsLocal)
                    .send_past_recipients_immediately(renderedSendPastRecipientsImmediately)
                    .build() : null)
                .build() : null)
            .send_options(renderedSendOptions != null ? CreateCampaignRequest.SendOptions.builder()
                .use_smart_sending(renderedUseSmartSending)
                .build() : null)
            .tracking_options(renderedTrackingOptions != null ? CreateCampaignRequest.TrackingOptions.builder()
                .add_tracking_params(renderedAddTrackingParams)
                .custom_tracking_params(customTrackingParamList)
                .is_tracking_clicks(renderedIsTrackingClicks)
                .is_tracking_opens(renderedIsTrackingOpens)
                .build() : null)
            .build();

        CreateCampaignRequest createCampaignRequest = CreateCampaignRequest.builder()
            .data(CreateCampaignRequest.Data.builder()
                .type(Constants.CAMPAIGN)
                .attributes(attributes)
                .build())
            .build();

        String requestJson = objectMapper.writeValueAsString(createCampaignRequest);

        ClassicHttpRequest request = Utils.getHttpRequest(Constants.CAMPAIGN_URL, requestJson, renderedRevision, renderedApiKey);
        HttpRequest httpRequest = HttpRequest.from(request);

        // send request
        HttpResponse httpResponse = httpClient.request(httpRequest);
        int statusCode = httpResponse.getStatus().getCode();

        if (statusCode != 201) {
            ErrorResponse errorResponse = objectMapper.convertValue(httpResponse.getBody(), ErrorResponse.class);
            logger.error("Failed to create campaign: {}", errorResponse);
            return Output.builder()
                .status(Constants.ERROR)
                .statusCode(statusCode)
                .errorResponse(errorResponse)
                .build();
        }

        // success
        CampaignCreateSuccessResponse campaignCreateSuccessResponse = objectMapper.convertValue(httpResponse.getBody(), CampaignCreateSuccessResponse.class);
        logger.info("Campaign created successfully in Klaviyo with ID: {}", campaignCreateSuccessResponse.getData().getId());
        return Output.builder()
            .status(Constants.SUCCESS)
            .statusCode(statusCode)
            .campaignCreateSuccessResponse(campaignCreateSuccessResponse)
            .errorResponse(null)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(title = "Status of the campaign creation")
        private final String status;

        @Schema(title = "Status code of the response")
        private final Integer statusCode;

        @Schema(title = "Response of the campaign creation - success case")
        private final CampaignCreateSuccessResponse campaignCreateSuccessResponse;

        @Schema(title = "Response of the campaign creation - error case")
        private final ErrorResponse errorResponse;
    }
}


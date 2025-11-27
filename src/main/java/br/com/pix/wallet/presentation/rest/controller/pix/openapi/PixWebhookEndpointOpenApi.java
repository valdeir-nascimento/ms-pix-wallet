package br.com.pix.wallet.presentation.rest.controller.pix.openapi;

import br.com.pix.wallet.application.pix.webhook.HandlePixWebhookOutput;
import br.com.pix.wallet.presentation.rest.controller.pix.request.HandlePixWebhookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Pix Webhooks", description = "Operations related to Pix Webhook events")
public interface PixWebhookEndpointOpenApi {

    @Operation(summary = "Handle Pix Webhook Event", description = "Receives and processes a Pix webhook event.", responses = {
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HandlePixWebhookOutput.class), examples = @ExampleObject(value = """
                    {
                      "webhookEventId": "d4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55",
                      "eventId": "evt-001",
                      "endToEndId": "E12345678202310271000s0011234567",
                      "eventType": "PAYMENT_RECEIVED",
                      "occurredAt": "2023-10-27T10:00:00Z",
                      "processedAt": "2023-10-27T10:00:01Z"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    ResponseEntity<HandlePixWebhookOutput> handleWebhook(
            @RequestBody(description = "Webhook event data", required = true, content = @Content(schema = @Schema(implementation = HandlePixWebhookRequest.class), examples = @ExampleObject(value = """
                    {
                      "eventId": "evt-001",
                      "endToEndId": "E12345678202310271000s0011234567",
                      "eventType": "PAYMENT_RECEIVED",
                      "occurredAt": "2023-10-27T10:00:00Z"
                    }
                    """))) HandlePixWebhookRequest request);
}

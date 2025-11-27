package br.com.pix.wallet.presentation.rest.controller.pix.openapi;

import br.com.pix.wallet.application.pix.transfer.CreatePixTransferOutput;
import br.com.pix.wallet.presentation.rest.controller.pix.request.CreatePixTransferRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Pix Transfers", description = "Operations related to Pix Transfers")
public interface PixTransferEndpointOpenApi {

    @Operation(summary = "Create a Pix Transfer", description = "Initiates a Pix transfer from one wallet to another using an idempotency key.", responses = {
        @ApiResponse(responseCode = "201", description = "Transfer created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreatePixTransferOutput.class), examples = @ExampleObject(value = """
            {
              "transferId": "c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33",
              "endToEndId": "E12345678202310271000s0011234567",
              "fromWalletId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
              "toWalletId": "d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44",
              "amount": 100.00,
              "status": "COMPLETED"
            }
            """))),
        @ApiResponse(responseCode = "400", description = "Invalid request data, insufficient funds, or idempotency conflict"),
        @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    ResponseEntity<CreatePixTransferOutput> createPixTransfer(
        @RequestBody(description = "Pix Transfer data", required = true, content = @Content(schema = @Schema(implementation = CreatePixTransferRequest.class), examples = @ExampleObject(value = """
            {
              "fromWalletId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
              "toWalletId": "d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44",
              "amount": 100.00,
              "idempotencyKey": "unique-key-123",
              "endToEndId": "E12345678202310271000s0011234567"
            }
            """))) CreatePixTransferRequest request
    );
}

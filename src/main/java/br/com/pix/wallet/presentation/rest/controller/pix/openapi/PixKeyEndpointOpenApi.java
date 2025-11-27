package br.com.pix.wallet.presentation.rest.controller.pix.openapi;

import br.com.pix.wallet.application.pix.register.RegisterPixKeyOutput;
import br.com.pix.wallet.presentation.rest.controller.pix.request.RegisterPixKeyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Pix Keys", description = "Operations related to Pix Keys management")
public interface PixKeyEndpointOpenApi {

    @Operation(summary = "Register a new Pix Key", description = "Registers a new Pix Key (EMAIL, PHONE, EVP) for a specific wallet.", responses = {
            @ApiResponse(responseCode = "201", description = "Pix Key registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterPixKeyOutput.class), examples = @ExampleObject(value = """
                    {
                      "pixKeyId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
                      "walletId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                      "keyType": "EMAIL",
                      "keyValue": "user@example.com"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or key already exists"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    ResponseEntity<RegisterPixKeyOutput> registerPixKey(
            @Parameter(description = "ID of the wallet", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11") String walletId,

            @RequestBody(description = "Pix Key registration data", required = true, content = @Content(schema = @Schema(implementation = RegisterPixKeyRequest.class), examples = @ExampleObject(value = """
                    {
                      "keyType": "EMAIL",
                      "keyValue": "user@example.com"
                    }
                    """))) RegisterPixKeyRequest request);
}

package br.com.pix.wallet.presentation.rest.controller.wallet.openapi;

import br.com.pix.wallet.application.wallet.balance.GetBalanceOutput;
import br.com.pix.wallet.application.wallet.create.CreateWalletOutput;
import br.com.pix.wallet.application.wallet.deposit.DepositOutput;
import br.com.pix.wallet.application.wallet.withdraw.WithdrawOutput;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.CreateWalletRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.DepositRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.WithdrawRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "Wallets", description = "Operations related to digital wallets")
public interface WalletEndpointOpenApi {

    @Operation(
        summary = "Create a new wallet",
        description = "Creates a new digital wallet for a specific owner.",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Wallet created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateWalletOutput.class), examples = @ExampleObject(value = """
                    {
                      "walletId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                      "ownerId": "12345678900"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
    ResponseEntity<CreateWalletOutput> createWallet(
        @RequestBody(
            description = "Data to create a wallet",
            required = true,
            content = @Content(schema = @Schema(implementation = CreateWalletRequest.class), examples = @ExampleObject(value = """
                {
                  "ownerId": "12345678900"
                }
                """))) CreateWalletRequest request);

    @Operation(
        summary = "Get wallet balance",
        description = "Retrieves the current balance of a wallet. Optionally, a specific date/time can be provided to get the balance at that moment.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Balance retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GetBalanceOutput.class), examples = @ExampleObject(value = """
                    {
                      "currentBalance": 100.50
                    }
                    """
                ))),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
        })
    ResponseEntity<GetBalanceOutput> getBalance(
        @Parameter(description = "ID of the wallet", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11") UUID walletId,
        @Parameter(description = "Date/time to check the balance (ISO 8601). If not provided, returns current balance.", example = "2023-10-27T10:00:00Z") Instant at
    );

    @Operation(
        summary = "Deposit funds",
        description = "Deposits a specific amount into the wallet.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Deposit successful",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DepositOutput.class), examples = @ExampleObject(value = """
                    {
                      "walletId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                      "newBalance": 150.50
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid amount or request data"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
        })
    ResponseEntity<DepositOutput> deposit(
        @Parameter(description = "ID of the wallet", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11") String walletId,
        @RequestBody(description = "Deposit data", required = true, content = @Content(schema = @Schema(implementation = DepositRequest.class), examples = @ExampleObject(value = """
            {
              "amount": 50.00
            }
            """))) DepositRequest request);

    @Operation(
        summary = "Withdraw funds",
        description = "Withdraws a specific amount from the wallet.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Withdrawal successful",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WithdrawOutput.class), examples = @ExampleObject(value = """
                    {
                      "walletId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                      "newBalance": 50.50
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Insufficient funds or invalid amount"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
        })
    ResponseEntity<WithdrawOutput> withdraw(
        @Parameter(description = "ID of the wallet", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11") UUID walletId,
        @RequestBody(
            description = "Withdrawal data",
            required = true,
            content = @Content(schema = @Schema(implementation = WithdrawRequest.class), examples = @ExampleObject(value = """
                {
                  "amount": 50.00
                }
                """))) WithdrawRequest request
    );
}

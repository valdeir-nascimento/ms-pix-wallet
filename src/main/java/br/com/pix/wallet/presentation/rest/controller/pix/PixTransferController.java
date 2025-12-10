package br.com.pix.wallet.presentation.rest.controller.pix;

import br.com.pix.wallet.application.pix.transfer.CreatePixTransferCommand;
import br.com.pix.wallet.application.pix.transfer.CreatePixTransferOutput;
import br.com.pix.wallet.application.pix.transfer.CreatePixTransferUseCase;
import br.com.pix.wallet.presentation.rest.controller.pix.openapi.PixTransferEndpointOpenApi;
import br.com.pix.wallet.presentation.rest.controller.pix.request.CreatePixTransferRequest;
import br.com.pix.wallet.presentation.rest.helper.ApiUriFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pix/transfers")
public class PixTransferController implements PixTransferEndpointOpenApi {

    private final CreatePixTransferUseCase createPixTransferUseCase;

    public PixTransferController(final CreatePixTransferUseCase createPixTransferUseCase) {
        this.createPixTransferUseCase = createPixTransferUseCase;
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<CreatePixTransferOutput> createPixTransfer(@Valid @RequestBody final CreatePixTransferRequest request) {
        final var command = CreatePixTransferCommand.with(
            request.fromWalletId(),
            request.toWalletId(),
            request.amount(),
            request.idempotencyKey(),
            request.endToEndId()
        );

        final var output = createPixTransferUseCase.execute(command);

        final var location = ApiUriFactory.createdLocation("/pix/transfers/{id}", output.transferId());

        return ResponseEntity.created(location).body(output);
    }
}

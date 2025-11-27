package br.com.pix.wallet.presentation.rest.controller.pix;

import br.com.pix.wallet.application.pix.register.RegisterPixKeyCommand;
import br.com.pix.wallet.application.pix.register.RegisterPixKeyOutput;
import br.com.pix.wallet.application.pix.register.RegisterPixKeyUseCase;
import br.com.pix.wallet.presentation.rest.controller.pix.openapi.PixKeyEndpointOpenApi;
import br.com.pix.wallet.presentation.rest.controller.pix.request.RegisterPixKeyRequest;
import br.com.pix.wallet.presentation.rest.helper.ApiUriFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets/{walletId}/pix-keys")
public class PixKeyController implements PixKeyEndpointOpenApi {

    private final RegisterPixKeyUseCase registerPixKeyUseCase;

    public PixKeyController(final RegisterPixKeyUseCase registerPixKeyUseCase) {
        this.registerPixKeyUseCase = registerPixKeyUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<RegisterPixKeyOutput> registerPixKey(@PathVariable final String walletId, @Valid @RequestBody final RegisterPixKeyRequest request) {
        final var command = RegisterPixKeyCommand.with(
            walletId,
            request.keyType(),
            request.keyValue()
        );

        final var output = registerPixKeyUseCase.execute(command);

        final var location = ApiUriFactory.createdLocation("/wallets/{walletId}/pix-keys/{pixKeyId}", output.walletId(), output.pixKeyId());

        return ResponseEntity.created(location).body(output);
    }
}

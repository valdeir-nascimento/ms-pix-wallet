package br.com.pix.wallet.presentation.rest.controller.pix;

import br.com.pix.wallet.application.pix.webhook.HandlePixWebhookCommand;
import br.com.pix.wallet.application.pix.webhook.HandlePixWebhookOutput;
import br.com.pix.wallet.application.pix.webhook.HandlePixWebhookUseCase;
import br.com.pix.wallet.presentation.rest.controller.pix.openapi.PixWebhookEndpointOpenApi;
import br.com.pix.wallet.presentation.rest.controller.pix.request.HandlePixWebhookRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pix/webhooks")
public class PixWebhookController implements PixWebhookEndpointOpenApi {

    private final HandlePixWebhookUseCase handlePixWebhookUseCase;

    public PixWebhookController(final HandlePixWebhookUseCase handlePixWebhookUseCase) {
        this.handlePixWebhookUseCase = handlePixWebhookUseCase;
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<HandlePixWebhookOutput> handleWebhook(@Valid @RequestBody final HandlePixWebhookRequest request) {
        final var command = HandlePixWebhookCommand.with(
            request.eventId(),
            request.endToEndId(),
            request.eventType(),
            request.occurredAt()
        );

        final var output = handlePixWebhookUseCase.execute(command);

        return ResponseEntity.ok(output);
    }
}

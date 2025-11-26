package br.com.pix.wallet.domain.pix.webhook;

import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.validation.Validator;

public class PixWebhookEventValidator extends Validator {

    private final PixWebhookEvent event;

    public PixWebhookEventValidator(final PixWebhookEvent event, final ValidationHandler handler) {
        super(handler);
        this.event = event;
    }

    @Override
    public void validate() {

        if (event.getEventId() == null || event.getEventId().isBlank()) {
            validationHandler().append(Error.of("'eventId' must not be null or blank"));
        }

        if (event.getEndToEndId() == null || event.getEndToEndId().isBlank()) {
            validationHandler().append(Error.of("'endToEndId' must not be null or blank"));
        }

        if (event.getType() == null) {
            validationHandler().append(Error.of("'type' must not be null"));
        }

        if (event.getOccurredAt() == null) {
            validationHandler().append(Error.of("'occurredAt' must not be null"));
        }

        if (event.getProcessedAt() == null) {
            validationHandler().append(Error.of("'processedAt' must not be null"));
        }
    }
}

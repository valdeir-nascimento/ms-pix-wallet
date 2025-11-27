package br.com.pix.wallet.application.pix.webhook;

import br.com.pix.wallet.domain.pix.transfer.PixTransferGateway;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventGateway;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEvent;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventType;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.handler.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HandlePixWebhookUseCaseImpl implements HandlePixWebhookUseCase {

    private final PixWebhookEventGateway pixWebhookEventGateway;
    private final PixTransferGateway pixTransferGateway;

    public HandlePixWebhookUseCaseImpl(final PixWebhookEventGateway pixWebhookEventGateway, final PixTransferGateway pixTransferGateway) {
        this.pixWebhookEventGateway = pixWebhookEventGateway;
        this.pixTransferGateway = pixTransferGateway;
    }

    @Override
    @Transactional
    public HandlePixWebhookOutput execute(final HandlePixWebhookCommand command) {
        return pixWebhookEventGateway.findByEventId(command.eventId())
            .map(HandlePixWebhookOutput::from)
            .orElseGet(() -> handleNewEvent(command));
    }

    private HandlePixWebhookOutput handleNewEvent(final HandlePixWebhookCommand command) {
        pixTransferGateway.findByEndToEndId(command.endToEndId())
            .orElseThrow(() -> DomainException.with(Error.of("No Pix transfer found for endToEndId '%s'".formatted(command.endToEndId()))));

        final var type = PixWebhookEventType.from(command.eventType());

        final var webhookEvent = PixWebhookEvent.newEvent(
            type,
            command.eventId(),
            command.endToEndId(),
            command.occurredAt()
        );

        final var notification = Notification.create();

        webhookEvent.validate(notification);

        if (notification.hasError()) {
            throw DomainException.with(notification.getErrors());
        }

        final var saved = pixWebhookEventGateway.save(webhookEvent);

        return HandlePixWebhookOutput.from(saved);
    }
}

package br.com.pix.wallet.application.pix.webhook;

import br.com.pix.wallet.application.UseCaseTest;
import br.com.pix.wallet.application.metrics.ApplicationMetrics;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.pix.transfer.PixTransfer;
import br.com.pix.wallet.domain.pix.transfer.PixTransferGateway;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEvent;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventGateway;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventID;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventType;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HandlePixWebhookUseCaseImplTest extends UseCaseTest {

    @InjectMocks
    private HandlePixWebhookUseCaseImpl handlePixWebhookUseCase;

    @Mock
    private PixWebhookEventGateway pixWebhookEventGateway;

    @Mock
    private PixTransferGateway pixTransferGateway;

    @Mock
    private ApplicationMetrics applicationMetrics;

    @Override
    protected List<Object> getMocks() {
        return List.of(pixWebhookEventGateway, pixTransferGateway, applicationMetrics);
    }

    @Test
    void givenDuplicateEvent_whenHandlePixWebhook_thenReturnExistingEvent() {
        // given
        final var expectedEventId = UUID.randomUUID().toString();
        final var expectedEndToEndId = "E12345678202311261234567890AB";
        final var expectedEventType = PixWebhookEventType.CREDIT_CONFIRMED.name();
        final var expectedOccurredAt = Instant.now();

        final var command = HandlePixWebhookCommand.with(
            expectedEventId,
            expectedEndToEndId,
            expectedEventType,
            expectedOccurredAt
        );

        final var existingEvent = mock(PixWebhookEvent.class);
        final var existingEventIdMock = mock(PixWebhookEventID.class);
        when(existingEvent.getId()).thenReturn(existingEventIdMock);
        when(existingEvent.getType()).thenReturn(PixWebhookEventType.CREDIT_CONFIRMED);
        when(existingEventIdMock.getValue()).thenReturn(UUID.fromString(expectedEventId));
        when(pixWebhookEventGateway.findByEventId(expectedEventId)).thenReturn(Optional.of(existingEvent));

        // when
        final var output = handlePixWebhookUseCase.execute(command);

        // then
        assertNotNull(output);
        verify(pixWebhookEventGateway).findByEventId(expectedEventId);
        verify(pixTransferGateway, never()).findByEndToEndId(any());
        verify(pixWebhookEventGateway, never()).save(any());
    }

    @Test
    void givenValidCommand_whenHandlePixWebhook_thenReturnOutput() {
        // given
        final var expectedEventId = UUID.randomUUID().toString();
        final var expectedEndToEndId = "E12345678202311261234567890AB";
        final var expectedEventType = "CREDIT_CONFIRMED";
        final var expectedOccurredAt = Instant.now();

        final var command = HandlePixWebhookCommand.with(
            expectedEventId,
            expectedEndToEndId,
            expectedEventType,
            expectedOccurredAt
        );

        final var transfer = mock(PixTransfer.class);
        when(pixWebhookEventGateway.findByEventId(expectedEventId)).thenReturn(Optional.empty());
        when(pixTransferGateway.findByEndToEndId(expectedEndToEndId)).thenReturn(Optional.of(transfer));
        when(pixWebhookEventGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        final var output = handlePixWebhookUseCase.execute(command);

        // then
        assertNotNull(output);
        assertNotNull(output.eventId());
        verify(pixWebhookEventGateway).findByEventId(expectedEventId);
        verify(pixTransferGateway).findByEndToEndId(expectedEndToEndId);
        verify(pixWebhookEventGateway).save(any());
    }

    @Test
    void givenTransferNotFound_whenHandlePixWebhook_thenThrowDomainException() {
        // given
        final var expectedEventId = UUID.randomUUID().toString();
        final var expectedEndToEndId = "E12345678202311261234567890AB";
        final var expectedEventType = PixWebhookEventType.CREDIT_CONFIRMED.name();
        final var expectedOccurredAt = Instant.now();

        final var command = HandlePixWebhookCommand.with(
            expectedEventId,
            expectedEndToEndId,
            expectedEventType,
            expectedOccurredAt
        );

        when(pixWebhookEventGateway.findByEventId(expectedEventId)).thenReturn(Optional.empty());
        when(pixTransferGateway.findByEndToEndId(expectedEndToEndId)).thenReturn(Optional.empty());

        // when
        final var exception = assertThrows(DomainException.class, () -> handlePixWebhookUseCase.execute(command));

        // then
        assertNotNull(exception);
        assertEquals(1, exception.getErrors().size());
        assertEquals("No Pix transfer found for endToEndId 'E12345678202311261234567890AB'", exception.getErrors().get(0).message());
        verify(pixWebhookEventGateway).findByEventId(expectedEventId);
        verify(pixTransferGateway).findByEndToEndId(expectedEndToEndId);
        verify(pixWebhookEventGateway, never()).save(any());
    }

    @Test
    void givenInvalidEventType_whenHandlePixWebhook_thenThrowDomainException() {
        // given
        final var expectedEventId = UUID.randomUUID().toString();
        final var expectedEndToEndId = "E12345678202311261234567890AB";
        final var invalidEventType = "INVALID_TYPE";
        final var expectedOccurredAt = Instant.now();

        final var command = HandlePixWebhookCommand.with(
            expectedEventId,
            expectedEndToEndId,
            invalidEventType,
            expectedOccurredAt
        );

        final var transfer = mock(PixTransfer.class);
        when(pixWebhookEventGateway.findByEventId(expectedEventId)).thenReturn(Optional.empty());
        when(pixTransferGateway.findByEndToEndId(expectedEndToEndId)).thenReturn(Optional.of(transfer));

        // when
        final var exception = assertThrows(DomainException.class, () -> handlePixWebhookUseCase.execute(command));

        // then
        assertNotNull(exception);
        assertTrue(exception.getErrors().size() > 0);
        verify(pixWebhookEventGateway).findByEventId(expectedEventId);
        verify(pixTransferGateway).findByEndToEndId(expectedEndToEndId);
        verify(pixWebhookEventGateway, never()).save(any());
    }
}

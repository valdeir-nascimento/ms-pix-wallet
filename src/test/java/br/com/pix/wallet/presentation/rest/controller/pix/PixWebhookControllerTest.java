package br.com.pix.wallet.presentation.rest.controller.pix;

import br.com.pix.wallet.ControllerTest;
import br.com.pix.wallet.application.pix.webhook.HandlePixWebhookCommand;
import br.com.pix.wallet.application.pix.webhook.HandlePixWebhookOutput;
import br.com.pix.wallet.application.pix.webhook.HandlePixWebhookUseCase;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.presentation.rest.controller.pix.request.HandlePixWebhookRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = PixWebhookController.class)
class PixWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private HandlePixWebhookUseCase handlePixWebhookUseCase;

    @Test
    void givenAValidCommand_whenCallsHandleWebhook_thenShouldReturnOk() throws Exception {
        // given
        final var expectedWebhookEventId = UUID.randomUUID().toString();
        final var expectedEventId = UUID.randomUUID().toString();
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedEventType = "PAYMENT_RECEIVED";
        final var expectedOccurredAt = Instant.now();
        final var expectedProcessedAt = Instant.now();

        final var requestBody = new HandlePixWebhookRequest(
            expectedEventId,
            expectedEndToEndId,
            expectedEventType,
            expectedOccurredAt
        );

        final var expectedOutput = new HandlePixWebhookOutput(
            expectedWebhookEventId,
            expectedEventId,
            expectedEndToEndId,
            expectedEventType,
            expectedOccurredAt,
            expectedProcessedAt
        );

        when(handlePixWebhookUseCase.execute(any(HandlePixWebhookCommand.class))).thenReturn(expectedOutput);

        // when
        final var request = post("/pix/webhooks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isOk())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.webhookEventId", equalTo(expectedWebhookEventId)))
            .andExpect(jsonPath("$.eventId", equalTo(expectedEventId)))
            .andExpect(jsonPath("$.endToEndId", equalTo(expectedEndToEndId)))
            .andExpect(jsonPath("$.eventType", equalTo(expectedEventType)))
            .andExpect(jsonPath("$.occurredAt", notNullValue()))
            .andExpect(jsonPath("$.processedAt", notNullValue()));

        verify(handlePixWebhookUseCase, times(1))
            .execute(argThat(command -> Objects.equals(expectedEventId, command.eventId())
                && Objects.equals(expectedEndToEndId, command.endToEndId())
                && Objects.equals(expectedEventType, command.eventType())
                && Objects.equals(expectedOccurredAt, command.occurredAt())));
    }

    @Test
    void givenAInvalidCommand_whenCallsHandleWebhook_thenShouldReturnDomainException() throws Exception {
        // given
        final var expectedEventId = UUID.randomUUID().toString();
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedEventType = "PAYMENT_RECEIVED";
        final var expectedOccurredAt = Instant.now();
        final var expectedMessage = "Invalid event";

        final var requestBody = new HandlePixWebhookRequest(
            expectedEventId,
            expectedEndToEndId,
            expectedEventType,
            expectedOccurredAt
        );

        when(handlePixWebhookUseCase.execute(any(HandlePixWebhookCommand.class)))
            .thenThrow(DomainException.with(Error.of(expectedMessage)));

        // when
        final var request = post("/pix/webhooks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isUnprocessableEntity())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.message", equalTo(expectedMessage)));

        verify(handlePixWebhookUseCase, times(1)).execute(any());
    }
}

package br.com.pix.wallet.presentation.rest.controller.pix;

import br.com.pix.wallet.ControllerTest;
import br.com.pix.wallet.application.pix.transfer.CreatePixTransferCommand;
import br.com.pix.wallet.application.pix.transfer.CreatePixTransferOutput;
import br.com.pix.wallet.application.pix.transfer.CreatePixTransferUseCase;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.exception.NotFoundException;
import br.com.pix.wallet.domain.pix.transfer.PixTransferStatus;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.presentation.rest.controller.pix.request.CreatePixTransferRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = PixTransferController.class)
class PixTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private CreatePixTransferUseCase createPixTransferUseCase;

    @Test
    void givenAValidCommand_whenCallsCreatePixTransfer_thenShouldReturnTransferId() throws Exception {
        // given
        final var expectedTransferId = UUID.randomUUID().toString();
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedFromWalletId = UUID.randomUUID().toString();
        final var expectedToWalletId = UUID.randomUUID().toString();
        final var expectedAmount = BigDecimal.TEN;
        final var expectedIdempotencyKey = UUID.randomUUID().toString();
        final var expectedStatus = PixTransferStatus.CONFIRMED;

        final var requestBody = new CreatePixTransferRequest(
            expectedFromWalletId,
            expectedToWalletId,
            expectedAmount,
            expectedIdempotencyKey,
            expectedEndToEndId
        );

        final var expectedOutput = new CreatePixTransferOutput(
            expectedTransferId,
            expectedEndToEndId,
            expectedFromWalletId,
            expectedToWalletId,
            expectedAmount,
            expectedStatus
        );

        when(createPixTransferUseCase.execute(any(CreatePixTransferCommand.class))).thenReturn(expectedOutput);

        // when
        final var request = post("/pix/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString(expectedTransferId)))
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.transferId", equalTo(expectedTransferId)))
            .andExpect(jsonPath("$.endToEndId", equalTo(expectedEndToEndId)))
            .andExpect(jsonPath("$.fromWalletId", equalTo(expectedFromWalletId)))
            .andExpect(jsonPath("$.toWalletId", equalTo(expectedToWalletId)))
            .andExpect(jsonPath("$.amount", equalTo(expectedAmount.intValue())))
            .andExpect(jsonPath("$.status", equalTo(expectedStatus.name())));

        verify(createPixTransferUseCase, times(1))
            .execute(argThat(command -> Objects.equals(expectedFromWalletId, command.fromWalletId())
                && Objects.equals(expectedToWalletId, command.toWalletId())
                && Objects.equals(expectedAmount, command.amount())
                && Objects.equals(expectedIdempotencyKey, command.idempotencyKey())
                && Objects.equals(expectedEndToEndId, command.endToEndId())));
    }

    @Test
    void givenAInvalidCommand_whenCallsCreatePixTransfer_thenShouldReturnDomainException() throws Exception {
        // given
        final var expectedFromWalletId = UUID.randomUUID().toString();
        final var expectedToWalletId = UUID.randomUUID().toString();
        final var expectedAmount = BigDecimal.TEN;
        final var expectedIdempotencyKey = UUID.randomUUID().toString();
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedMessage = "Insufficient funds";

        final var requestBody = new CreatePixTransferRequest(
            expectedFromWalletId,
            expectedToWalletId,
            expectedAmount,
            expectedIdempotencyKey,
            expectedEndToEndId
        );

        when(createPixTransferUseCase.execute(any(CreatePixTransferCommand.class)))
            .thenThrow(DomainException.with(Error.of(expectedMessage)));

        // when
        final var request = post("/pix/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isUnprocessableEntity())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.message", equalTo(expectedMessage)));

        verify(createPixTransferUseCase, times(1)).execute(any());
    }

    @Test
    void givenAInvalidWalletId_whenCallsCreatePixTransfer_thenShouldReturnNotFoundException() throws Exception {
        // given
        final var expectedFromWalletId = UUID.randomUUID().toString();
        final var expectedToWalletId = UUID.randomUUID().toString();
        final var expectedAmount = BigDecimal.TEN;
        final var expectedIdempotencyKey = UUID.randomUUID().toString();
        final var expectedEndToEndId = UUID.randomUUID().toString();
        final var expectedErrorMessage = "Wallet with ID %s was not found" .formatted(expectedFromWalletId);

        final var requestBody = new CreatePixTransferRequest(
            expectedFromWalletId,
            expectedToWalletId,
            expectedAmount,
            expectedIdempotencyKey,
            expectedEndToEndId);

        when(createPixTransferUseCase.execute(any(CreatePixTransferCommand.class)))
            .thenThrow(NotFoundException.with(Wallet.class, expectedFromWalletId));

        // when
        final var request = post("/pix/transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", equalTo(expectedErrorMessage)));

        verify(createPixTransferUseCase, times(1)).execute(any());
    }
}

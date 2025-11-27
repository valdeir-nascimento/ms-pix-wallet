package br.com.pix.wallet.presentation.rest.controller.pix;

import br.com.pix.wallet.ControllerTest;
import br.com.pix.wallet.application.pix.register.RegisterPixKeyCommand;
import br.com.pix.wallet.application.pix.register.RegisterPixKeyOutput;
import br.com.pix.wallet.application.pix.register.RegisterPixKeyUseCase;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.exception.NotFoundException;
import br.com.pix.wallet.domain.pix.pixkey.PixKeyType;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.presentation.rest.controller.pix.request.RegisterPixKeyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = PixKeyController.class)
class PixKeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private RegisterPixKeyUseCase registerPixKeyUseCase;

    @Test
    void givenAValidCommand_whenCallsRegisterPixKey_thenShouldReturnPixKeyId() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID().toString();
        final var expectedKeyType = PixKeyType.EMAIL;
        final var expectedKeyValue = "test@test.com";
        final var expectedPixKeyId = UUID.randomUUID().toString();

        final var requestBody = new RegisterPixKeyRequest(expectedKeyType.name(), expectedKeyValue);

        final var expectedOutput = new RegisterPixKeyOutput(
            expectedPixKeyId,
            expectedWalletId,
            expectedKeyType.name(),
            expectedKeyValue
        );

        when(registerPixKeyUseCase.execute(any(RegisterPixKeyCommand.class))).thenReturn(expectedOutput);

        // when
        final var request = post("/wallets/{walletId}/pix-keys", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString(expectedPixKeyId)))
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.pixKeyId", equalTo(expectedPixKeyId)))
            .andExpect(jsonPath("$.walletId", equalTo(expectedWalletId)))
            .andExpect(jsonPath("$.keyType", equalTo(expectedKeyType.name())))
            .andExpect(jsonPath("$.keyValue", equalTo(expectedKeyValue)));

        verify(registerPixKeyUseCase, times(1))
            .execute(argThat(command -> Objects.equals(expectedWalletId, command.walletId())
                && Objects.equals(expectedKeyType.name(), command.keyType())
                && Objects.equals(expectedKeyValue, command.keyValue())));
    }

    @Test
    void givenAInvalidCommand_whenCallsRegisterPixKey_thenShouldReturnDomainException() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID().toString();
        final var expectedKeyType = PixKeyType.EMAIL;
        final var expectedKeyValue = "invalid-email";
        final var expectedMessage = "Invalid email";

        final var requestBody = new RegisterPixKeyRequest(expectedKeyType.name(), expectedKeyValue);

        when(registerPixKeyUseCase.execute(any(RegisterPixKeyCommand.class)))
            .thenThrow(DomainException.with(Error.of(expectedMessage)));

        // when
        final var request = post("/wallets/{walletId}/pix-keys", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isUnprocessableEntity())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.message", equalTo(expectedMessage)));

        verify(registerPixKeyUseCase, times(1)).execute(any());
    }

    @Test
    void givenAInvalidWalletId_whenCallsRegisterPixKey_thenShouldReturnNotFoundException() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID().toString();
        final var expectedKeyType = PixKeyType.EMAIL;
        final var expectedKeyValue = "test@test.com";
        final var expectedErrorMessage = "Wallet with ID %s was not found" .formatted(expectedWalletId);

        final var requestBody = new RegisterPixKeyRequest(expectedKeyType.name(), expectedKeyValue);

        when(registerPixKeyUseCase.execute(any(RegisterPixKeyCommand.class)))
            .thenThrow(NotFoundException.with(Wallet.class, expectedWalletId));

        // when
        final var request = post("/wallets/{walletId}/pix-keys", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", equalTo(expectedErrorMessage)));

        verify(registerPixKeyUseCase, times(1)).execute(any());
    }
}

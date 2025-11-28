package br.com.pix.wallet.presentation.rest.controller.wallet;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.pix.wallet.ControllerTest;
import br.com.pix.wallet.application.wallet.balance.GetBalanceOutput;
import br.com.pix.wallet.application.wallet.balance.GetBalanceUseCase;
import br.com.pix.wallet.application.wallet.create.CreateWalletCommand;
import br.com.pix.wallet.application.wallet.create.CreateWalletOutput;
import br.com.pix.wallet.application.wallet.create.CreateWalletUseCase;
import br.com.pix.wallet.application.wallet.deposit.DepositCommand;
import br.com.pix.wallet.application.wallet.deposit.DepositOutput;
import br.com.pix.wallet.application.wallet.deposit.DepositUseCase;
import br.com.pix.wallet.application.wallet.withdraw.WithdrawCommand;
import br.com.pix.wallet.application.wallet.withdraw.WithdrawOutput;
import br.com.pix.wallet.application.wallet.withdraw.WithdrawUseCase;
import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.exception.NotFoundException;
import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.wallet.Wallet;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.CreateWalletRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.DepositRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.WithdrawRequest;

@ControllerTest(controllers = WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private CreateWalletUseCase createWalletUseCase;

    @MockitoBean
    private GetBalanceUseCase getBalanceUseCase;

    @MockitoBean
    private DepositUseCase depositUseCase;

    @MockitoBean
    private WithdrawUseCase withdrawUseCase;

    @Test
    void givenAValidCommand_whenCallsCreateWallet_thenShouldWalletOutput() throws Exception {
        // given
        final var expectedOwnerId = "d34c31ee-bdff-4122-8d5c-72f7bef1e1b3";
        final var expectedWalletId = "56533401-e033-45c5-9941-6218cfce579f";
        final var requestBody = new CreateWalletRequest(expectedOwnerId);

        final var expectedOutput = CreateWalletOutput.from(UUID.fromString(expectedWalletId), expectedOwnerId);

        when(createWalletUseCase.execute(any(CreateWalletCommand.class))).thenReturn(expectedOutput);

        // when
        final var request = post("/wallets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString(expectedWalletId)))
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.walletId", equalTo(expectedWalletId)))
            .andExpect(jsonPath("$.ownerId", equalTo(expectedOwnerId)));

        verify(createWalletUseCase, times(1))
            .execute(argThat(command -> Objects.equals(expectedOwnerId, command.ownerId())));
    }

    @Test
    void givenAInvalidCommand_whenCallsCreateWallet_thenShouldReturnDomainException() throws Exception {
        // given
        final var expectedOwnerId = "d34c31ee-bdff-4122-8d5c-72f7bef1e1b3";
        final var expectedMessage = "'ownerId' should not be null";
        final var requestBody = new CreateWalletRequest(expectedOwnerId);

        when(createWalletUseCase.execute(any(CreateWalletCommand.class)))
            .thenThrow(DomainException.with(new Error(expectedMessage)));

        // when
        final var request = post("/wallets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isUnprocessableEntity())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.message", equalTo(expectedMessage)))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].message", equalTo(expectedMessage)));

        verify(createWalletUseCase, times(1))
            .execute(argThat(command -> Objects.equals(expectedOwnerId, command.ownerId())));
    }

    @Test
    void givenExistingOwnerId_whenCallsCreateWallet_thenShouldReturnDomainException() throws Exception {
        // given
        final var expectedOwnerId = "d34c31ee-bdff-4122-8d5c-72f7bef1e1b3";
        final var expectedMessage = "Owner ID already has a wallet";
        final var requestBody = new CreateWalletRequest(expectedOwnerId);

        when(createWalletUseCase.execute(any(CreateWalletCommand.class)))
            .thenThrow(DomainException.with(new Error(expectedMessage)));

        // when
        final var request = post("/wallets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isUnprocessableEntity())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.message", equalTo(expectedMessage)))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].message", equalTo(expectedMessage)));

        verify(createWalletUseCase, times(1))
            .execute(argThat(command -> Objects.equals(expectedOwnerId, command.ownerId())));
    }

    @Test
    void givenAValidId_whenCallsGetBalance_thenReturnBalance() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedBalance = BigDecimal.TEN;
        final var expectedOutput = GetBalanceOutput.from(expectedBalance);

        when(getBalanceUseCase.execute(eq(expectedWalletId), any())).thenReturn(expectedOutput);

        // when
        final var request = get("/wallets/{id}/balance", expectedWalletId)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isOk())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.currentBalance", equalTo(expectedBalance.intValue())));

        verify(getBalanceUseCase, times(1)).execute(eq(expectedWalletId), any());
    }

    @Test
    void givenAInvalidId_whenCallsGetBalance_thenReturnNotFound() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedErrorMessage = "Wallet with ID %s was not found" .formatted(expectedWalletId);

        when(getBalanceUseCase.execute(eq(expectedWalletId), any()))
            .thenThrow(NotFoundException.with(Wallet.class, expectedWalletId));

        // when
        final var request = get("/wallets/{id}/balance", expectedWalletId)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", equalTo(expectedErrorMessage)));

        verify(getBalanceUseCase, times(1)).execute(eq(expectedWalletId), any());
    }

    @Test
    void givenAValidCommand_whenCallsDeposit_thenShouldReturnNewBalance() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.TEN;
        final var expectedNewBalance = BigDecimal.valueOf(20);
        final var requestBody = new DepositRequest(expectedAmount);

        final var expectedOutput = DepositOutput.from(expectedWalletId, expectedNewBalance);

        when(depositUseCase.execute(any(DepositCommand.class))).thenReturn(expectedOutput);

        // when
        final var request = post("/wallets/{id}/deposit", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isOk())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.walletId", equalTo(expectedWalletId.toString())))
            .andExpect(jsonPath("$.newBalance", equalTo(expectedNewBalance.intValue())));

        verify(depositUseCase, times(1))
            .execute(argThat(command -> Objects.equals(expectedWalletId.toString(), command.walletId())
                && Objects.equals(expectedAmount, command.amount())));
    }

    @Test
    void givenAInvalidAmount_whenCallsDeposit_thenShouldReturnBadRequest() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.ZERO;
        final var requestBody = new DepositRequest(expectedAmount);

        // when
        final var request = post("/wallets/{id}/deposit", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isBadRequest())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE));

        verify(depositUseCase, never()).execute(any());
    }

    @Test
    void givenAInvalidId_whenCallsDeposit_thenReturnNotFound() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.TEN;
        final var requestBody = new DepositRequest(expectedAmount);
        final var expectedErrorMessage = "Wallet with ID %s was not found" .formatted(expectedWalletId);

        when(depositUseCase.execute(any(DepositCommand.class)))
            .thenThrow(NotFoundException.with(Wallet.class, expectedWalletId));

        // when
        final var request = post("/wallets/{id}/deposit", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", equalTo(expectedErrorMessage)));

        verify(depositUseCase, times(1)).execute(any());
    }

    @Test
    void givenAInvalidCommand_whenCallsDeposit_thenShouldReturnDomainException() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.TEN;
        final var requestBody = new DepositRequest(expectedAmount);
        final var expectedMessage = "Some domain error";

        when(depositUseCase.execute(any(DepositCommand.class)))
            .thenThrow(DomainException.with(new Error(expectedMessage)));

        // when
        final var request = post("/wallets/{id}/deposit", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isUnprocessableEntity())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.message", equalTo(expectedMessage)));

        verify(depositUseCase, times(1)).execute(any());
    }

    @Test
    void givenAValidCommand_whenCallsWithdraw_thenShouldReturnNewBalance() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.TEN;
        final var expectedNewBalance = BigDecimal.ZERO;
        final var requestBody = new WithdrawRequest(expectedAmount);

        final var expectedOutput = WithdrawOutput.from(expectedWalletId, expectedNewBalance);

        when(withdrawUseCase.execute(any(WithdrawCommand.class))).thenReturn(expectedOutput);

        // when
        final var request = post("/wallets/{id}/withdraw", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isOk())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.walletId", equalTo(expectedWalletId.toString())))
            .andExpect(jsonPath("$.newBalance", equalTo(expectedNewBalance.intValue())));

        verify(withdrawUseCase, times(1))
            .execute(argThat(command -> Objects.equals(expectedWalletId, command.walletId())
                && Objects.equals(expectedAmount, command.amount())));
    }

    @Test
    void givenAInvalidAmount_whenCallsWithdraw_thenShouldReturnBadRequest() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.ZERO;
        final var requestBody = new WithdrawRequest(expectedAmount);

        // when
        final var request = post("/wallets/{id}/withdraw", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isBadRequest())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE));

        verify(withdrawUseCase, never()).execute(any());
    }

    @Test
    void givenAInvalidId_whenCallsWithdraw_thenReturnNotFound() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.TEN;
        final var requestBody = new WithdrawRequest(expectedAmount);
        final var expectedErrorMessage = "Wallet with ID %s was not found" .formatted(expectedWalletId);

        when(withdrawUseCase.execute(any(WithdrawCommand.class)))
            .thenThrow(NotFoundException.with(Wallet.class, expectedWalletId));

        // when
        final var request = post("/wallets/{id}/withdraw", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", equalTo(expectedErrorMessage)));

        verify(withdrawUseCase, times(1)).execute(any());
    }

    @Test
    void givenAInvalidCommand_whenCallsWithdraw_thenShouldReturnDomainException() throws Exception {
        // given
        final var expectedWalletId = UUID.randomUUID();
        final var expectedAmount = BigDecimal.TEN;
        final var requestBody = new WithdrawRequest(expectedAmount);
        final var expectedMessage = "Insufficient funds";

        when(withdrawUseCase.execute(any(WithdrawCommand.class)))
            .thenThrow(DomainException.with(new Error(expectedMessage)));

        // when
        final var request = post("/wallets/{id}/withdraw", expectedWalletId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.mapper.writeValueAsString(requestBody));

        final var response = this.mockMvc.perform(request).andDo(print());

        // then
        response.andExpect(status().isUnprocessableEntity())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.message", equalTo(expectedMessage)));

        verify(withdrawUseCase, times(1)).execute(any());
    }
}
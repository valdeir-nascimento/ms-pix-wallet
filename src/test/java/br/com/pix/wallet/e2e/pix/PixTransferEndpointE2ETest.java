package br.com.pix.wallet.e2e.pix;

import br.com.pix.wallet.E2ETest;
import br.com.pix.wallet.presentation.rest.controller.pix.request.CreatePixTransferRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.CreateWalletRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.DepositRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@E2ETest
@Testcontainers
class PixTransferEndpointE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("pix_wallet_db")
        .withUsername("pix_user")
        .withPassword("pix_password");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES_CONTAINER::getDriverClassName);
        registry.add("spring.flyway.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.flyway.password", POSTGRES_CONTAINER::getPassword);
    }

    private String createWallet() throws Exception {
        final var ownerId = UUID.randomUUID().toString();
        final var request = new CreateWalletRequest(ownerId);
        final var result = this.mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        final var responseJson = this.mapper.readTree(result.getResponse().getContentAsString());
        return responseJson.get("walletId").asText();
    }

    private void deposit(String walletId, BigDecimal amount) throws Exception {
        final var depositRequest = new DepositRequest(amount);
        this.mockMvc.perform(post("/wallets/{id}/deposit", walletId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(depositRequest)))
            .andExpect(status().isOk());
    }

    @Test
    void testCreatePixTransfer() throws Exception {
        final var fromWalletId = createWallet();
        final var toWalletId = createWallet();
        deposit(fromWalletId, BigDecimal.valueOf(100));

        final var amount = BigDecimal.valueOf(50);
        final var idempotencyKey = UUID.randomUUID().toString();
        final var endToEndId = UUID.randomUUID().toString();

        final var request = new CreatePixTransferRequest(
            fromWalletId,
            toWalletId,
            amount,
            idempotencyKey,
            endToEndId
        );

        this.mockMvc.perform(post("/pix/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.transferId", notNullValue()))
            .andExpect(jsonPath("$.fromWalletId", equalTo(fromWalletId)))
            .andExpect(jsonPath("$.toWalletId", equalTo(toWalletId)))
            .andExpect(jsonPath("$.amount", equalTo(50)))
            .andExpect(jsonPath("$.status", notNullValue()));
    }
}

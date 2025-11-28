package br.com.pix.wallet.e2e.pix;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.pix.wallet.E2ETest;
import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventType;
import br.com.pix.wallet.presentation.rest.controller.pix.request.CreatePixTransferRequest;
import br.com.pix.wallet.presentation.rest.controller.pix.request.HandlePixWebhookRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.CreateWalletRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.DepositRequest;

@E2ETest
@Testcontainers
class PixWebhookEndpointE2ETest {

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

    @Test
    void testHandleWebhook() throws Exception {
        final var fromWalletId = createWallet();
        final var toWalletId = createWallet();
        deposit(fromWalletId, BigDecimal.valueOf(100));

        final var endToEndId = UUID.randomUUID().toString();
        createPixTransfer(fromWalletId, toWalletId, BigDecimal.valueOf(50), endToEndId);

        final var eventId = UUID.randomUUID().toString();
        final var eventType = PixWebhookEventType.CREDIT_CONFIRMED.name();
        final var occurredAt = Instant.now();

        final var request = new HandlePixWebhookRequest(
            eventId,
            endToEndId,
            eventType,
            occurredAt
        );

        this.mockMvc.perform(post("/pix/webhooks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.webhookEventId", notNullValue()))
            .andExpect(jsonPath("$.eventId", equalTo(eventId)))
            .andExpect(jsonPath("$.endToEndId", equalTo(endToEndId)))
            .andExpect(jsonPath("$.eventType", equalTo(eventType)));
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

    private void deposit(final String walletId, final BigDecimal amount) throws Exception {
        final var depositRequest = new DepositRequest(amount);
        this.mockMvc.perform(post("/wallets/{id}/deposit", walletId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(depositRequest)))
            .andExpect(status().isOk());
    }

    private void createPixTransfer(
        final String fromWalletId,
        final String toWalletId,
        final BigDecimal amount,
        final String endToEndId
    ) throws Exception {
        final var request = new CreatePixTransferRequest(
            fromWalletId,
            toWalletId,
            amount,
            UUID.randomUUID().toString(),
            endToEndId
        );

        this.mockMvc.perform(post("/pix/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}

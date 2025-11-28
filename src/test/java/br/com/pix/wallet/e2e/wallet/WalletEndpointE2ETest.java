package br.com.pix.wallet.e2e.wallet;

import br.com.pix.wallet.E2ETest;
import br.com.pix.wallet.infrastructure.persistence.repository.WalletJpaRepository;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.CreateWalletRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.DepositRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.WithdrawRequest;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@E2ETest
@Testcontainers
class WalletEndpointE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletJpaRepository walletJpaRepository;

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
    void testCreateWallet() throws Exception {
        final var ownerId = UUID.randomUUID().toString();
        final var request = new CreateWalletRequest(ownerId);

        final var result = this.mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.walletId", notNullValue()))
            .andExpect(jsonPath("$.ownerId", equalTo(ownerId)))
            .andReturn();

        final var responseJson = this.mapper.readTree(result.getResponse().getContentAsString());
        final var walletId = responseJson.get("walletId").asText();

        final var wallet = walletJpaRepository.findById(UUID.fromString(walletId));
        if (wallet.isEmpty()) {
            throw new AssertionError("Wallet not found in database");
        }
    }

    @Test
    void testGetBalance() throws Exception {
        final var ownerId = UUID.randomUUID().toString();
        final var createRequest = new CreateWalletRequest(ownerId);

        final var createResult = this.mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        final var responseJson = this.mapper.readTree(createResult.getResponse().getContentAsString());
        final var walletId = responseJson.get("walletId").asText();

        this.mockMvc.perform(get("/wallets/{id}/balance", walletId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentBalance", equalTo(0.0)));
    }

    @Test
    void testDeposit() throws Exception {
        final var ownerId = UUID.randomUUID().toString();
        final var createRequest = new CreateWalletRequest(ownerId);

        final var createResult = this.mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        final var responseJson = this.mapper.readTree(createResult.getResponse().getContentAsString());
        final var walletId = responseJson.get("walletId").asText();

        final var depositAmount = BigDecimal.valueOf(100.50);
        final var depositRequest = new DepositRequest(depositAmount);

        this.mockMvc.perform(post("/wallets/{id}/deposit", walletId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(depositRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.walletId", equalTo(walletId)))
            .andExpect(jsonPath("$.newBalance", equalTo(100.5)));

        final var wallet = walletJpaRepository.findById(UUID.fromString(walletId)).orElseThrow();
        if (wallet.getCurrentBalance().compareTo(depositAmount) != 0) {
            throw new AssertionError("Balance not updated correctly in database");
        }
    }

    @Test
    void testWithdraw() throws Exception {
        final var ownerId = UUID.randomUUID().toString();
        final var createRequest = new CreateWalletRequest(ownerId);

        final var createResult = this.mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        final var responseJson = this.mapper.readTree(createResult.getResponse().getContentAsString());
        final var walletId = responseJson.get("walletId").asText();

        final var depositAmount = BigDecimal.valueOf(100.00);
        final var depositRequest = new DepositRequest(depositAmount);

        this.mockMvc.perform(post("/wallets/{id}/deposit", walletId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(depositRequest)))
            .andExpect(status().isOk());

        final var withdrawAmount = BigDecimal.valueOf(50.00);
        final var withdrawRequest = new WithdrawRequest(withdrawAmount);

        this.mockMvc.perform(post("/wallets/{id}/withdraw", walletId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(withdrawRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.walletId", equalTo(walletId)))
            .andExpect(jsonPath("$.newBalance", equalTo(50.0)));

        final var wallet = walletJpaRepository.findById(UUID.fromString(walletId)).orElseThrow();
        if (wallet.getCurrentBalance().compareTo(BigDecimal.valueOf(50.00)) != 0) {
            throw new AssertionError("Balance not updated correctly in database");
        }
    }
}

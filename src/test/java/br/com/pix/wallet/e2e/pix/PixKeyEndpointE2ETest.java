package br.com.pix.wallet.e2e.pix;

import br.com.pix.wallet.E2ETest;
import br.com.pix.wallet.domain.pix.pixkey.PixKeyType;
import br.com.pix.wallet.presentation.rest.controller.pix.request.RegisterPixKeyRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.CreateWalletRequest;
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

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@E2ETest
@Testcontainers
class PixKeyEndpointE2ETest {

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
    void testRegisterPixKey() throws Exception {
        final var walletId = createWallet();
        final var keyType = PixKeyType.EMAIL;
        final var keyValue = "test@test.com";
        final var request = new RegisterPixKeyRequest(keyType.name(), keyValue);

        this.mockMvc.perform(post("/wallets/{walletId}/pix-keys", walletId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.pixKeyId", notNullValue()))
            .andExpect(jsonPath("$.walletId", equalTo(walletId)))
            .andExpect(jsonPath("$.keyType", equalTo(keyType.name())))
            .andExpect(jsonPath("$.keyValue", equalTo(keyValue)));
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
}

package br.com.pix.wallet.presentation.rest.controller.security;

import br.com.pix.wallet.ControllerTest;
import br.com.pix.wallet.application.security.auth.AuthenticateUserCommand;
import br.com.pix.wallet.application.security.auth.AuthenticateUserOutput;
import br.com.pix.wallet.application.security.auth.AuthenticateUserUseCase;
import br.com.pix.wallet.application.security.register.RegisterUserCommand;
import br.com.pix.wallet.application.security.register.RegisterUserOutput;
import br.com.pix.wallet.application.security.register.RegisterUserUseCase;
import br.com.pix.wallet.domain.user.UserRole;
import br.com.pix.wallet.presentation.rest.controller.security.request.AuthenticateUserRequest;
import br.com.pix.wallet.presentation.rest.controller.security.request.RegisterUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Test
    void givenRegisterRequest_whenCallsEndpoint_thenReturnUser() throws Exception {
        final var request = new RegisterUserRequest("operator", "secret", Set.of("OPERATOR"));
        final var expectedOutput = new RegisterUserOutput("id", "operator", Set.of(UserRole.OPERATOR));
        when(registerUserUseCase.execute(any(RegisterUserCommand.class))).thenReturn(expectedOutput);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", equalTo("operator")));

        verify(registerUserUseCase, times(1)).execute(any(RegisterUserCommand.class));
    }

    @Test
    void givenLoginRequest_whenCallsEndpoint_thenReturnToken() throws Exception {
        final var request = new AuthenticateUserRequest("operator", "secret");
        final var expectedOutput = new AuthenticateUserOutput("token", Instant.now());
        when(authenticateUserUseCase.execute(any(AuthenticateUserCommand.class))).thenReturn(expectedOutput);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", equalTo("token")));

        verify(authenticateUserUseCase, times(1)).execute(any(AuthenticateUserCommand.class));
    }
}


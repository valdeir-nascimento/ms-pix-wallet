package br.com.pix.wallet.presentation.rest.controller.security;

import br.com.pix.wallet.application.security.auth.AuthenticateUserCommand;
import br.com.pix.wallet.application.security.auth.AuthenticateUserUseCase;
import br.com.pix.wallet.application.security.register.RegisterUserCommand;
import br.com.pix.wallet.application.security.register.RegisterUserUseCase;
import br.com.pix.wallet.presentation.rest.controller.security.request.AuthenticateUserRequest;
import br.com.pix.wallet.presentation.rest.controller.security.request.RegisterUserRequest;
import br.com.pix.wallet.presentation.rest.controller.security.response.AuthenticateUserResponse;
import br.com.pix.wallet.presentation.rest.controller.security.response.RegisterUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(
        final RegisterUserUseCase registerUserUseCase,
        final AuthenticateUserUseCase authenticateUserUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody final RegisterUserRequest request) {
        final var command = RegisterUserCommand.with(request.username(), request.password(), request.roles());
        final var output = registerUserUseCase.execute(command);
        return ResponseEntity.ok(RegisterUserResponse.from(output));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticateUserResponse> login(@Valid @RequestBody final AuthenticateUserRequest request) {
        final var command = AuthenticateUserCommand.with(request.username(), request.password());
        final var output = authenticateUserUseCase.execute(command);
        return ResponseEntity.ok(AuthenticateUserResponse.from(output));
    }
}


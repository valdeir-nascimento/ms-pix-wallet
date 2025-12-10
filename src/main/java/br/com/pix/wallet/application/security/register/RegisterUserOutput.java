package br.com.pix.wallet.application.security.register;

import br.com.pix.wallet.domain.user.UserAccount;
import br.com.pix.wallet.domain.user.UserRole;

import java.util.Set;

public record RegisterUserOutput(
    String userId,
    String username,
    Set<UserRole> roles
) {

    public static RegisterUserOutput from(final UserAccount account) {
        return new RegisterUserOutput(
            account.getId().getValue().toString(),
            account.getUsername(),
            account.getRoles()
        );
    }
}


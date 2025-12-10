package br.com.pix.wallet.domain.user;

import br.com.pix.wallet.domain.validation.Error;
import br.com.pix.wallet.domain.validation.ValidationHandler;
import br.com.pix.wallet.domain.validation.Validator;

public class UserAccountValidator extends Validator {

    private final UserAccount userAccount;

    public UserAccountValidator(final UserAccount userAccount, final ValidationHandler handler) {
        super(handler);
        this.userAccount = userAccount;
    }

    @Override
    public void validate() {
        if (userAccount.getUsername() == null || userAccount.getUsername().isBlank()) {
            validationHandler().append(Error.of("'username' must not be null or blank"));
        }

        if (userAccount.getPassword() == null || userAccount.getPassword().isBlank()) {
            validationHandler().append(Error.of("'password' must not be null or blank"));
        }

        if (userAccount.getRoles() == null || userAccount.getRoles().isEmpty()) {
            validationHandler().append(Error.of("'roles' must contain at least one role"));
        }
    }
}


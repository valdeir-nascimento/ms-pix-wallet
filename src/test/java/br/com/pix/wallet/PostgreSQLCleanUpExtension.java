package br.com.pix.wallet;


import br.com.pix.wallet.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import br.com.pix.wallet.infrastructure.persistence.repository.PixKeyJpaRepository;
import br.com.pix.wallet.infrastructure.persistence.repository.WalletJpaRepository;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.List;

public class PostgreSQLCleanUpExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(final ExtensionContext context) {
        final var appContext = SpringExtension.getApplicationContext(context);
        cleanUp(List.of(
            appContext.getBean(LedgerEntryJpaRepository.class),
            appContext.getBean(PixKeyJpaRepository.class),
            appContext.getBean(WalletJpaRepository.class)
        ));
    }

    private void cleanUp(final Collection<CrudRepository> repositories) {
        repositories.forEach(CrudRepository::deleteAll);
    }
}


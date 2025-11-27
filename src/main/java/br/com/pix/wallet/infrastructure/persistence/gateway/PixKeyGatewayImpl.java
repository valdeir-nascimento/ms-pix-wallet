package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.domain.pix.pixkey.PixKeyGateway;
import br.com.pix.wallet.domain.pix.pixkey.PixKey;
import br.com.pix.wallet.infrastructure.persistence.entity.PixKeyEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.PixKeyJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PixKeyGatewayImpl implements PixKeyGateway {

    private final PixKeyJpaRepository pixKeyJpaRepository;

    public PixKeyGatewayImpl(final PixKeyJpaRepository pixKeyJpaRepository) {
        this.pixKeyJpaRepository = pixKeyJpaRepository;
    }

    @Override
    @Transactional
    public PixKey save(final PixKey pixKey) {
        final var entity = PixKeyEntity.from(pixKey);
        final var saved = pixKeyJpaRepository.save(entity);
        return saved.toAggregate();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByKeyValue(final String keyValue) {
        return pixKeyJpaRepository.existsByKeyValue(keyValue);
    }
}

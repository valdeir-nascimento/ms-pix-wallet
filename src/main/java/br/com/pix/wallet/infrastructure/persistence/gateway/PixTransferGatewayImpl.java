package br.com.pix.wallet.infrastructure.persistence.gateway;

import br.com.pix.wallet.domain.pix.transfer.PixTransferGateway;
import br.com.pix.wallet.domain.pix.transfer.PixTransfer;
import br.com.pix.wallet.infrastructure.persistence.entity.PixTransferEntity;
import br.com.pix.wallet.infrastructure.persistence.repository.PixTransferJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class PixTransferGatewayImpl implements PixTransferGateway {

    private final PixTransferJpaRepository pixTransferJpaRepository;

    public PixTransferGatewayImpl(final PixTransferJpaRepository pixTransferJpaRepository) {
        this.pixTransferJpaRepository = pixTransferJpaRepository;
    }

    @Override
    @Transactional
    public PixTransfer save(final PixTransfer transfer) {
        final var entity = PixTransferEntity.from(transfer);
        final var saved = pixTransferJpaRepository.save(entity);
        return saved.toAggregate();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PixTransfer> findByEndToEndId(final String endToEndId) {
        return pixTransferJpaRepository.findByEndToEndId(endToEndId)
                .map(PixTransferEntity::toAggregate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdempotencyKey(final String idempotencyKey) {
        return pixTransferJpaRepository.existsByIdempotencyKey(idempotencyKey);
    }
}

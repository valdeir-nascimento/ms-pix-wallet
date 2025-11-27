package br.com.pix.wallet.domain.pix.pixkey;

public interface PixKeyGateway {
    PixKey save(PixKey pixKey);

    boolean existsByKeyValue(String keyValue);
}
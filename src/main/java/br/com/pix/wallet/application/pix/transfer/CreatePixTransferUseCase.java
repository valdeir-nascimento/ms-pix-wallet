package br.com.pix.wallet.application.pix.transfer;

public interface CreatePixTransferUseCase {
    CreatePixTransferOutput execute(CreatePixTransferCommand command);
}

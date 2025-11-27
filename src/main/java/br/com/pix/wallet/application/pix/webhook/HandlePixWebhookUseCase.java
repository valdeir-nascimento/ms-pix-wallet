package br.com.pix.wallet.application.pix.webhook;

public interface HandlePixWebhookUseCase {
    HandlePixWebhookOutput execute(HandlePixWebhookCommand command);
}
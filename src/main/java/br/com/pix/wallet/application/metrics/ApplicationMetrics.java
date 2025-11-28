package br.com.pix.wallet.application.metrics;

import br.com.pix.wallet.domain.pix.webhook.PixWebhookEventType;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ApplicationMetrics {

    private final MeterRegistry meterRegistry;
    private final DistributionSummary pixTransferAmountSummary;
    private final Timer pixTransferTimer;
    private final DistributionSummary depositAmountSummary;
    private final DistributionSummary withdrawAmountSummary;

    public ApplicationMetrics(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.pixTransferAmountSummary = DistributionSummary.builder("pix_wallet_pix_transfer_amount")
            .description("Distribution of Pix transfer amounts")
            .baseUnit("BRL")
            .publishPercentileHistogram()
            .register(meterRegistry);

        this.pixTransferTimer = Timer.builder("pix_wallet_pix_transfer_duration_seconds")
            .description("Time spent creating Pix transfers")
            .publishPercentileHistogram()
            .register(meterRegistry);

        this.depositAmountSummary = DistributionSummary.builder("pix_wallet_deposit_amount")
            .description("Distribution of deposit amounts")
            .baseUnit("BRL")
            .publishPercentileHistogram()
            .register(meterRegistry);

        this.withdrawAmountSummary = DistributionSummary.builder("pix_wallet_withdraw_amount")
            .description("Distribution of withdraw amounts")
            .baseUnit("BRL")
            .publishPercentileHistogram()
            .register(meterRegistry);
    }

    public void recordWalletCreation(final boolean success) {
        meterRegistry.counter("pix_wallet_wallet_creation_total", "status", success ? "success" : "failure")
            .increment();
    }

    public void recordDepositOperation(final boolean success, final BigDecimal amount) {
        meterRegistry.counter("pix_wallet_deposit_operations_total", "status", success ? "success" : "failure")
            .increment();
        if (success && amount != null) {
            depositAmountSummary.record(amount.doubleValue());
        }
    }

    public void recordWithdrawOperation(final boolean success, final BigDecimal amount) {
        meterRegistry.counter("pix_wallet_withdraw_operations_total", "status", success ? "success" : "failure")
            .increment();
        if (success && amount != null) {
            withdrawAmountSummary.record(amount.doubleValue());
        }
    }

    public Timer.Sample startPixTransferTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordPixTransferOutcome(
        final Timer.Sample sample,
        final boolean success,
        final BigDecimal amount
    ) {
        meterRegistry.counter("pix_wallet_pix_transfer_total", "status", success ? "success" : "failure")
            .increment();

        if (success && amount != null) {
            pixTransferAmountSummary.record(amount.doubleValue());
        }

        if (sample != null) {
            sample.stop(pixTransferTimer);
        }
    }

    public void recordPixWebhookEvent(final PixWebhookEventType type, final String status) {
        final var safeType = type == null ? "UNKNOWN" : type.name();
        meterRegistry.counter("pix_wallet_webhook_events_total", "eventType", safeType, "status", status)
            .increment();
    }
}


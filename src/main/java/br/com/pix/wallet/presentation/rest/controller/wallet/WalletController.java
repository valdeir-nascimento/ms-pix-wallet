package br.com.pix.wallet.presentation.rest.controller.wallet;

import br.com.pix.wallet.application.wallet.balance.GetBalanceOutput;
import br.com.pix.wallet.application.wallet.balance.GetBalanceUseCase;
import br.com.pix.wallet.application.wallet.create.CreateWalletCommand;
import br.com.pix.wallet.application.wallet.create.CreateWalletOutput;
import br.com.pix.wallet.application.wallet.create.CreateWalletUseCase;
import br.com.pix.wallet.application.wallet.deposit.DepositCommand;
import br.com.pix.wallet.application.wallet.deposit.DepositOutput;
import br.com.pix.wallet.application.wallet.deposit.DepositUseCase;
import br.com.pix.wallet.application.wallet.withdraw.WithdrawCommand;
import br.com.pix.wallet.application.wallet.withdraw.WithdrawOutput;
import br.com.pix.wallet.application.wallet.withdraw.WithdrawUseCase;
import br.com.pix.wallet.presentation.rest.controller.wallet.openapi.WalletEndpointOpenApi;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.CreateWalletRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.DepositRequest;
import br.com.pix.wallet.presentation.rest.controller.wallet.request.WithdrawRequest;
import br.com.pix.wallet.presentation.rest.helper.ApiUriFactory;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController implements WalletEndpointOpenApi {

    private final CreateWalletUseCase createWalletUseCase;
    private final GetBalanceUseCase getBalanceUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;

    public WalletController(
        final CreateWalletUseCase createWalletUseCase,
        final GetBalanceUseCase getBalanceUseCase,
        final DepositUseCase depositUseCase,
        final WithdrawUseCase withdrawUseCase
    ) {
        this.createWalletUseCase = createWalletUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateWalletOutput> createWallet(@RequestBody @Valid final CreateWalletRequest request) {
        final var command = CreateWalletCommand.with(request.ownerId());
        final var output = createWalletUseCase.execute(command);
        final var location = ApiUriFactory.createdLocation("/wallets/{walletId}", output.walletId());
        return ResponseEntity.created(location).body(output);
    }

    @Override
    @GetMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<GetBalanceOutput> getBalance(
        @PathVariable("id") final UUID walletId,
        @RequestParam(value = "at", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final Instant at
    ) {
        final var output = getBalanceUseCase.execute(walletId, at);
        return ResponseEntity.ok(output);
    }

    @Override
    @PostMapping("/{id}/deposit")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<DepositOutput> deposit(@PathVariable("id") final String walletId, @RequestBody @Valid final DepositRequest request) {
        final var command = DepositCommand.with(walletId, request.amount());
        final var output = depositUseCase.execute(command);
        return ResponseEntity.ok(output);
    }

    @Override
    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<WithdrawOutput> withdraw(@PathVariable("id") final UUID walletId, @RequestBody @Valid final WithdrawRequest request) {
        final var command = WithdrawCommand.with(walletId, request.amount());
        final var output = withdrawUseCase.execute(command);
        return ResponseEntity.ok(output);
    }
}

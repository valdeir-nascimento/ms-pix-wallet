package br.com.pix.wallet.domain.exception;

import br.com.pix.wallet.domain.core.AggregateRoot;
import br.com.pix.wallet.domain.core.Identifier;
import  br.com.pix.wallet.domain.validation.Error;

import java.util.Collections;
import java.util.List;

public class NotFoundException extends DomainException {

    protected NotFoundException(final String aMessage, final List<Error> anErrors) {
        super(aMessage, anErrors);
    }

    public static NotFoundException with(
        final Class<? extends AggregateRoot<?>> anAggregate,
        final Identifier<?> id
    ) {
        final var anError = "%s with ID %s was not found".formatted(anAggregate.getSimpleName(), id.getValue());
        return new NotFoundException(anError, Collections.emptyList());
    }

    public static NotFoundException with(
        final Class<?> anEntity,
        final Object id
    ) {
        final var anError = "%s with ID %s was not found".formatted(
            anEntity.getSimpleName(),
            id
        );
        return new NotFoundException(anError, Collections.emptyList());
    }
}

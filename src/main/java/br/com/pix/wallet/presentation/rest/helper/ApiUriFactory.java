package br.com.pix.wallet.presentation.rest.helper;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public final class ApiUriFactory {

    private ApiUriFactory() {
    }

    public static URI createdLocation(final String pathTemplate, final Object... uriVariables) {
        return ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path(pathTemplate)
            .buildAndExpand(uriVariables)
            .toUri();
    }
}

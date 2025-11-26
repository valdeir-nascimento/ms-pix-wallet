package br.com.pix.wallet.domain.core;

public abstract class Identifier<T> extends ValueObject {

    public abstract T getValue();

}

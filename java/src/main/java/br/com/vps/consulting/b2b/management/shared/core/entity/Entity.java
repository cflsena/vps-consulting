package br.com.vps.consulting.b2b.management.shared.core.entity;

public abstract class Entity<I extends Identifier<?>> {
    protected abstract void validate();
}

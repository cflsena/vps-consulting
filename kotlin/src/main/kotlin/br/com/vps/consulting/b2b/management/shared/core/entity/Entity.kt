package br.com.vps.consulting.b2b.management.shared.core.entity

abstract class Entity<I : Identifier<*>>(val id: I) {
    abstract fun validate()
}

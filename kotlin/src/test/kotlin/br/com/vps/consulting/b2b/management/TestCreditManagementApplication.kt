package br.com.vps.consulting.b2b.management

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<CreditManagementApplication>().with(TestcontainersConfiguration::class).run(*args)
}

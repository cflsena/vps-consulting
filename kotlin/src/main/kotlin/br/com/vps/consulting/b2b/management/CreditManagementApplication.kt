package br.com.vps.consulting.b2b.management

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class CreditManagementApplication

fun main(args: Array<String>) {
	runApplication<CreditManagementApplication>(*args)
}

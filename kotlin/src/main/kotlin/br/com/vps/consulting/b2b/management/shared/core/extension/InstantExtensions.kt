package br.com.vps.consulting.b2b.management.shared.core.extension

import br.com.vps.consulting.b2b.management.shared.core.utils.ManagementConstants.BRASILIA_TIME_ZONE
import java.time.Instant
import java.time.OffsetDateTime

fun Instant.toBrazilianOffsetDateTime(): OffsetDateTime {
    return this.atOffset(BRASILIA_TIME_ZONE)
}
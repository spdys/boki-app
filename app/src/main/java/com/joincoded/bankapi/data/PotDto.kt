package com.joincoded.bankapi.data

import java.math.BigDecimal

data class PotSummaryDto(
    val potId: Long,
    val name: String,
    val balance: BigDecimal,
    val cardToken: String?,
    val allocationType: AllocationType,
    val allocationValue: BigDecimal
)

data class PotRequest(
    val name: String,
    val allocationType: AllocationType,
    val allocationValue: BigDecimal
)

data class PotResponse(
    val potId: Long,
    val name: String,
    val balance: BigDecimal,
    val allocationType: AllocationType,
    val allocationValue: BigDecimal
)

enum class AllocationType {
    FIXED,
    PERCENTAGE
}
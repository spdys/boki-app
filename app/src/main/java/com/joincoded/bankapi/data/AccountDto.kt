package com.joincoded.bankapi.data

import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateAccountRequest(
    // val userId: Long,
    val accountType: AccountType
)

data class AccountResponse(
    val id: Long,
    val accountNumber: String,
    val accountType: AccountType,
    val balance: BigDecimal,
    val createdAt: String
)

data class CloseAccountResponse(
    val accountId: Long,
    val accountNumber: String,
    val accountType: AccountType,
    val isActive: Boolean
)

data class AccountSummaryDto(
    val accountId: Long,
    val accountNumber: String,
    val accountType: AccountType,
    val balance: BigDecimal,
    val cardNumber: String?,
    val currency: String,
    val isActive: Boolean,
    val pots: List<PotSummaryDto>? = null
)

enum class AccountType {
    MAIN,
    SAVINGS
}
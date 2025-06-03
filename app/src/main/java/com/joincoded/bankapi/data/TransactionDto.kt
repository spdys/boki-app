package com.joincoded.bankapi.data

import java.math.BigDecimal
import java.time.LocalDateTime

data class DepositSalaryRequest(
    val destinationId: Long,
    val amount: BigDecimal,
)

data class DepositSalaryResponse(
    val destinationId: Long,
    val balanceBefore: BigDecimal,
    val balanceAfter: BigDecimal,
)

data class PotTransferRequest(
    val sourcePotId: Long,
    val amount: BigDecimal
)

data class PotTransferResponse(
    val newPotBalance: BigDecimal,
    val newAccountBalance: BigDecimal,
)

data class PotDepositRequest(
    val sourceAccountId: Long,
    val destinationPotId: Long,
    val amount: BigDecimal,
)

data class PotDepositResponse(
    val newPotBalance: BigDecimal,
    val newAccountBalance: BigDecimal
)

data class TransactionHistoryRequest(
    val cardId: Long? = null,
    val accountId: Long? = null,
    val potId: Long? = null,
)

enum class TransactionType{
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    PURCHASE,
}

data class TransactionHistoryResponse(
    val id: Long,
    val amount: BigDecimal,
    val transactionType: String,
    val description: String?,
    val createdAt: LocalDateTime
)

data class TransferRequest(
    val sourceId: Long,
    val destinationId: Long,
    val amount: BigDecimal
)

data class TransferResponse(
    val sourceId: Long,
    val newBalanceAfter: BigDecimal,
)
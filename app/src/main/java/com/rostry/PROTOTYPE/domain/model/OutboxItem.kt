package com.rostry.prototype.domain.model

data class OutboxItem(
    val outboxId: Long,
    val entityType: String,
    val entityId: Long,
    val payloadJson: String,
    val status: String,
    val createdAt: Long
)

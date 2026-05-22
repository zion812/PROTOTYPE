package com.rostry.prototype.domain.model

data class OutboxItem(
    val outboxId: String,
    val entityType: String,
    val entityId: String,
    val payloadJson: String,
    val status: String,
    val createdAt: Long
)

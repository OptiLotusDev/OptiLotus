package dev.optilotus.app.domain

import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class BlockId(val value: String) {
    companion object {
        fun random(): BlockId = BlockId(Uuid.random().toString())
    }
}

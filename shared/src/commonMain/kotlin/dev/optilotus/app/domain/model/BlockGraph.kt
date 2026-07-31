package dev.optilotus.app.domain.model

import dev.optilotus.app.domain.BlockId

data class Connection(
    val fromBlockId: BlockId,
    val toBlockId: BlockId,
    val slotName: String? = null
)

data class BlockGraph(
    val entryPointBlockId: BlockId,
    val nodes: Map<BlockId, BlockAstNode>,
    val connections: List<Connection>
) {
    fun nextBlockAfter(currentBlockId: BlockId, viaSlotNamed: String? = null): BlockId? {
        return connections.find {
            it.fromBlockId == currentBlockId && it.slotName == viaSlotNamed
        }?.toBlockId
    }
}

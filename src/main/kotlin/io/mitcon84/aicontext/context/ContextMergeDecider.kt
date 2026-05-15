package io.mitcon84.aicontext.context

class ContextMergeDecider {
    fun decide(existingItems: List<ContextItem>, newItem: ContextItem): MergeDecision {
        val newStart = newItem.startLine ?: return MergeDecision.AddNew
        val newEnd = newItem.endLine ?: return MergeDecision.AddNew

        val sameFileItems = existingItems.filter { existing ->
            existing.filePath == newItem.filePath &&
                existing.startLine != null &&
                existing.endLine != null
        }

        sameFileItems.firstOrNull { existing ->
            newStart >= existing.startLine!! && newEnd <= existing.endLine!!
        }?.let { return MergeDecision.SkipAlreadyCovered(it) }

        val mergeItems = sameFileItems.filter { existing ->
            rangesTouchOrOverlap(
                existing.startLine!!,
                existing.endLine!!,
                newStart,
                newEnd
            )
        }

        if (mergeItems.isEmpty()) {
            return MergeDecision.AddNew
        }

        val unionStart = (mergeItems.mapNotNull { it.startLine } + newStart).min()
        val unionEnd = (mergeItems.mapNotNull { it.endLine } + newEnd).max()
        return MergeDecision.Merge(
            itemsToMerge = mergeItems,
            startLine = unionStart,
            endLine = unionEnd
        )
    }

    private fun rangesTouchOrOverlap(
        existingStart: Int,
        existingEnd: Int,
        newStart: Int,
        newEnd: Int
    ): Boolean = newStart <= existingEnd + 1 && newEnd >= existingStart - 1
}

sealed class MergeDecision {
    data object AddNew : MergeDecision()
    data class SkipAlreadyCovered(val coveringItem: ContextItem) : MergeDecision()
    data class Merge(
        val itemsToMerge: List<ContextItem>,
        val startLine: Int,
        val endLine: Int
    ) : MergeDecision()
}

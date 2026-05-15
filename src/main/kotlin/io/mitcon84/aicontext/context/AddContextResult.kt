package io.mitcon84.aicontext.context

sealed class AddContextResult {
    data class Added(val item: ContextItem) : AddContextResult()
    data class Merged(val item: ContextItem) : AddContextResult()
    data class SkippedAlreadyCovered(val coveringItem: ContextItem) : AddContextResult()
}

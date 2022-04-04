package io.posidon.android.libduckduckgo

class InstantAnswer internal constructor(
    val title: String,
    val sourceName: String,
    val sourceUrl: String,
    val description: String,
    val infoTable: Array<InfoboxEntry>?,
    val rawData: String
)
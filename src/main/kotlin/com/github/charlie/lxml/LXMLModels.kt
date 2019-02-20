package com.github.charlie.lxml

data class Lyrics(
    val title: String?,
    val artist: List<String>,
    val album: String?,
    val language: String?,
    val offsetMs: Int,
    val lines: List<LyricLine>
)

data class LyricLine(
    val timeMs: Int,
    val text: RichLyricText,
    val timeInCharacters: TimeInCharactersToken?,
    val translationTokensByLanguage: Map<String, RichLyricText>
): Comparable<LyricLine> {
    override fun compareTo(other: LyricLine): Int = timeMs.compareTo(other.timeMs)
}

typealias TimeInCharactersToken = List<TimeInCharacterEntry>
data class TimeInCharacterEntry (
    val pos: Int,
    val startMs: Int,
    val endMs: Int
)

interface LyricToken {
    val plainText: String
}

typealias RichLyricText = List<LyricToken>
data class PlainLyricToken(
    override val plainText: String
): LyricToken

data class RubyLyricToken(
    override val plainText: String,
    val ruby: String
): LyricToken

fun RichLyricText.toPlainText() = joinToString("") { it.plainText }
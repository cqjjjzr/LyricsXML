package com.github.charlie.lxml

import java.util.*

data class Lyrics(
    val title: String?,
    val artist: List<String>,
    val album: String?,
    val language: String?,
    val offsetMs: Int,
    val lines: List<Line>
)

interface Line: Comparable<Line> {
    val timeMs: Int

    override fun compareTo(other: Line): Int = timeMs.compareTo(other.timeMs)
}

data class LyricLine(
    override val timeMs: Int,
    val text: RichLyricText,
    val timeInCharacters: TimeInCharactersToken?,
    val translationTokensByLanguage: Map<String, RichLyricText>
): Line

data class SplitLine(
    override val timeMs: Int
): Line

typealias TimeInCharactersToken = SortedSet<TimeInCharacterEntry>
data class TimeInCharacterEntry (
    val pos: Int,
    val endMs: Int
): Comparable<TimeInCharacterEntry> {
    override fun compareTo(other: TimeInCharacterEntry): Int = endMs.compareTo(other.endMs)
}

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
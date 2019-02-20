package com.github.charlie.lxml

import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timestampRegex = "\\[([0-9.:]+)]".toRegex()
private val wholeRegex = "((?:\\[[0-9.:]+])+)(.*)".toRegex()
class LRCImporter {
    fun importFromLRC(lrc: String): Lyrics {
        var title: String? = null
        val artist = arrayListOf<String>()
        var album: String? = null
        var offset = 0
        val lines = arrayListOf<LyricLine>()
        lrc.lines().forEach {
            if (!it.startsWith("[")) return@forEach
            when {
                it.startsWith("[ti:") ->
                    title = it.removeSurrounding("[ti:", "]")
                it.startsWith("[ar:") ->
                    it.removeSurrounding("[ar:", "]").apply {
                        (if (artistSeparator.isNotEmpty()) split(artistSeparator)
                        else listOf(this)).let(artist::addAll)
                    }
                it.startsWith("[al:") ->
                    album = it.removeSurrounding("[al:", "]")
                it.startsWith("[by:") ->
                    return@forEach // not implemented
                it.startsWith("[offset:") ->
                    offset = it.removeSurrounding("[offset:", "]").toIntOrNull() ?: 0
                else -> lines.addAll(parseLine(it))
            }
        }

        return Lyrics(title, artist, album, null, offset, lines.sorted())
    }

    private fun parseLine(unparsed: String): List<LyricLine> {
        try {
            val match = wholeRegex.matchEntire(unparsed) ?: return emptyList()
            val timestamps = match.groupValues[1]
            val line = match.groupValues[2]
            return timestampRegex.findAll(timestamps).map {
                val timeMs = LocalTime.parse(
                    "00:${it.groupValues[1].removeSurrounding("[", "]")}"
                    , DateTimeFormatter.ISO_TIME).toNanoOfDay() / 1000000
                LyricLine(timeMs.toInt(), listOf(PlainLyricToken(line)), null, emptyMap())
            }.toList()
        } catch (ex: Exception) {
            return emptyList()
        }
    }

    var artistSeparator: String = ""
}
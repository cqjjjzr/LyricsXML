package com.github.charlie.lxml

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll

class LRCImporterTest {
    private val importer = LRCImporter()

    @Test
    fun full() {
        val lyrics = importWithResource("/full.lrc")
        assertEquals("千ノ縁", lyrics.title)
        assertEquals(listOf("Yonder Voice"), lyrics.artist)
        assertEquals("千ノ縁", lyrics.album)
        assertEquals(5, lyrics.offsetMs)

        assertEquals(listOf(
            LyricLine(5100, listOf(PlainLyricToken("空に向かう")), null, emptyMap()),
            LyricLine(9200, listOf(PlainLyricToken("爛漫たる夜桜")), null, emptyMap()),
            LyricLine(81100, listOf(PlainLyricToken("空に向かう")), null, emptyMap())
        ), lyrics.lines)
    }

    @Test
    fun fullWithMultipleArtists() {
        importer.artistSeparator = ";"
        val lyrics = importWithResource("/fullWithMultipleArtists.lrc")
        assertEquals(listOf("Yonder Voice", "瑶山百霊"), lyrics.artist)
        importer.artistSeparator = ""
    }

    private fun importWithResource(name: String)
            = importer.importFromLRC(LXMLParserTest::class.java.getResource(name).readText())
}
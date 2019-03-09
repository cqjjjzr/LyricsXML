package com.github.charlie.lxml

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LXMLParserTest {
    var parser = LXMLParser()

    @Test
    fun testFull() {
        val lyrics = parseWithResource("/full.xml")
        assertEquals("千ノ縁", lyrics.title)
        assertEquals("千ノ縁", lyrics.album)
        assertEquals(listOf("Yonder Voice", "瑶山百霊"), lyrics.artist)
        assertEquals(5, lyrics.offsetMs)
        assertEquals("jp_JP", lyrics.language)

        val expected = listOf(
            LyricLine(500,
                listOf(
                    RubyLyricToken("空", "そら"),
                    PlainLyricToken("に"),
                    RubyLyricToken("向", "む"),
                    PlainLyricToken("かう")),
                listOf(
                    TimeInCharacterEntry(0, 0, 0),
                    TimeInCharacterEntry(1, 150, 225),
                    TimeInCharacterEntry(2, 250, 300),
                    TimeInCharacterEntry(3, 350, 400),
                    TimeInCharacterEntry(4, 400, 500)
                ),
                mapOf("zh_CN" to listOf(PlainLyricToken("夜樱烂漫")))),
            SplitLine(501),
            LyricLine(600,
                listOf(
                    RubyLyricToken("爛漫", "らんまん"),
                    PlainLyricToken("たる"),
                    RubyLyricToken("夜", "よ"),
                    RubyLyricToken("桜", "さくら")),
                null,
                mapOf("zh_CN" to listOf(PlainLyricToken("朝向天空绽放"))))
        )

        assertEquals(expected, lyrics.lines)
        val line = lyrics.lines.first() as LyricLine
        assertEquals("空に向かう", line.text.toPlainText())
    }

    @Test
    fun testSmallest() {
        val lyrics = parseWithResource("/smallest.xml")
        assertNull(lyrics.title)
        assertNull(lyrics.album)
        assertEquals(0, lyrics.artist.size, "number of artists should be 0")
        assertEquals(0, lyrics.offsetMs)
        assertNull(lyrics.language)

        assertEquals(1, lyrics.lines.size, "number of lines should be 1")
        val line = lyrics.lines.first() as LyricLine
        assertNull(line.timeInCharacters)
        assertEquals(0, line.translationTokensByLanguage.size, "number of translations should be 0")
        assertEquals("空に向かう", line.text.toPlainText())
        assertEquals(1, line.text.size, "number of text tokens should be 1")
    }

    @Test
    fun testEmpty() {
        val lyrics = parseWithResource("/missingLine.xml")
        assertEquals(0, lyrics.lines.size, "number of lines should be 0")
    }

    @Test
    fun testBad() {
        assertThrows<IllegalLXMLException> { parseWithResource("/missingText.xml") }
        assertThrows<IllegalLXMLException> { parseWithResource("/badTimeInCharacter.xml") }
        assertThrows<IllegalLXMLException> { parseWithResource("/badTimeInCharacter2.xml") }
    }

        private fun parseWithResource(name: String)
                = parser.parse(LXMLParserTest::class.java.getResourceAsStream(name))
}
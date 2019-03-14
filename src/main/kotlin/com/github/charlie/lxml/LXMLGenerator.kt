package com.github.charlie.lxml

import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.dom4j.QName
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter
import java.io.StringWriter
import java.io.Writer
import javax.xml.XMLConstants

const val LXMLNamespaceURI = "http://www.github.com/cqjjjzr/LyricsXML"
class LXMLGenerator {
    fun generateStringUTF8(lyrics: Lyrics): String {
        return StringWriter().apply {
            XMLWriter(this).write(generate(lyrics))
        }.toString()
    }

    fun generateAndWrite(lyrics: Lyrics, writer: Writer, encoding: String = "UTF-8") {
        XMLWriter(
            writer,
            OutputFormat.createCompactFormat().also { it.encoding = encoding })
            .write(generate(lyrics))
    }

    fun generate(lyrics: Lyrics): Document {
        return DocumentHelper.createDocument().apply {
            addElement("lyrics", LXMLNamespaceURI).apply {
                addAttribute(
                    QName.get("schemaLocation", "xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI),
                    "$LXMLNamespaceURI $SchemaFileName")

                if (lyrics.title != null) addElement("title").text = lyrics.title
                lyrics.artist.forEach { addElement("artist").text = it }
                if (lyrics.album != null) addElement("album").text = lyrics.album
                if (lyrics.language != null) addElement("lang").text = lyrics.language
                if (lyrics.offsetMs != 0) addElement("offsetMs").text = lyrics.offsetMs.toString()

                addElement("lines").apply {
                    lyrics.lines.forEach {
                        when (it) {
                            is LyricLine -> addElement("line").fillWithLyricLine(it)
                            is SplitLine -> addElement("split").fillWithSplitLine(it)
                        }
                    }
                }
            }
        }
    }

    private fun Element.fillWithLyricLine(line: LyricLine) {
        addAttribute("timeMs", line.timeMs.toString())
        addElement("text").fillWithRichText(line.text)
        if (line.timeInCharacters != null) {
            addElement("time").text = generateTimeInCharactersString(line.timeInCharacters)
        }
        line.translationTokensByLanguage.forEach { lang, text ->
            addElement("translation")
                .addAttribute("lang", lang)
                .fillWithRichText(text)
        }
    }

    private fun Element.fillWithSplitLine(line: SplitLine) {
        addAttribute("timeMs", line.timeMs.toString())
    }

    private fun generateTimeInCharactersString(timeInCharacters: TimeInCharactersToken): String {
        return buildString {
            timeInCharacters.forEach {
                append(it.pos)
                append(',')
                append(it.endMs)
                append(';')
            }
        }
    }

    private fun Element.fillWithRichText(text: RichLyricText) {
        text.forEach {
            when (it) {
                is RubyLyricToken ->
                    addElement("ruby").addAttribute("v", it.ruby).text = it.plainText
                else ->
                    addText(it.plainText)
            }
        }
    }
}
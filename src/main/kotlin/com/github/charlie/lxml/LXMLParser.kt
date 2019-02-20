package com.github.charlie.lxml

import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.Text
import org.dom4j.io.SAXReader
import org.jetbrains.annotations.Contract
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.lang.IllegalArgumentException

const val SchemaFileName = "LXML_0.0.1.xsd"
const val SchemaResourceName = "/$SchemaFileName"
private val validatorFactory = com.sun.msv.verifier.jarv.TheFactoryImpl()
private val schema = validatorFactory.compileSchema(LXMLParser::class.java.getResourceAsStream(SchemaResourceName))
class LXMLParser {
    fun parse(xmlString: String) = parse(reader.read(ByteArrayInputStream(xmlString.toByteArray())))
    fun parse(file: File) = parse(reader.read(file))
    fun parse(inputStream: InputStream) = parse(reader.read(inputStream))

    @Throws(IllegalLXMLException::class)
    fun parse(document: Document): Lyrics {
        try {
            schema.newVerifier().verify(document)
            val root = document.rootElement
            return Lyrics(
                title =    root.elementText("title"),
                artist =   root.elements("artist").map { it.text },
                album =    root.elementText("album"),
                offsetMs = root.elementText("offsetMs")?.toInt() ?: 0,
                language = root.elementText("lang"),
                lines =    root.elements("line").map { parseLine(it as Element) }.sorted()
            )
        } catch (ex: Exception) {
            throw IllegalLXMLException("bad LXML exception", ex)
        }

    }

    private fun parseLine(element: Element): LyricLine {
        return LyricLine(
            element.attributeValue("timeMs").toInt(),
            parseRichLyricText(element.element("text")),
            parseTimeInCharacters(element.elementText("time")),
            parseTranslations(element.elements("translation"))
        )
    }

    @Contract("!null -> !null")
    private fun parseRichLyricText(element: Element?): RichLyricText {
        if (element == null) throw IllegalArgumentException("bad line: without text token")
        return element.content().map {
            when (it) {
                is Text -> PlainLyricToken(it.text)
                is Element -> {
                    when (it.name) {
                        "ruby" -> RubyLyricToken(
                            plainText = it.text,
                            ruby = it.attributeValue("v"))
                        else -> throw IllegalArgumentException("bad line: unrecognized text token childnode: $it")
                    }
                }
                else -> throw IllegalArgumentException("bad line: malformed text token")
            }
        }
    }

    private fun parseTimeInCharacters(elementText: String?): TimeInCharactersToken? {
        if (elementText == null) return null
        return elementText
            .split(";")
            .map { it.split(",", limit = 3) }
            .filter { it.size == 3 }
            .map {
                val start = it[1].toInt()
                val end = it[2].toInt()
                if (start > end)
                    throw IllegalArgumentException("bad time token: start time > end time")
                TimeInCharacterEntry(
                    it[0].toInt(),
                    it[1].toInt(),
                    it[2].toInt())
            }
    }

    private fun parseTranslations(elements: List<Element>): Map<String, RichLyricText> {
        val result = hashMapOf<String, RichLyricText>()
        elements.forEach {
            val lang = it.attributeValue("lang")
            result[lang] = parseRichLyricText(it)
        }
        return result
    }

    private val reader = SAXReader()
}
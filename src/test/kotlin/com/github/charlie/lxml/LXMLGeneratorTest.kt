package com.github.charlie.lxml

import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import org.junit.jupiter.api.Test
import org.xmlunit.assertj.XmlAssert
import java.io.StringWriter

class LXMLGeneratorTest {
    private val parser = LXMLParser()
    private val generator = LXMLGenerator()

    @Test
    fun test() {
        val xml = SAXReader().read(LXMLParserTest::class.java.getResourceAsStream("/full.xml"))
        val expected = StringWriter().apply { XMLWriter(this).write(xml) }.toString()
        val actual = generator.generateStringUTF8(parser.parse(xml))
        XmlAssert.assertThat(expected).and(actual).normalizeWhitespace().areIdentical()
    }
}
package com.github.charlie.lxml

import org.dom4j.Document
import org.dom4j.io.SAXWriter
import org.iso_relax.verifier.Verifier
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException

/*import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

fun NodeList.iterable() = NodeListIterable(this)
class NodeListIterable(
    private val nodeList: NodeList
): Iterable<Node> {
    override fun iterator(): Iterator<Node> = NodeListIterator()
    inner class NodeListIterator: Iterator<Node> {
        var pos: Int = 0
        override fun hasNext(): Boolean = pos < nodeList.length
        override fun next(): Node = nodeList.item(pos++)
    }
}

fun Element.getFirstOrNullByTagName(tagName: String) = getElementsByTagName(tagName).run {
    if (length == 0) null
    else item(0)
}

fun Document.getFirstOrNullByTagName(tagName: String) = getElementsByTagName(tagName).run {
    if (length == 0) null
    else item(0)
}*/

private class DOMErrorHandler: ErrorHandler {
    var exception: SAXParseException? = null

    override fun warning(exception: SAXParseException?) {
        this.exception = exception
    }

    override fun error(exception: SAXParseException?) {
        this.exception = exception
    }

    override fun fatalError(exception: SAXParseException?) {
        this.exception = exception
    }

}
fun Verifier.verify(doc: Document) {
    val errorHandler = DOMErrorHandler()
    setErrorHandler(errorHandler)
    val valid = verifierHandler.apply {
        SAXWriter(this).write(doc)
    }.isValid
    if (!valid)
        throw errorHandler.exception ?: XMLValidatingFailedException()
}
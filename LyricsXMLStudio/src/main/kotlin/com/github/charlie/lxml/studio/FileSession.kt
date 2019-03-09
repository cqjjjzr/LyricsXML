package com.github.charlie.lxml.studio

import com.github.charlie.lxml.LXMLParser
import com.github.charlie.lxml.Lyrics
import tornadofx.getProperty
import tornadofx.property
import java.io.File

class FileSession(file: File?) {
    var file: File? by property(file)
    fun fileProperty() = getProperty(FileSession::file)

    /*val fileName = file?.name
    fun fileNameProperty() = fileProperty().stringBinding {
        it?.name
    }*/

    var dirty by property(false)
    fun dirtyProperty() = getProperty(FileSession::dirty)

    val lyrics: Lyrics =
        if (file != null) LXMLParser().parse(file)
        else Lyrics(null, emptyList(), null, null, 0, emptyList())
}
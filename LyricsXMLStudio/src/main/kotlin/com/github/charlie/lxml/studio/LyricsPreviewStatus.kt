package com.github.charlie.lxml.studio

import com.github.charlie.lxml.Line
import javafx.collections.ObservableList
import tornadofx.getProperty
import tornadofx.observable
import tornadofx.property

class LyricsPreviewStatus(line: Line? = null) {
    var line: Line? by property(line)
    fun lineProperty() = getProperty(LyricsPreviewStatus::line)

    var localTimeMs: Int by property(0)
    fun localTimeMsProperty() = getProperty(LyricsPreviewStatus::localTimeMs)
}
package com.github.charlie.lxml.studio.ui

import javafx.scene.Parent
import tornadofx.View
import tornadofx.borderpane
import tornadofx.canvas

class LyricsView: View() {
    private val controller: LyricsController by inject()
    private val canvas = canvas {
        height = 250.0
        minHeight(250.0)
    }
    override val root: Parent = borderpane {
        center = canvas
    }
}
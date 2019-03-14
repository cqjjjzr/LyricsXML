package com.github.charlie.lxml.studio.ui

import javafx.stage.Stage
import tornadofx.App

const val Version = "0.0.1"
class LyricsXMLStudioApp: App(MainView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        //JMetro(JMetro.Style.LIGHT).applyTheme(stage.scene)
    }
}
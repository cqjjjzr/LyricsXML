package com.github.charlie.lxml.studio.ui

import com.github.charlie.lxml.studio.FileSession
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.Controller
import java.io.File

class MainController: Controller() {
    fun test() {
        sessions += FileSession(File("D:\\full.xml")).apply {
            dirty = true
        }
    }

    fun save(fileSession: FileSession) {
        fileSession.file = File("D:\\full.xml") // 迫真
    }

    fun closeSession(fileSession: FileSession) {
        sessions -= fileSession
    }

    fun saveAs(session: FileSession, file: File) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun saveAndCloseSession(fileSession: FileSession) {
        save(fileSession)
        closeSession(fileSession)
    }

    fun newFile() {
        sessions += FileSession(null)
    }

    fun open(file: File?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val sessions: ObservableList<FileSession> = FXCollections.observableArrayList()
}
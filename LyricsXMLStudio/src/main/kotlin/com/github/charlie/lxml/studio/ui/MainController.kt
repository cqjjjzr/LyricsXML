package com.github.charlie.lxml.studio.ui

import com.github.charlie.lxml.LXMLGenerator
import com.github.charlie.lxml.LXMLParser
import com.github.charlie.lxml.studio.FileSession
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.Controller
import java.io.File

class MainController: Controller() {
    fun test() {
        sessions += FileSession(File("E:\\full.xml")).apply {
            dirty = true
        }
    }

    fun save(fileSession: FileSession) {
        //fileSession.file = File("D:\\full.xml") // 迫真
        val file = fileSession.file ?: return
        file.writer(Charsets.UTF_8).use {
            generator.generateAndWrite(fileSession.lyrics, it)
        }
        fileSession.dirty = false
    }

    fun closeSession(fileSession: FileSession) {
        sessions -= fileSession
    }

    fun saveAs(session: FileSession, file: File) {
        session.file = file
        save(session)
    }

    fun saveAndCloseSession(fileSession: FileSession) {
        save(fileSession)
        closeSession(fileSession)
    }

    fun newFile() {
        sessions += FileSession(null)
    }

    fun open(file: File) {
        sessions += FileSession(file)
    }

    val sessions: ObservableList<FileSession> = FXCollections.observableArrayList()
    private val generator = LXMLGenerator()
    private val parser = LXMLParser()
}
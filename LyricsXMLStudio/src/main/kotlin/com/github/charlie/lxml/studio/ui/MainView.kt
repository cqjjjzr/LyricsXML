package com.github.charlie.lxml.studio.ui

import com.github.charlie.lxml.studio.FileSession
import javafx.collections.ListChangeListener
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File

class MainView: View() {
    private val filterForLXML: Array<out FileChooser.ExtensionFilter>
            = arrayOf(FileChooser.ExtensionFilter(messages["dialog.lxmlFilter"], "xml"))
    private val controller: MainController by inject()
    private val tabPane = tabpane {
        BindingUtil.mapContent(tabs, controller.sessions, ::createTab)
        tabs.addListener { change: ListChangeListener.Change<out Tab> ->
            if (!change.next()) return@addListener
            if (change.wasAdded())
                change.addedSubList.last().select()
        }
    }
    private val currentTab: Tab? get() = tabPane.selectionModel.selectedItem

    private val currentSession: FileSession? get() = currentTab?.userData as FileSession?
    override val root: Parent = borderpane {
        top = menubar {
            menu(messages["ui.file"]) {
                item(messages["ui.file.new"]) {
                    action {
                        controller.newFile()
                    }
                }

                item(messages["ui.file.open"]) {
                    action {
                        val file = chooseFileForOpen()
                        controller.open(file)
                    }
                }

                item(messages["ui.file.save"]) {
                    setupDisableOnNoTabOpened()
                    action {
                        val session = currentSession ?: return@action
                        save(session)
                    }
                }

                item(messages["ui.file.saveAs"]) {
                    setupDisableOnNoTabOpened()
                    action {
                        val session = currentSession ?: return@action
                        chooseFileForSave(session)?.let {
                            controller.saveAs(session, it)
                        }
                    }
                }
                item(messages["ui.file.close"]) {
                    setupDisableOnNoTabOpened()
                    action {
                        val session = currentSession ?: return@action
                        if (session.dirty) {

                        }
                    }
                }
                separator()
                item(messages["ui.file.saveAll"]) {
                    action {
                        tabPane.tabs.forEach {
                            val session = it.userData as FileSession
                            save(session)
                        }
                    }
                }
                item(messages["ui.file.closeAll"]) {
                    setupDisableOnNoTabOpened()
                    action {

                    }
                }
                separator()
                item(messages["ui.file.exit"]) {
                    action {
                        primaryStage.close()
                    }
                }
            }
        }
        center = tabPane
        titleProperty.bind(tabPane.selectionModel.selectedItemProperty().stringBinding {
            val fileName = if (it != null)
                ((it.userData as FileSession).fileName ?: messages["ui.untitled"]) + " - "
            else ""
            "${fileName}LyricsXML v$Version"
        })

        controller.test()
    }

    private fun createTab(fileSession: FileSession): Tab {
        return Tab(fileSession.fileName ?: messages["ui.untitled"]).apply {
            this.userData = fileSession
            textProperty().bind(fileSession.fileNameProperty().stringBinding {
                it ?: messages["ui.untitled"]
            })
            borderpane {
                center = textarea(fileSession.lyrics.toString())
                bottom = canvas(500.0, 300.0)
            }
            setOnCloseRequest {
                if (fileSession.dirty) {
                    when (askIfSave(fileSession)) {
                        ButtonType.CANCEL -> it.consume()
                        ButtonType.YES -> controller.saveAndCloseSession(fileSession)
                        ButtonType.NO -> controller.closeSession(fileSession)
                    }
                }
            }
        }
    }

    private fun chooseFileForSave(session: FileSession): File? {
        return chooseFile(
            title = messages["dialog.save"],
            filters = filterForLXML,
            mode = FileChooserMode.Save) {
            initialFileName = session.file?.path
        }.firstOrNull()
    }

    private fun chooseFileForOpen(): File? {
        return chooseFile(
            title = messages["dialog.open"],
            filters = filterForLXML,
            mode = FileChooserMode.Single
        ).firstOrNull()
    }

    private fun MenuItem.setupDisableOnNoTabOpened() {
        enableWhen {
            tabPane.selectionModel.selectedItemProperty().isNotNull
        }
        disableWhen {
            tabPane.selectionModel.selectedItemProperty().isNull
        }
    }

    private fun askIfSave(fileSession: FileSession): ButtonType {
        return alert(
            type = Alert.AlertType.WARNING,
            header = "Closing ${fileSession.file?.path ?: "Untitled"}",
            title = "Save?",
            content = "File ${fileSession.file?.path ?: "Untitled"} modified, save?",
            buttons = *arrayOf(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL))
            .result
    }

    private fun save(session: FileSession) {
        if (session.file == null)
            chooseFileForSave(session)?.let {
                controller.saveAs(session, it)
            }
        else controller.save(session)
    }
}
package com.github.charlie.lxml.studio.ui

import com.github.charlie.lxml.studio.FileSession
import com.github.charlie.lxml.studio.exceptionAlert
import com.github.charlie.lxml.studio.messageFormat
import javafx.collections.ListChangeListener
import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import org.fxmisc.easybind.EasyBind
import tornadofx.*
import java.io.File

class MainView: View() {
    private val controller: MainController by inject()
    private val tabPane = tabpane {
        BindingUtil.mapContent(tabs, controller.sessions, ::createTab)
        tabs.addListener { change: ListChangeListener.Change<out Tab> ->
            if (!change.next()) return@addListener
            if (change.wasAdded())
                change.addedSubList.last().select()
        }
        tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
    }
    private val currentTab: Tab? get() = tabPane.selectionModel.selectedItem
    private val currentSession: FileSession? get() = currentTab?.userData as FileSession?

    override val root: Parent = borderpane {
        top = menubar {
            menu(messages["ui.file"]) {
                item(messages["ui.file.new"], KeyCombination.keyCombination("Ctrl+N")) {
                    actionWithCatching(messages["ui.exception.new"]) {
                        controller.newFile()
                    }
                }

                item(messages["ui.file.open"], KeyCombination.keyCombination("Ctrl+O")) {
                    actionWithCatching(messages["ui.exception.open"]) {
                        val file = chooseFileForOpen()
                        if (file != null)
                            controller.open(file)
                    }
                }

                item(messages["ui.file.save"], KeyCombination.keyCombination("Ctrl+S")) {
                    setupDisableOnNoTabOpened()
                    actionWithCatching(messages["ui.exception.save"]) {
                        val session = currentSession ?: return@actionWithCatching
                        save(session)
                    }
                }

                item(messages["ui.file.saveAs"], KeyCombination.keyCombination("Ctrl+Alt+S")) {
                    setupDisableOnNoTabOpened()
                    actionWithCatching(messages["ui.exception.save"]) {
                        val session = currentSession ?: return@actionWithCatching
                        chooseFileForSave(session)?.let {
                            controller.saveAs(session, it)
                        }
                    }
                }
                item(messages["ui.file.close"], KeyCombination.keyCombination("Ctrl+W")) {
                    setupDisableOnNoTabOpened()
                    action {
                        val session = currentSession ?: return@action
                        checkTabAndClose(session)
                    }
                }
                separator()
                item(messages["ui.file.saveAll"]) {
                    action {
                        tabPane.tabs.forEach {
                            val session = it.userData as FileSession
                            try {
                                save(session)
                            } catch (ex: Exception) {
                                exceptionAlert(messages["ui.exception.save"].messageFormat(session.fileName), ex)
                                return@action
                            }
                        }
                    }
                }

                item(messages["ui.file.closeAll"]) {
                    setupDisableOnNoTabOpened()
                    action {
                        tabPane.tabs.map { it.fileSession }.forEach {
                            // be careful, here tabs is copied by "map" to avoid concurrent modification exception
                            try {
                                checkTabAndClose(it)
                            } catch (ex: Exception) {
                                exceptionAlert(messages["ui.exception.saveAndClose"].messageFormat(it.fileName), ex)
                                return@action
                            }
                        }
                    }
                }
                separator()
                item(messages["ui.file.exit"], KeyCombination.keyCombination("Ctrl+Q")) {
                    action {
                        primaryStage.close()
                    }
                }
            }
        }
        center = tabPane


        titleProperty.bind(EasyBind.select(tabPane.selectionModel.selectedItemProperty()).selectObject { tab ->
            tab?.fileSession?.fileProperty()?.stringBinding {
                messages["ui.title.withFile"].messageFormat(Version, it?.path ?: messages["ui.untitled"])
            }
        }.orElse(messages["ui.title"].messageFormat(Version)))

        controller.test()
    }

    // Controller-related helper functions

    @Suppress("RedundantLambdaArrow") // "addListener" bug
    private fun createTab(fileSession: FileSession): Tab {
        return Tab(fileSession.fileName).apply {
            this.userData = fileSession
            val lyricsView = find<LyricsView>(mapOf(LyricsView::status to fileSession.previewStatus))
            this.properties["lyricsView"] = lyricsView

            textProperty().bind(fileSession.fileProperty().stringBinding {
                it?.name ?: messages["ui.untitled"]
            })
            splitpane(Orientation.VERTICAL,
                textarea(fileSession.lyrics.toString()) {
                    textProperty().addListener { _ ->
                        fileSession.dirty = true
                    }
                },
                lyricsView.root) {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS
                setDividerPosition(0, 0.7)
                SplitPane.setResizableWithParent(lyricsView.root, false)

                fileSession.previewStatus.line = fileSession.lyrics.lines[0]
                fileSession.previewStatus.localTimeMs = 450
            }
            setOnCloseRequest {
                checkTabAndClose(fileSession)
                it.consume()
            }
        }
    }

    private fun chooseFileForSave(session: FileSession): File? {
        return chooseFile(
            title = messages["dialog.save"],
            filters = filterForLXML,
            mode = FileChooserMode.Save) {
            initialFileName = session.file?.path
        }.firstOrNull()?.let {
            if (it.extension.isEmpty()) File(it.path + ".xml")
            else it
        }
    }

    private fun chooseFileForOpen(): File? {
        return chooseFile(
            title = messages["dialog.open"],
            filters = filterForLXML,
            mode = FileChooserMode.Single
        ).firstOrNull()
    }

    private fun checkTabAndClose(fileSession: FileSession): Boolean {
        if (fileSession.dirty) {
            when (askIfSave(fileSession)) {
                ButtonType.CANCEL -> return false
                ButtonType.YES -> save(fileSession)
                ButtonType.NO -> controller.closeSession(fileSession)
            }
        } else controller.closeSession(fileSession)
        return true
    }

    private fun askIfSave(fileSession: FileSession): ButtonType {
        val fileName = fileSession.fileName
        return alert(
            type = Alert.AlertType.WARNING,
            header = messages["dialog.saveOrNot.title"].messageFormat(fileName),
            title = "Save?",
            content = messages["dialog.saveOrNot.content"].messageFormat(fileName),
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

    // Just GUI helpers

    private fun MenuItem.setupDisableOnNoTabOpened() {
        enableWhen {
            tabPane.selectionModel.selectedItemProperty().isNotNull
        }
        disableWhen {
            tabPane.selectionModel.selectedItemProperty().isNull
        }
    }

    private val filterForLXML: Array<out FileChooser.ExtensionFilter>
            = arrayOf(FileChooser.ExtensionFilter(messages["dialog.lxmlFilter"], "*.xml"))
    private val Tab.fileSession get() = userData as FileSession
    private val FileSession?.fileName get() = this?.file?.name ?: messages["ui.untitled"]
    private inline fun MenuItem.actionWithCatching(message: String, crossinline block: () -> Unit) {
        action {
            try {
                block()
            } catch (ex: Exception) {
                exceptionAlert(message, ex)
            }
        }
    }

    private inline fun catching(message: String, block: () -> Unit) {
        try {
            block()
        } catch (ex: Exception) {
            exceptionAlert(message, ex)
        }
    }
    private inline fun catching(messageSupplier: () -> String, block: () -> Unit) {
        try {
            block()
        } catch (ex: Exception) {
            exceptionAlert(messageSupplier(), ex)
        }
    }
}
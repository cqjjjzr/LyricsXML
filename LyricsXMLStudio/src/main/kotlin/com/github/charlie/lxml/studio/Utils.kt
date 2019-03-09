package com.github.charlie.lxml.studio

import javafx.scene.control.Alert
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.PrintWriter
import java.io.StringWriter
import java.text.MessageFormat

fun String.messageFormat(vararg params: String) = MessageFormat.format(this, *params)!!

fun Component.exceptionAlert(message: String, ex: Throwable) {
    Alert(Alert.AlertType.ERROR).apply {
        this.title = messages["dialog.error"]
        this.contentText = message
        this.headerText = messages["dialog.errorLong"]

        dialogPane.expandableContent = gridpane {
            maxWidth = Double.MAX_VALUE
            row {
                label(messages["dialog.stackTrace"])
            }

            row {
                textarea(StringWriter().apply {
                    PrintWriter(this).let(ex::printStackTrace)
                }.toString()) {
                    maxHeight = Double.MAX_VALUE
                    maxWidth = Double.MAX_VALUE
                    vgrow = Priority.ALWAYS
                    hgrow = Priority.ALWAYS

                    isEditable = false
                    isWrapText = false
                }
            }
        }
        dialogPane.isExpanded = true
        showAndWait()
    }
}
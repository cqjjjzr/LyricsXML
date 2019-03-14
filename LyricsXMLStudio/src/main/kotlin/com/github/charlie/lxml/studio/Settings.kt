package com.github.charlie.lxml.studio

import tornadofx.getProperty
import tornadofx.objectBinding
import tornadofx.property
import java.awt.Font

private const val DefaultFontFamily = "微软雅黑"
private const val DefaultFontSize = 36.0
class Settings {
    companion object {
        val INSTANCE = Settings()
    }
    var fontFamily by property(DefaultFontFamily)
    fun fontFamilyProperty() = getProperty(Settings::fontFamily)
    var fontSize by property(DefaultFontSize)
    fun fontSizeProperty() = getProperty(Settings::fontSize)

    var font: Font by property()
    fun fontProperty() = getProperty(Settings::font)

    init {
        fontProperty().bind(fontFamilyProperty().objectBinding(fontSizeProperty()) {
            Font(it, Font.PLAIN, fontSize.toInt())
        })
    }
}
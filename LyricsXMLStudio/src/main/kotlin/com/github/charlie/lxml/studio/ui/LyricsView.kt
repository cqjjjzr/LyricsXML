package com.github.charlie.lxml.studio.ui

import com.github.charlie.lxml.LyricLine
import com.github.charlie.lxml.RubyLyricToken
import com.github.charlie.lxml.SplitLine
import com.github.charlie.lxml.studio.LyricsPreviewStatus
import com.github.charlie.lxml.studio.Settings
import com.github.charlie.lxml.toPlainText
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.canvas.Canvas
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import org.jfree.fx.FXGraphics2D
import tornadofx.*
import java.awt.*
import javax.swing.SwingUtilities
import kotlin.math.max

private val FillingColor = Color.BLACK
private val OutlineColor = Color.CYAN
class LyricsView: View() {
    private val controller: LyricsController by inject()
    val status: LyricsPreviewStatus by param()
    private var translationLanguage: String? = null

    private val canvas = LyricsCanvas(120.0)
    override val root: Parent = vbox {
        isFillWidth = true
        add(canvas)
        VBox.setVgrow(canvas, Priority.ALWAYS)
        hbox {
            alignment = Pos.CENTER_LEFT
            combobox<String?> {
                maxWidth = Double.MAX_VALUE
                hgrow = Priority.ALWAYS

                converter = TranslationLanguageConverter()
                items = TranslationLanguages.keys.toList().observable()
                isEditable = true
                valueProperty().addListener { _, _, newValue ->
                    translationLanguage = newValue
                    repaint()
                }
            }
            button(messages["ui.preview.settings"]) {
                GridPane.setFillWidth(this, true)
                minWidth = javafx.scene.control.Button.USE_PREF_SIZE

                action {

                }
            }
        }
    }

    fun repaint() = canvas.repaint()

    inner class LyricsCanvas(private val prefHeight: Double)
        : Canvas(), InvalidationListener {
        private val width by widthProperty().integerBinding { it?.toInt() ?: 0 }
        private val height by heightProperty().integerBinding { it?.toInt() ?: 0 }
        private val graphics = FXGraphics2D(graphicsContext2D)
        init {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.background = Color.WHITE
            graphics.stroke = BasicStroke(1.0f)
            //widthProperty().addListener(this)
            //heightProperty().addListener(this)
        }

        fun repaint(isClear: Boolean = false) {
            val line = status.line
            graphics.apply {
                if (line == null || line is SplitLine) {
                    clear()
                    return
                }

                if (isClear) clear()

                if (line is LyricLine)
                    paintLyricsLine(line, status.localTimeMs,
                        width, height)
            }
        }

        private fun Graphics2D.clear() = clearRect(0, 0, width, height)

        private fun Graphics2D.paintLyricsLine(line: LyricLine, localTimeMs: Int,
                                               width: Int, height: Int) {
            val baseFont = Settings.INSTANCE.font
            //val textFont = baseFont.deriveFont(Font.BOLD)
            val rubyFont = baseFont.deriveFont(baseFont.size * 0.5f)
            font = baseFont
            val fontMetrics = baseFont.fontMetrics
            val rubyFontMetrics = rubyFont.fontMetrics
            //val fontMetrics = getFontMetrics(baseFont)
            //val rubyFontMetrics = getFontMetrics(rubyFont)

            val renderingTokens = calculateRenderingTokens(
                line,
                fontMetrics,
                rubyFontMetrics)
            val totalWidth = renderingTokens.sumBy {
                it.width
            }

            var x = (width - totalWidth) / 2
            val textY = height * 3 / 5
            val rubyY = textY - rubyFontMetrics.height - 10

            var remainingHighlightChars = calculateHighlightChars(line, localTimeMs)
            renderingTokens.forEach {
                when (it) {
                    is RenderingRubyToken -> {
                        val highlightChars =
                            if (remainingHighlightChars > it.ruby.length)
                                it.ruby.length.toDouble()
                            else
                                remainingHighlightChars
                        val highlightPlainChars = highlightChars / it.ruby.length * it.text.length

                        val textX =
                            if (it.textWidth < it.rubyWidth) x + (it.rubyWidth - it.textWidth) / 2
                            else x
                        val rubyX =
                            if (it.textWidth < it.rubyWidth) x
                            else x + (it.textWidth - it.rubyWidth) / 2
                        drawStringWithHighlight(it.ruby, rubyFont, rubyX, rubyY, highlightChars)
                        drawStringWithHighlight(it.text, baseFont, textX, textY, highlightPlainChars)
                        remainingHighlightChars -= highlightChars
                    }
                    is RenderingPlainToken -> {
                        val highlightChars =
                            if (remainingHighlightChars > it.text.length)
                                it.text.length.toDouble()
                            else
                                remainingHighlightChars
                        drawStringWithHighlight(it.text, baseFont, x, textY, highlightChars)
                        remainingHighlightChars -= highlightChars
                    }
                }
                x += it.width
            }

            drawTranslation(line, baseFont, width, height)
        }

        private fun Graphics2D.drawStringWithHighlight(
            str: String,
            font: Font,
            x: Int,
            y: Int,
            highlightChars: Double
        ) {
            this.font = font
            paint = FillingColor
            if (highlightChars == 0.0) {
                drawString(str, x, y)
                return
            }

            var remainingHighlightChars = highlightChars
            val vec = font.createGlyphVector(fontRenderContext, str)
            drawGlyphVector(vec, x.toFloat(), y.toFloat())
            val positions = vec.getGlyphPositions(0, vec.numGlyphs, null)

            paint = OutlineColor
            val originalTransform = transform
            for (i in 0..vec.numGlyphs) {
                val outline = vec.getGlyphOutline(i)
                val outlineX = (x + positions[i * 2 + 1]).toDouble()
                val outlineY = (y + positions[i * 2 + 1]).toDouble()
                translate(outlineX, outlineY)
                if (remainingHighlightChars > 1) {
                    draw(outline)
                    translate(-outlineX, -outlineY)
                    remainingHighlightChars--
                } else {
                    val bounds = vec.getGlyphPixelBounds(i, null, 0.0f, 0.0f)
                    bounds.width = (bounds.width * remainingHighlightChars).toInt()
                    clip = bounds
                    draw(outline)
                    clip = null
                    translate(-outlineX, -outlineY)
                    return
                }
                transform = originalTransform
            }
        }

        private fun calculateRenderingTokens(
            line: LyricLine,
            fontMetrics: FontMetrics,
            rubyFontMetrics: FontMetrics
        ): List<RenderingToken> {
            return line.text.map {
                when (it) {
                    is RubyLyricToken -> {
                        //font = baseFont
                        val textWidth = fontMetrics.computeStringWidth(it.plainText)
                        //font = rubyFont
                        val rubyWidth = rubyFontMetrics.computeStringWidth(it.ruby)
                        RenderingRubyToken(
                            it.plainText,
                            it.ruby,
                            textWidth, rubyWidth
                        )
                    }
                    else -> {
                        //font = baseFont
                        RenderingPlainToken(
                            it.plainText,
                            fontMetrics.computeStringWidth(it.plainText)
                        )
                    }
                }
            }
        }

        private fun calculateHighlightChars(line: LyricLine, localTimeMs: Int): Double {
            var prevPos = 0
            var prevEnd = 0
            val token = line.timeInCharacters ?: return 0.0
            token.forEach {
                if (it.endMs <= localTimeMs) {
                    prevPos = it.pos
                    prevEnd = it.endMs
                }
                else {
                    return prevPos +
                            (it.pos - prevPos) * ((localTimeMs - prevEnd).toDouble() / (it.endMs - prevEnd))
                }
            }
            return prevPos.toDouble()
        }

        private fun Graphics2D.drawTranslation(
            line: LyricLine,
            baseFont: Font,
            width: Int,
            height: Int
        ) {
            if (translationLanguage != null &&
                line.translationTokensByLanguage.containsKey(translationLanguage!!)
            ) {
                val translationPlain = line.translationTokensByLanguage[translationLanguage!!]!!
                    .toPlainText()
                val translationFont = baseFont.deriveFont(baseFont.size * 0.75f)
                font = translationFont
                SwingUtilities.computeStringWidth(fontMetrics, translationPlain)
                val translationWidth =
                    SwingUtilities.computeStringWidth(fontMetrics, translationPlain)

                drawString(translationPlain, (width - translationWidth) / 2, height * 4 / 5 + 10)
            }
        }

        override fun invalidated(observable: Observable?) = repaint()

        override fun minHeight(width: Double): Double = prefHeight
        override fun maxHeight(width: Double): Double = Double.MAX_VALUE
        override fun minWidth(height: Double): Double = 0.0
        override fun maxWidth(height: Double): Double = Double.MAX_VALUE
        override fun isResizable(): Boolean = true

        override fun resize(width: Double, height: Double) {
            super.setWidth(width)
            super.setHeight(height)
            repaint(true)
        }
    }

    private abstract class RenderingToken (
        @JvmField
        val text: String = "",
        @JvmField
        val width: Int = 0
    )
    private class RenderingPlainToken(
        text: String,
        width: Int
    ): RenderingToken(text, width)
    private class RenderingRubyToken(
        text: String,
        @JvmField
        val ruby: String,
        @JvmField
        val textWidth: Int,
        @JvmField
        val rubyWidth: Int
    ): RenderingToken(text, max(textWidth, rubyWidth))
}

class TranslationLanguageConverter : StringConverter<String?>() {
    override fun fromString(string: String?): String? {
        if (string == null) return ""
        return string.substringAfterLast(' ')
            .removeSurrounding("[", "]")
    }

    override fun toString(lang: String?): String? {
        if (lang.isNullOrEmpty()) return ""
        return "${TranslationLanguages[lang] ?: "Unknown"} [$lang]"
    }
}

// FUCK FXGraphics2D FXFontMetrics
private fun FontMetrics.computeStringWidth(string: String) = SwingUtilities.computeStringWidth(this, string)
private val dummyCanvas = java.awt.Canvas()
private val Font.fontMetrics get() = dummyCanvas.getFontMetrics(this)
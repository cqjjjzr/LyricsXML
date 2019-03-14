package com.github.charlie.lxml.studio.ui

val TranslationLanguages = object {}.javaClass
    .getResourceAsStream("/language-codes.csv").reader().readLines()
    .map { it.split(",", limit = 2) }
    .filter { it.size >= 2 }
    .associateBy({ it[0] }) { it[1] }
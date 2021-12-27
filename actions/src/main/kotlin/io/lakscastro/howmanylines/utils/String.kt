package io.lakscastro.howmanylines.utils

fun String.clear(): String = split("\n").joinToString("") { it.trim() }
fun String.trimEachLine(): String = split("\n").joinToString("\n") { it.trim() }

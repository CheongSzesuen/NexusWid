package cn.waijade.nexuswid.utils

import androidx.compose.ui.graphics.Color

fun String.toColor(): Color {
    val comma1 = this.indexOf(',')
    val comma2 = this.indexOf(',', comma1 + 1)
    val comma3 = this.indexOf(',', comma2 + 1)
    val comma4 = this.indexOf(',', comma3 + 1)

    val r = this.substringAfter('(').substringBefore(',').toFloat()
    val g = this.slice(comma1 + 1..<comma2).toFloat()
    val b = this.slice(comma2 + 1..<comma3).toFloat()
    val a = this.slice(comma3 + 1..<comma4).toFloat()
    return Color(r, g, b, a)
}

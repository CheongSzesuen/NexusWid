package cn.waijade.nexuswid.ui.utils

fun <T> MutableList<T>.onBack() {
    if (size > 1) removeLastOrNull()
}

fun <T> MutableList<T>.onTopLevelNavigate(screen: T) {
    if (size < 2) add(screen)
    else set(1, screen)
}

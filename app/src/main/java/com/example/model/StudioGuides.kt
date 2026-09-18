package com.example.model

import androidx.compose.ui.graphics.Color

enum class SymmetryMode(val titleAr: String, val iconLabel: String) {
    NONE("معطل", "✕"),
    VERTICAL("مرآة عمودية", "┃"),
    HORIZONTAL("مرآة أفقية", "━"),
    QUAD("تناظر رباعي", "╋")
}

enum class CanvasPaper(val titleAr: String, val color: Color, val isDark: Boolean) {
    WHITE("أبيض ناصع", Color(0xFFFFFFFF), false),
    WARM_CREAM("ورق دافئ", Color(0xFFFBF8EE), false),
    KRAFT("ورق كرافت", Color(0xFFE2D1B3), false),
    STUDIO_GRAY("رمادي حيادي", Color(0xFF9E9E9E), true),
    DARK_SLATE("أسود داكن", Color(0xFF1E1E24), true)
}

enum class ActiveSlidingSheet {
    NONE,
    BRUSHES,
    LAYERS,
    TOOLS,
    COLOR,
    EXPORT
}

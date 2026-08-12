package com.atharok.screentime.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SupabaseOfficialGreen = Color(0xFF3ECF8E)

val SupabaseLogoIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "SupabaseOfficialLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(SupabaseOfficialGreen)) {
            moveTo(21.362f, 9.354f)
            lineTo(12.393f, 0.287f)
            curveTo(11.979f, -0.133f, 11.267f, -0.076f, 10.923f, 0.404f)
            lineTo(0.25f, 15.297f)
            curveTo(-0.198f, 15.922f, 0.248f, 16.792f, 1.02f, 16.792f)
            horizontalLineTo(10.638f)
            lineTo(2.638f, 24.646f)
            curveTo(2.224f, 25.066f, 2.936f, 25.009f, 3.28f, 24.529f)
            lineTo(13.953f, 9.636f)
            curveTo(14.401f, 9.011f, 13.955f, 8.141f, 13.183f, 8.141f)
            horizontalLineTo(3.565f)
            close()
        }
    }.build()
}

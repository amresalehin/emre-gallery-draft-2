package com.example.model

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

data class FilterOption(
    val id: String,
    val name: String,
    val colorMatrix: ColorMatrix
)

object FilterPresets {
    val NORMAL = FilterOption("Normal", "Original", ColorMatrix())

    val VIVID = FilterOption(
        "Vivid",
        "Vivid",
        ColorMatrix(
            floatArrayOf(
                1.2f, 0f, 0f, 0f, 0f,
                0f, 1.2f, 0f, 0f, 0f,
                0f, 0f, 1.2f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    val MONOCHROME = FilterOption(
        "Monochrome",
        "B&W",
        ColorMatrix().apply { setToSaturation(0f) }
    )

    val SEPIA = FilterOption(
        "Sepia",
        "Sepia",
        ColorMatrix(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    val WARM = FilterOption(
        "Warm",
        "Warmth",
        ColorMatrix(
            floatArrayOf(
                1.15f, 0f, 0f, 0f, 10f,
                0f, 1.05f, 0f, 0f, 5f,
                0f, 0f, 0.9f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    val COOL = FilterOption(
        "Cool",
        "Cool",
        ColorMatrix(
            floatArrayOf(
                0.9f, 0f, 0f, 0f, -10f,
                0f, 1.0f, 0f, 0f, 0f,
                0f, 0f, 1.2f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    val CYBERPUNK = FilterOption(
        "Cyberpunk",
        "Cyber",
        ColorMatrix(
            floatArrayOf(
                1.3f, 0f, 0.2f, 0f, 15f,
                0f, 0.8f, 0.1f, 0f, -10f,
                0.1f, 0.1f, 1.4f, 0f, 25f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    val DRAMATIC = FilterOption(
        "Dramatic",
        "Dramatic",
        ColorMatrix(
            floatArrayOf(
                1.35f, 0f, 0f, 0f, -20f,
                0f, 1.35f, 0f, 0f, -20f,
                0f, 0f, 1.35f, 0f, -20f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    val all: List<FilterOption> = listOf(
        NORMAL, VIVID, WARM, COOL, MONOCHROME, SEPIA, CYBERPUNK, DRAMATIC
    )

    fun getFilterMatrix(presetName: String): ColorMatrix {
        return all.find { it.id.equals(presetName, ignoreCase = true) }?.colorMatrix ?: ColorMatrix()
    }

    fun buildAdjustmentMatrix(
        presetName: String,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): ColorMatrix {
        val base = ColorMatrix(getFilterMatrix(presetName).values.copyOf())

        // Saturation factor
        if (saturation != 0f) {
            val satMatrix = ColorMatrix().apply { setToSaturation(1f + saturation) }
            // Multiply manually or compose
        }

        // Apply contrast and brightness adjustments to diagonal and offset
        val contrastFactor = 1f + contrast
        val brightnessOffset = brightness * 100f

        val values = base.values
        for (i in 0..2) {
            val row = i * 5
            values[row] = values[row] * contrastFactor
            values[row + 1] = values[row + 1] * contrastFactor
            values[row + 2] = values[row + 2] * contrastFactor
            values[row + 4] = values[row + 4] + brightnessOffset
        }

        return ColorMatrix(values)
    }
}

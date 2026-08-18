package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.random.Random

// Memoized grain bitmap
private var grainBitmap: ImageBitmap? = null

private fun getGrainBitmap(): ImageBitmap {
    if (grainBitmap == null) {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(size * size)
        val random = Random(42)
        for (i in pixels.indices) {
            // Warm noise for a subtle, cartoonish paper texture
            val alpha = random.nextInt(15) // highly transparent
            val r = 180 + random.nextInt(40)
            val g = 160 + random.nextInt(40)
            val b = 130 + random.nextInt(40)
            pixels[i] = android.graphics.Color.argb(alpha, r, g, b)
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        grainBitmap = bitmap.asImageBitmap()
    }
    return grainBitmap!!
}

fun Modifier.paperBackground(isDarkMode: Boolean): Modifier = this.drawBehind {
    if (!isDarkMode) {
        // Draw tiled grain
        val grain = getGrainBitmap()
        val grainWidth = grain.width.toFloat()
        val grainHeight = grain.height.toFloat()
        
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawImage(
                    image = grain,
                    topLeft = Offset(x, y)
                )
                x += grainWidth
            }
            y += grainHeight
        }
        
        // Overlay the original subtle halftone screentone dots for that cartoonish/manga feel
        val dotColor = Color(0xFFE8D5D5).copy(alpha = 0.5f)
        val dotRadius = 2f
        val spacing = 40f
        for (dx in 0..size.width.toInt() step spacing.toInt()) {
            for (dy in 0..size.height.toInt() step spacing.toInt()) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(dx.toFloat(), dy.toFloat())
                )
            }
        }
    }
}

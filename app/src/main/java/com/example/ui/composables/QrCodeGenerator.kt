package com.example.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun QrCodeGenerator(
    content: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    codeColor: Color = Color(0xFF0F172A)
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val hash = content.hashCode()
            val sizeRaw = 15 // 15x15 matrix
            val cellWidth = size.width / sizeRaw.toFloat()
            val cellHeight = size.height / sizeRaw.toFloat()

            // Draw Corner Anchors (The distinctive QR position squares)
            fun drawAnchor(x: Int, y: Int) {
                // outer black box
                drawRect(
                    color = codeColor,
                    topLeft = Offset(x * cellWidth, y * cellHeight),
                    size = Size(4 * cellWidth, 4 * cellHeight)
                )
                // white middle box
                drawRect(
                    color = backgroundColor,
                    topLeft = Offset((x + 1) * cellWidth, (y + 1) * cellHeight),
                    size = Size(2 * cellWidth, 2 * cellHeight)
                )
                // black inner box
                drawRect(
                    color = codeColor,
                    topLeft = Offset((x + 1.25f) * cellWidth, (y + 1.25f) * cellHeight),
                    size = Size(1.5f * cellWidth, 1.5f * cellHeight)
                )
            }

            // Draw anchoring finder boxes
            drawAnchor(0, 0) // Top Left
            drawAnchor(sizeRaw - 4, 0) // Top Right
            drawAnchor(0, sizeRaw - 4) // Bottom Left

            // Draw custom deterministic matrix based on hashed key
            val random = java.util.Random(hash.toLong())
            for (r in 0 until sizeRaw) {
                for (c in 0 until sizeRaw) {
                    // Skip where corners were drawn
                    if (r < 4 && c < 4) continue
                    if (r < 4 && c >= sizeRaw - 4) continue
                    if (r >= sizeRaw - 4 && c < 4) continue

                    // Dynamic bits
                    val drawBlock = random.nextBoolean()
                    if (drawBlock) {
                        drawRect(
                            color = codeColor,
                            topLeft = Offset(c * cellWidth, r * cellHeight),
                            size = Size(cellWidth + 0.5f, cellHeight + 0.5f) // small padding overlay to eliminate spacing lines
                        )
                    }
                }
            }

            // Draw tiny alignment block on bottom-right center
            drawRect(
                color = codeColor,
                topLeft = Offset((sizeRaw - 3) * cellWidth, (sizeRaw - 3) * cellHeight),
                size = Size(cellWidth, cellHeight)
            )
        }
    }
}

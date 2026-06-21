package com.musicplayer.melodex.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * M3 Expressive 取色器对话框
 *
 * 提供：
 * - 色相条（Hue bar）
 * - 饱和度/明度面板（SV panel）
 * - 实时颜色预览
 * - HEX 输入
 * - 预设色快捷选择
 */
@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var currentColor by remember { mutableStateOf(initialColor) }
    var hue by remember { mutableStateOf(getHue(initialColor)) }
    var saturation by remember { mutableStateOf(getSaturation(initialColor)) }
    var value by remember { mutableStateOf(getValueOf(initialColor)) }
    var hexInput by remember { mutableStateOf(colorToHex(initialColor)) }

    val syncColor = { h: Float, s: Float, v: Float ->
        val newColor = Color.hsv(h, s, v)
        currentColor = newColor
        hexInput = colorToHex(newColor)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Pick a color",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── SV Panel (Saturation × Value) ──
                SaturationValuePanel(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onColorChange = { s, v ->
                        saturation = s
                        value = v
                        syncColor(hue, s, v)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Hue Bar ──
                HueBar(
                    hue = hue,
                    onHueChange = { h ->
                        hue = h
                        syncColor(h, saturation, value)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Preview + HEX ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Color preview circle
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = currentColor,
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {}

                    // HEX input
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { input ->
                            val clean = input.filter { it in "0123456789ABCDEFabcdef" }.take(6)
                            hexInput = if (clean.startsWith("#") || input.startsWith("#")) {
                                if (input.length > 7) input.take(7) else input
                            } else {
                                "#$clean"
                            }
                            // Parse hex
                            val parsed = parseHexColor(hexInput)
                            if (parsed != null) {
                                currentColor = parsed
                                hue = getHue(parsed)
                                saturation = getSaturation(parsed)
                                value = getValueOf(parsed)
                            }
                        },
                        label = { Text("HEX") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Preset Colors ──
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val presets = listOf(
                        Color(0xFF4A5CF7), // Blue-Purple
                        Color(0xFF1976D2), // Blue
                        Color(0xFF00897B), // Teal
                        Color(0xFF43A047), // Green
                        Color(0xFFFDD835), // Yellow
                        Color(0xFFFF7043), // Deep Orange
                        Color(0xFFE53935), // Red
                        Color(0xFFD81B60), // Pink
                    )
                    presets.forEach { preset ->
                        val isSelected = currentColor == preset
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                ),
                            shape = CircleShape,
                            color = preset,
                            onClick = {
                                currentColor = preset
                                hue = getHue(preset)
                                saturation = getSaturation(preset)
                                value = getValueOf(preset)
                                hexInput = colorToHex(preset)
                            }
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (getValueOf(preset) > 0.5f) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onColorSelected(currentColor)
                            onDismiss()
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Apply", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

/**
 * 饱和度/明度二维面板 — 横轴为饱和度，纵轴为明度。
 */
@Composable
private fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onColorChange: (saturation: Float, value: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Background: white (top) → hue color (bottom)
        // But we need a 2D: horizontal = saturation, vertical = value
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val s = (offset.x / size.width).coerceIn(0f, 1f)
                        val v = (1f - offset.y / size.height).coerceIn(0f, 1f)
                        onColorChange(s, v)
                    }
                }
        ) {
            // Draw the SV panel
            val step = 4
            for (y in 0 until size.height.toInt() step step) {
                for (x in 0 until size.width.toInt() step step) {
                    val s = x / size.width
                    val v = 1f - y / size.height
                    val color = Color.hsv(hue, s, v)
                    drawRect(
                        color = color,
                        topLeft = Offset(x.toFloat(), y.toFloat()),
                        size = androidx.compose.ui.geometry.Size(step.toFloat(), step.toFloat())
                    )
                }
            }

            // Draw indicator
            val indicatorX = saturation * size.width
            val indicatorY = (1f - value) * size.height
            drawCircle(
                color = Color.White,
                radius = 10f,
                center = Offset(indicatorX, indicatorY),
                style = Stroke(width = 3f)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = 11f,
                center = Offset(indicatorX, indicatorY),
                style = Stroke(width = 1f)
            )
        }
    }
}

/**
 * 色相条 — 水平渐变，从红到红（360°）。
 */
@Composable
private fun HueBar(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var barWidth by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val h = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                        onHueChange(h)
                    }
                }
        ) {
            barWidth = size.width
            // Draw hue gradient
            val segmentCount = 360
            val segmentWidth = size.width / segmentCount
            for (i in 0 until segmentCount) {
                drawRect(
                    color = Color.hsv(i.toFloat(), 1f, 1f),
                    topLeft = Offset(i * segmentWidth, 0f),
                    size = androidx.compose.ui.geometry.Size(segmentWidth + 1f, size.height)
                )
            }

            // Draw indicator
            val indicatorX = (hue / 360f) * size.width
            drawCircle(
                color = Color.White,
                radius = 14f,
                center = Offset(indicatorX, size.height / 2f),
                style = Stroke(width = 3f)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = 15f,
                center = Offset(indicatorX, size.height / 2f),
                style = Stroke(width = 1f)
            )
        }
    }
}

// ── Color Helpers ──

private fun getHue(color: Color): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt(),
        hsv
    )
    return hsv[0]
}

private fun getSaturation(color: Color): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt(),
        hsv
    )
    return hsv[1]
}

private fun getValueOf(color: Color): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt(),
        hsv
    )
    return hsv[2]
}

private fun colorToHex(color: Color): String {
    val r = (color.red * 255).toInt().coerceIn(0, 255)
    val g = (color.green * 255).toInt().coerceIn(0, 255)
    val b = (color.blue * 255).toInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(r, g, b)
}

private fun parseHexColor(hex: String): Color? {
    val clean = hex.removePrefix("#")
    if (clean.length != 6) return null
    return try {
        val rgb = clean.toLong(16)
        Color(
            red = ((rgb shr 16) and 0xFF) / 255f,
            green = ((rgb shr 8) and 0xFF) / 255f,
            blue = (rgb and 0xFF) / 255f
        )
    } catch (_: Exception) {
        null
    }
}
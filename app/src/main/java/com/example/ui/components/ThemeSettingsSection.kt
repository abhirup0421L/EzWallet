package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ThemeSettingsSection(
    currentBgColor: String?,
    currentSelectionColor: String?,
    currentFileColor: String?,
    onUpdateTheme: (String?, String?, String?) -> Unit
) {
    val bgColors = listOf("#FFFFFF", "#F8F9FA", "#FDF5E6", "#121212", "#1A1A2E", "#0F172A")
    val selectionColors = listOf("#6750A4", "#2563EB", "#059669", "#DC2626", "#D97706", "#7C3AED")
    val fileColors = listOf("#EADDFF", "#DBEAFE", "#D1FAE5", "#FEE2E2", "#FEF3C7", "#EDE9FE")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Custom Theme",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ColorPickerRow(
                label = "Background Color",
                colors = bgColors,
                selectedColor = currentBgColor,
                onColorSelected = { onUpdateTheme(it, currentSelectionColor, currentFileColor) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ColorPickerRow(
                label = "Selection (Primary) Color",
                colors = selectionColors,
                selectedColor = currentSelectionColor,
                onColorSelected = { onUpdateTheme(currentBgColor, it, currentFileColor) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ColorPickerRow(
                label = "File (Card) Color",
                colors = fileColors,
                selectedColor = currentFileColor,
                onColorSelected = { onUpdateTheme(currentBgColor, currentSelectionColor, it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onUpdateTheme(null, null, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Restore Default Theme")
            }
        }
    }
}

@Composable
fun ColorPickerRow(
    label: String,
    colors: List<String>,
    selectedColor: String?,
    onColorSelected: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(colors) { hex ->
                val color = Color(android.graphics.Color.parseColor(hex))
                val isSelected = selectedColor?.equals(hex, ignoreCase = true) == true
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

fun Color.luminance(): Float {
    val red = this.red.toDouble()
    val green = this.green.toDouble()
    val blue = this.blue.toDouble()
    return (0.2126 * red + 0.7152 * green + 0.0722 * blue).toFloat()
}

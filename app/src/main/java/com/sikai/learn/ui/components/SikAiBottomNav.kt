package com.sikai.learn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.SikAi

data class SikAiNavItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun SikAiBottomNav(
    items: List<SikAiNavItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The main container for the floating navbar
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp, top = 4.dp) // Minimized margins
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp)) // Slimmer rounded borders
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(vertical = 6.dp), // Minimized vertical padding
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item.key == selectedKey
                
                Box(
                    modifier = Modifier
                        .size(40.dp) // Reduced circle size
                        .clip(CircleShape)
                        .clickable { onSelect(item.key) }
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary 
                            else androidx.compose.ui.graphics.Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary 
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp) // Reduced icon size
                    )
                }
            }
        }
    }
}

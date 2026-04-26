package com.sikai.learn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.NeoVedicTokens

data class NeoVedicNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
)

@Composable
fun NeoVedicBottomNav(
    items: List<NeoVedicNavItem>,
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        HairlineDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val isSelected = currentRoute?.startsWith(item.route) == true
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(item.route) }
                        .padding(NeoVedicTokens.SpaceXs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(NeoVedicTokens.IconLg),
                    )
                    Text(
                        item.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .height(2.dp)
                                .width(20.dp)
                                .background(MaterialTheme.colorScheme.secondary),
                        )
                    }
                }
            }
        }
    }
}

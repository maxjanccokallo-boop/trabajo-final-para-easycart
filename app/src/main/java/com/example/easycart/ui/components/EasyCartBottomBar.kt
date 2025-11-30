package com.example.easycart.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.easycart.ui.navigation.BottomTab

@Composable
fun EasyCartBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3EDF7))
            .padding(vertical = 8.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            BottomTab.values().forEach { tab ->

                val isSelected = selectedTab == tab

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.10f else 1f,
                    label = "scaleAnim"
                )

                val circleColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFE5D9FF) else Color.Transparent,
                    label = "colorAnim"
                )

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF5E35B1) else Color(0xFF555555),
                    label = "iconColorAnim"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 4.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(circleColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color(0xFF5E35B1) else Color(0xFF444444)
                    )
                }
            }
        }
    }
}

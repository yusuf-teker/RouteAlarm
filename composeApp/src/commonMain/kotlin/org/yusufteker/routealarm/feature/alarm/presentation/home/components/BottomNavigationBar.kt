    package org.yusufteker.routealarm.feature.alarm.presentation.home.components

    import androidx.compose.animation.animateColorAsState
    import androidx.compose.animation.core.FastOutSlowInEasing
    import androidx.compose.animation.core.tween
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Add
    import androidx.compose.material.icons.filled.Home
    import androidx.compose.material.icons.filled.Settings
    import androidx.compose.material3.Icon
    import androidx.compose.material3.Surface
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.vector.ImageVector
    import androidx.compose.ui.unit.dp
    import org.yusufteker.routealarm.app.Routes
    import org.yusufteker.routealarm.core.presentation.AppColors
    /**
     * Represents an item in the bottom navigation bar
     */
    data class BottomNavItem(
        val label: String,
        val icon: ImageVector,
        val route: Routes
    )

    /**
     * A composable function that renders a single navigation item in the bottom bar
     */
    @Composable
    fun NavItem(
        item: BottomNavItem,
        selected: Boolean,
        onClick: () -> Unit
    ) {
        val backgroundColor by animateColorAsState(
            targetValue = if (selected) Color.Green.copy(alpha = 0.1f) else Color.Transparent,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "backgroundColorAnimation"
        )

        val iconColor by animateColorAsState(
            targetValue = if (selected) Color.Green else AppColors.textSecondary,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "iconColorAnimation"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp)
                .height(36.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    /**
     * A composable function that renders the bottom navigation bar
     */
    @Composable
    fun BottomNavigationBar(
        currentRoute: String,
        onItemSelected: (Routes) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val items = listOf(
            BottomNavItem("Alarms", Icons.Filled.Home, Routes.HomeScreen),
            BottomNavItem("Add", Icons.Filled.Add, Routes.AddAlarmScreen),
            BottomNavItem("Settings", Icons.Filled.Settings, Routes.SettingsScreen),
        )

        Surface(
            modifier = modifier,
            color = Color.Transparent,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        NavItem(
                            item = item,
                            selected = item.route.toString() == currentRoute,
                            onClick = { onItemSelected(item.route) }
                        )
                    }
                }
            }
        }
    }
package org.yusufteker.routealarm.feature.alarm.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.app.Routes
import org.yusufteker.routealarm.core.presentation.AppColors
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.nav_add
import routealarm.composeapp.generated.resources.nav_alarms
import routealarm.composeapp.generated.resources.nav_settings

data class BottomNavItem(
    val labelRes: StringResource,
    val icon: ImageVector,
    val route: Routes,
    val isSelected: (NavDestination?) -> Boolean
)

@Composable
fun NavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) AppColors.accent.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "backgroundColorAnimation"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) AppColors.accent else AppColors.textSecondary,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "iconColorAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.labelRes),
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(item.labelRes),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
fun BottomNavigationBar(
    currentDestination: NavDestination?,
    onItemSelected: (Routes) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            labelRes = Res.string.nav_alarms,
            icon = Icons.Filled.Home,
            route = Routes.HomeScreen,
            isSelected = { it?.hasRoute<Routes.HomeScreen>() == true }
        ),
        BottomNavItem(
            labelRes = Res.string.nav_add,
            icon = Icons.Filled.AddCircle,
            route = Routes.AddAlarmScreen,
            isSelected = { it?.hasRoute<Routes.AddAlarmScreen>() == true }
        ),
        BottomNavItem(
            labelRes = Res.string.nav_settings,
            icon = Icons.Filled.Settings,
            route = Routes.SettingsScreen,
            isSelected = { it?.hasRoute<Routes.SettingsScreen>() == true }
        ),
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, AppColors.border, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = AppColors.surfaceElevated,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    NavItem(
                        item = item,
                        selected = item.isSelected(currentDestination),
                        onClick = { onItemSelected(item.route) }
                    )
                }
            }
        }
    }
}

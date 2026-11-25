package org.yusufteker.routealarm.permissions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.PlatformType
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.getPlatform
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.bus
import routealarm.composeapp.generated.resources.close
import routealarm.composeapp.generated.resources.continue_text
import routealarm.composeapp.generated.resources.location_dialog_bg_desc_android
import routealarm.composeapp.generated.resources.location_dialog_bg_desc_ios
import routealarm.composeapp.generated.resources.location_dialog_bg_title_android
import routealarm.composeapp.generated.resources.location_dialog_bg_title_ios
import routealarm.composeapp.generated.resources.location_dialog_fg_desc
import routealarm.composeapp.generated.resources.location_dialog_fg_title
import routealarm.composeapp.generated.resources.map_pin
import routealarm.composeapp.generated.resources.open_settings

@Composable
fun LocationPermissionDialog(
    canContinueAnyway: Boolean = false,
    onDismiss: () -> Unit,
    onContinueClicked: () -> Unit,
    onSkipClicked: () -> Unit = onDismiss
) {
    val isAndroid = getPlatform().type == PlatformType.ANDROID
    val initialPage = if (canContinueAnyway) 1 else 0
    val pagerState = rememberPagerState(initialPage = initialPage) { 2 }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppColors.cardBackground,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(state = pagerState, modifier = Modifier.height(230.dp)) { page ->
                    when (page) {
                        0 -> PermissionPageContent(
                            title = stringResource(Res.string.location_dialog_fg_title),
                            description = stringResource(Res.string.location_dialog_fg_desc),
                            imageRes = Res.drawable.map_pin
                        )
                        1 -> PermissionPageContent(
                            title = stringResource(
                                if (isAndroid) Res.string.location_dialog_bg_title_android
                                else Res.string.location_dialog_bg_title_ios
                            ),
                            description = stringResource(
                                if (isAndroid) Res.string.location_dialog_bg_desc_android
                                else Res.string.location_dialog_bg_desc_ios
                            ),
                            imageRes = Res.drawable.bus
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                DotsIndicator(
                    totalDots = 2,
                    selectedIndex = pagerState.currentPage
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = if (canContinueAnyway) onSkipClicked else onDismiss) {
                        Text(
                            text = stringResource(
                                if (canContinueAnyway) Res.string.continue_text else Res.string.close
                            ),
                            color = AppColors.textSecondary
                        )
                    }

                    Button(
                        onClick = onContinueClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.accent,
                            contentColor = AppColors.onAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(Res.string.open_settings))
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionPageContent(
    title: String,
    description: String,
    imageRes: DrawableResource
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(AppColors.accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(resource = imageRes),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(AppColors.accent)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val color = if (index == selectedIndex) AppColors.accent else AppColors.border
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(width = if (index == selectedIndex) 20.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

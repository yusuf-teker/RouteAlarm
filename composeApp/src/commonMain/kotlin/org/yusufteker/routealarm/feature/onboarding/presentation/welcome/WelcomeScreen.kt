package org.yusufteker.routealarm.feature.onboarding.presentation.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography
import org.yusufteker.routealarm.core.presentation.button.PrimaryButton
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.app_name
import routealarm.composeapp.generated.resources.get_started
import routealarm.composeapp.generated.resources.never_miss_stop
import routealarm.composeapp.generated.resources.welcomeNoBackground
import routealarm.composeapp.generated.resources.welcome_feature_1
import routealarm.composeapp.generated.resources.welcome_feature_2
import routealarm.composeapp.generated.resources.welcome_feature_3

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.background,
                        Color(0xFF10253B),
                        AppColors.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AppColors.accent.copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(resource = Res.drawable.welcomeNoBackground),
                        contentDescription = stringResource(Res.string.app_name),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(180.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(Res.string.app_name),
                    style = AppTypography.headlineLarge.copy(fontSize = 32.sp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.never_miss_stop),
                    style = AppTypography.bodyRegular.copy(
                        color = AppColors.textSecondary,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppColors.cardBackground.copy(alpha = 0.85f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = AppColors.border,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WelcomeFeatureRow(
                            icon = Icons.Default.Place,
                            text = stringResource(Res.string.welcome_feature_1)
                        )
                        WelcomeFeatureRow(
                            icon = Icons.Default.LocationOn,
                            text = stringResource(Res.string.welcome_feature_2)
                        )
                        WelcomeFeatureRow(
                            icon = Icons.Default.Notifications,
                            text = stringResource(Res.string.welcome_feature_3)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(Res.string.get_started),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WelcomeFeatureRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = AppTypography.bodyRegular.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        )
    }
}

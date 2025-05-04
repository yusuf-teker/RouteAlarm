package org.yusufteker.routealarm.feature.onboarding.presentation.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.WelcomeBackgroundColor
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.welcomeNoBackground


@Composable
fun WelcomeScreen(
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(color = WelcomeBackgroundColor).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo / Görsel
            Image(
                painter = painterResource(resource = Res.drawable.welcomeNoBackground), // kendi görselinle değiştir
                contentDescription = "App Logo", modifier = Modifier.size(480.dp)
            )
            // App İsmi
            Text(
                text = "Route Alarm", style = TextStyle.Default.copy(
                    color = AppColors.textPrimary,
                    fontSize = 48.sp,
                ), fontWeight = FontWeight.Bold
            )

            // Açıklama
            Text(
                modifier = Modifier.padding(horizontal = 64.dp),
                text = "Never miss your stop again.",
                style = TextStyle.Default.copy(
                    color = AppColors.textPrimary,
                    fontSize = 24.sp,
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Devam Buton
            Button(
                modifier = Modifier.width(360.dp).padding(bottom = 72.dp),
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.buttonBackground,
                    contentColor = AppColors.buttonText
                ),
            ){
                Text(
                    text = "Get started.",
                    style = TextStyle.Default.copy(
                        fontSize = 24.sp,
                        color = AppColors.buttonText
                    )

                )
            }


        }
    }
}

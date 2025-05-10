package org.yusufteker.routealarm.core.presentation.button

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.yusufteker.routealarm.core.presentation.AppColors


@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: TextUnit = 24.sp,
) {
    Button(
        modifier = modifier.padding(16.dp),
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.buttonBackground,
            contentColor = AppColors.buttonText,
            disabledContainerColor = AppColors.buttonBackground.copy(alpha = 0.5f),
            disabledContentColor = AppColors.buttonText.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text, style = TextStyle.Default.copy(
                fontSize = fontSize, color = AppColors.buttonText
            )
        )
    }
}
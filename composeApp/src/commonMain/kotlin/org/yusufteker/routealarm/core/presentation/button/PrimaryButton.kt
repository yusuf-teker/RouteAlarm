package org.yusufteker.routealarm.core.presentation.button

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    fontSize: TextUnit = 16.sp,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = AppColors.buttonBackground,
        contentColor = AppColors.buttonText,
        disabledContainerColor = AppColors.buttonBackground.copy(alpha = 0.4f),
        disabledContentColor = AppColors.buttonText.copy(alpha = 0.5f)
    )
) {
    Button(
        modifier = modifier.heightIn(min = 52.dp),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = colors
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) colors.contentColor else colors.disabledContentColor
            )
        )
    }
}

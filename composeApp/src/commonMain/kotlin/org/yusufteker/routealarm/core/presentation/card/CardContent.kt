package org.yusufteker.routealarm.core.presentation.card


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.core.presentation.AppTypography

enum class LayoutOrientation { VERTICAL, HORIZONTAL }


sealed class CardContent {
    data class TextContent(val text: String) : CardContent()
    data class EditableTextContent(
        val value: String, val onValueChange: (String) -> Unit, val placeholder: String = "", val textStyle: TextStyle = AppTypography.titleLarge,
        val modifier: Modifier = Modifier,
    ) : CardContent()

    data class ImageContent(
        val painter: Painter,
        val contentDescription: String? = null,
        val modifier: Modifier = Modifier
    ) : CardContent()

    data class CombinedContent(
        val items: List<CardContent>, val spacing: Dp = 8.dp,
        val orientation: LayoutOrientation = LayoutOrientation.VERTICAL

    ) : CardContent()

    data class CustomComposable(val content: @Composable () -> Unit) : CardContent()

}

@Composable
fun AdaptiveCard(
    content: CardContent,
    modifier: Modifier = Modifier.background(Color.Transparent),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    shape: Shape = MaterialTheme.shapes.medium,
    colors: CardColors = CardDefaults.cardColors(),
    padding: PaddingValues = PaddingValues()
) {
    Card(
        modifier = modifier, elevation = elevation, shape = shape, colors = colors
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(AppColors.cardBackground).padding(padding)
        ) {
            when (content) {
                is CardContent.TextContent -> TextContent(content)

                is CardContent.EditableTextContent -> EditableTextContent(content)

                is CardContent.ImageContent -> ImageContent(content)

                is CardContent.CombinedContent -> CombinedContent(content)

                is CardContent.CustomComposable -> content.content
            }
        }
    }
}


@Composable
private fun TextContent(content: CardContent.TextContent) {
    Text(
        modifier = Modifier.background(AppColors.cardBackground).fillMaxWidth(),
        text = content.text,
        style = AppTypography.bodyRegular.copy()


    )
}

@Composable
private fun EditableTextContent(content: CardContent.EditableTextContent) {
    TextField(
        modifier = content.modifier.fillMaxWidth().background(AppColors.cardBackground),
        value = content.value,
        onValueChange = content.onValueChange,
        placeholder = {
            Text(
                text = content.placeholder,
                color = AppColors.textSecondary,
                style = AppTypography.titleLarge
            )
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = AppColors.textPrimary,
            unfocusedTextColor = AppColors.textPrimary,
            cursorColor = AppColors.green,
            focusedContainerColor = AppColors.cardBackground,
            unfocusedContainerColor = AppColors.cardBackground,
            focusedIndicatorColor = AppColors.cardBackground,
            unfocusedIndicatorColor = AppColors.cardBackground,
            disabledIndicatorColor = AppColors.cardBackground,
            errorIndicatorColor = AppColors.cardBackground
        ),
        textStyle = content.textStyle,
        singleLine = true
    )
}

@Composable
private fun ImageContent(content: CardContent.ImageContent) {
    Image(
        painter = content.painter,
        contentDescription = content.contentDescription,
        modifier = content.modifier
    )
}

@Composable
private fun CombinedContent(content: CardContent.CombinedContent) {
    if (content.orientation == LayoutOrientation.VERTICAL){
        Column(
            verticalArrangement = Arrangement.spacedBy(content.spacing)
        ) {
            content.items.forEach { item ->
                when (item) {
                    is CardContent.TextContent -> TextContent(item)
                    is CardContent.EditableTextContent -> EditableTextContent(item)
                    is CardContent.ImageContent -> ImageContent(item)
                    is CardContent.CombinedContent -> CombinedContent(item)
                    is CardContent.CustomComposable -> item.content()
                }
            }
        }
    }else{
        Row(
            horizontalArrangement = Arrangement.spacedBy(content.spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content.items.forEach { item ->
                when (item) {
                    is CardContent.TextContent -> TextContent(item)
                    is CardContent.EditableTextContent -> EditableTextContent(item)
                    is CardContent.ImageContent -> ImageContent(item)
                    is CardContent.CombinedContent -> CombinedContent(item)
                    is CardContent.CustomComposable -> item.content()
                }
            }
        }
    }

}
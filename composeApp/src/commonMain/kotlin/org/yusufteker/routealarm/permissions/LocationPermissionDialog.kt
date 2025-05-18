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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.bus
import routealarm.composeapp.generated.resources.map_pin


@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onContinueClicked: () -> Unit
) {
    val pagerState = rememberPagerState { 2 }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(state = pagerState, modifier = Modifier.height(240.dp)) { page ->
                    when (page) {
                        0 -> PermissionPageContent(
                            title = "Konum Erişimi Gerekli",
                            description = "Bu uygulama, rotanı takip edip sana zamanında uyarı verebilmek için konumuna erişmeli.",
                            imageRes = Res.drawable.map_pin // daha sonra değiştirirsin
                        )
                        1 -> PermissionPageContent(
                            title = "Arka Planda Konum",
                            description = "Duraktan uzaklaşırsan ya da ekran kapalıyken bile seni bilgilendirmemiz için arka planda konum gerekir.",
                            imageRes = Res.drawable.bus
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DotsIndicator(
                    totalDots = 2,
                    selectedIndex = pagerState.currentPage
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Kapat")
                    }

                    Button(
                        onClick = onContinueClicked
                    ) {
                        Text("Devam Et")
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
        Image(
            painter = painterResource(resource = imageRes),
            contentDescription = null,
            modifier = Modifier
                .height(120.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
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
            val color = if (index == selectedIndex) Color.Black else Color.LightGray
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

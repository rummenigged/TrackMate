package com.octopus.edu.core.design.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 57.sp,
                lineHeight = 64.sp,
                fontWeight = FontWeight.W400,
            ),
        displayMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.W400,
            ),
        displaySmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.W400,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.W400,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.W400,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.W400,
            ),
        titleLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.W400,
            ),
        titleMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.W500,
            ),
        titleSmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W500,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.W400,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400,
            ),
        bodySmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.W400,
            ),
        labelLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W500,
                letterSpacing = 0.5.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.W500,
                letterSpacing = 0.5.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.W500,
            ),
    )

@Preview(device = TABLET)
@Composable
private fun TypographyPreview() {
    TrackMateTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            val displayLarge = MaterialTheme.typography.displayLarge.copy(color = Color.Black)
            val displayMedium = MaterialTheme.typography.displayMedium.copy(color = Color.Black)
            val displaySmall = MaterialTheme.typography.displaySmall.copy(color = Color.Black)
            val headlineLarge = MaterialTheme.typography.headlineLarge.copy(color = Color.Black)
            val headlineMedium = MaterialTheme.typography.headlineMedium.copy(color = Color.Black)
            val headlineSmall = MaterialTheme.typography.headlineSmall.copy(color = Color.Black)
            val titleLarge = MaterialTheme.typography.titleLarge.copy(color = Color.Black)
            val titleMedium = MaterialTheme.typography.titleMedium.copy(color = Color.Black)
            val titleSmall = MaterialTheme.typography.titleSmall.copy(color = Color.Black)
            val bodyLarge = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
            val bodyMedium = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            val bodySmall = MaterialTheme.typography.bodySmall.copy(color = Color.Black)
            val labelLarge = MaterialTheme.typography.labelLarge.copy(color = Color.Black)
            val labelMedium = MaterialTheme.typography.labelMedium.copy(color = Color.Black)
            val labelSmall = MaterialTheme.typography.labelSmall.copy(color = Color.Black)

            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                FontInfoRow(
                    typographyName = "Display Large",
                    fontSize = "57sp",
                    textStyle = displayLarge,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Display Medium",
                    fontSize = "45sp",
                    textStyle = displayMedium,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Display Small",
                    fontSize = "36sp",
                    textStyle = displaySmall,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Headline Large",
                    fontSize = "32sp",
                    textStyle = headlineLarge,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Headline Medium",
                    fontSize = "28sp",
                    textStyle = headlineMedium,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Headline Small",
                    fontSize = "24sp",
                    textStyle = headlineSmall,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Title Large",
                    fontSize = "22sp",
                    textStyle = titleLarge,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Title Medium",
                    fontSize = "16sp",
                    textStyle = titleMedium,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Title Small",
                    fontSize = "14sp",
                    textStyle = titleSmall,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Body Large",
                    fontSize = "16sp",
                    textStyle = bodyLarge,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Body Medium",
                    fontSize = "14sp",
                    textStyle = bodyMedium,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Body Small",
                    fontSize = "12sp",
                    textStyle = bodySmall,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Label Large",
                    fontSize = "14sp",
                    textStyle = labelLarge,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Label Medium",
                    fontSize = "12sp",
                    textStyle = labelMedium,
                )
                Spacer(modifier = Modifier.height(24.dp))
                FontInfoRow(
                    typographyName = "Label Small",
                    fontSize = "11sp",
                    textStyle = labelSmall,
                )
            }
        }
    }
}

@Composable
private fun FontInfoRow(
    typographyName: String,
    fontName: String = "Roboto",
    fontSize: String,
    textStyle: TextStyle,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            typographyName,
            style = textStyle,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            fontName,
            style = textStyle,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            fontSize,
            style = textStyle,
            textAlign = TextAlign.Center,
        )
    }
}

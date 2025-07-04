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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
            ),
        displayMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
            ),
        displaySmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Normal,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
        labelLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontStyle = FontStyle.Normal,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
    )

@Preview(device = Devices.PIXEL)
@Composable
private fun TypographyPreview() {
    TrackMateTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            val h1 = MaterialTheme.typography.displayLarge.copy(color = Color.Black)
            val h2 = MaterialTheme.typography.displayMedium.copy(color = Color.Black)
            val h3 = MaterialTheme.typography.displaySmall.copy(color = Color.Black)
            val h4 = MaterialTheme.typography.headlineLarge.copy(color = Color.Black)
            val h5 = MaterialTheme.typography.headlineMedium.copy(color = Color.Black)
            val h6 = MaterialTheme.typography.headlineSmall.copy(color = Color.Black)
            val body1 = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
            val body2 = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
            val caption1 = MaterialTheme.typography.labelLarge.copy(color = Color.Black)
            val caption2 = MaterialTheme.typography.labelMedium.copy(color = Color.Black)
            val caption3 = MaterialTheme.typography.labelSmall.copy(color = Color.Black)

            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                FontInfoRow(
                    typographyName = "Display Large",
                    fontName = "Roboto Bold",
                    fontSize = "24sp",
                    textStyle = h1,
                )
                Spacer(modifier = Modifier.height(30.dp))
                FontInfoRow(
                    typographyName = "Display Medium",
                    fontName = "Roboto Bold",
                    fontSize = "20sp",
                    textStyle = h2,
                )
                Spacer(modifier = Modifier.height(30.dp))
                FontInfoRow(
                    typographyName = "Display Small",
                    fontName = "Roboto Medium",
                    fontSize = "20sp",
                    textStyle = h3,
                )
                Spacer(modifier = Modifier.height(30.dp))
                FontInfoRow(
                    typographyName = "Headline Large",
                    fontName = "Roboto Medium",
                    fontSize = "18sp",
                    textStyle = h4,
                )
                Spacer(modifier = Modifier.height(30.dp))
                FontInfoRow(
                    typographyName = "Headline Medium",
                    fontName = "Roboto Bold",
                    fontSize = "16sp",
                    textStyle = h5,
                )
                Spacer(modifier = Modifier.height(30.dp))
                FontInfoRow(
                    typographyName = "Headline Small",
                    fontName = "Roboto Medium",
                    fontSize = "14sp",
                    textStyle = h6,
                )
                Spacer(modifier = Modifier.height(30.dp))
                FontInfoRow(
                    typographyName = "Body Large",
                    fontName = "Roboto Regular",
                    fontSize = "14sp",
                    textStyle = body1,
                )
                Spacer(modifier = Modifier.height(30.dp))
                FontInfoRow(
                    typographyName = "Body Medium",
                    fontName = "Roboto Medium",
                    fontSize = "12sp",
                    textStyle = body2,
                )
                Spacer(modifier = Modifier.height(30.dp))
                FontInfoRow(
                    typographyName = "Label Large",
                    fontName = "Roboto Black",
                    fontSize = "12sp",
                    textStyle = caption1,
                )
                Spacer(modifier = Modifier.height(30.dp))
                FontInfoRow(
                    typographyName = "Label Medium",
                    fontName = "Roboto Bold",
                    fontSize = "10sp",
                    textStyle = caption2,
                )
                Spacer(modifier = Modifier.height(30.dp))
                FontInfoRow(
                    typographyName = "Label Small",
                    fontName = "Roboto Medium",
                    fontSize = "10sp",
                    textStyle = caption3,
                )
            }
        }
    }
}

@Composable
private fun FontInfoRow(
    typographyName: String,
    fontName: String,
    fontSize: String,
    textStyle: TextStyle,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Text(
            typographyName,
            style = textStyle,
            modifier = Modifier.width(130.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            fontName,
            style = textStyle,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        Text(
            fontSize,
            style = textStyle,
            modifier = Modifier.width(130.dp),
            textAlign = TextAlign.Center,
        )
    }
}

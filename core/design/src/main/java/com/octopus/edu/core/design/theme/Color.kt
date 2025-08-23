package com.octopus.edu.core.design.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices.TV_1080p
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val primaryLight = Color(0xFF186B52)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFA5F2D3)
val onPrimaryContainerLight = Color(0xFF00513D)
val secondaryLight = Color(0xFF4C6359)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFCEE9DB)
val onSecondaryContainerLight = Color(0xFF354C42)
val tertiaryLight = Color(0xFF3F6375)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFC2E8FD)
val onTertiaryContainerLight = Color(0xFF264B5C)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFF5FBF6)
val onBackgroundLight = Color(0xFF171D1A)
val surfaceLight = Color(0xFFF5FBF6)
val onSurfaceLight = Color(0xFF171D1A)
val surfaceVariantLight = Color(0xFFDBE5DE)
val onSurfaceVariantLight = Color(0xFF404944)
val outlineLight = Color(0xFF707974)
val outlineVariantLight = Color(0xFFBFC9C3)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF2C322F)
val inverseOnSurfaceLight = Color(0xFFECF2ED)
val inversePrimaryLight = Color(0xFF8AD6B8)
val surfaceDimLight = Color(0xFFD5DBD7)
val surfaceBrightLight = Color(0xFFF5FBF6)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFEFF5F0)
val surfaceContainerLight = Color(0xFFE9EFEA)
val surfaceContainerHighLight = Color(0xFFE4EAE5)
val surfaceContainerHighestLight = Color(0xFFDEE4DF)

val primaryLightMediumContrast = Color(0xFF003E2E)
val onPrimaryLightMediumContrast = Color(0xFFFFFFFF)
val primaryContainerLightMediumContrast = Color(0xFF2C7A61)
val onPrimaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val secondaryLightMediumContrast = Color(0xFF243B32)
val onSecondaryLightMediumContrast = Color(0xFFFFFFFF)
val secondaryContainerLightMediumContrast = Color(0xFF5A7268)
val onSecondaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val tertiaryLightMediumContrast = Color(0xFF133A4B)
val onTertiaryLightMediumContrast = Color(0xFFFFFFFF)
val tertiaryContainerLightMediumContrast = Color(0xFF4E7284)
val onTertiaryContainerLightMediumContrast = Color(0xFFFFFFFF)
val errorLightMediumContrast = Color(0xFF740006)
val onErrorLightMediumContrast = Color(0xFFFFFFFF)
val errorContainerLightMediumContrast = Color(0xFFCF2C27)
val onErrorContainerLightMediumContrast = Color(0xFFFFFFFF)
val backgroundLightMediumContrast = Color(0xFFF5FBF6)
val onBackgroundLightMediumContrast = Color(0xFF171D1A)
val surfaceLightMediumContrast = Color(0xFFF5FBF6)
val onSurfaceLightMediumContrast = Color(0xFF0D1210)
val surfaceVariantLightMediumContrast = Color(0xFFDBE5DE)
val onSurfaceVariantLightMediumContrast = Color(0xFF2F3834)
val outlineLightMediumContrast = Color(0xFF4B5550)
val outlineVariantLightMediumContrast = Color(0xFF666F6A)
val scrimLightMediumContrast = Color(0xFF000000)
val inverseSurfaceLightMediumContrast = Color(0xFF2C322F)
val inverseOnSurfaceLightMediumContrast = Color(0xFFECF2ED)
val inversePrimaryLightMediumContrast = Color(0xFF8AD6B8)
val surfaceDimLightMediumContrast = Color(0xFFC2C8C3)
val surfaceBrightLightMediumContrast = Color(0xFFF5FBF6)
val surfaceContainerLowestLightMediumContrast = Color(0xFFFFFFFF)
val surfaceContainerLowLightMediumContrast = Color(0xFFEFF5F0)
val surfaceContainerLightMediumContrast = Color(0xFFE4EAE5)
val surfaceContainerHighLightMediumContrast = Color(0xFFD8DED9)
val surfaceContainerHighestLightMediumContrast = Color(0xFFCDD3CE)

val primaryLightHighContrast = Color(0xFF003325)
val onPrimaryLightHighContrast = Color(0xFFFFFFFF)
val primaryContainerLightHighContrast = Color(0xFF00543F)
val onPrimaryContainerLightHighContrast = Color(0xFFFFFFFF)
val secondaryLightHighContrast = Color(0xFF1A3028)
val onSecondaryLightHighContrast = Color(0xFFFFFFFF)
val secondaryContainerLightHighContrast = Color(0xFF374E44)
val onSecondaryContainerLightHighContrast = Color(0xFFFFFFFF)
val tertiaryLightHighContrast = Color(0xFF043040)
val onTertiaryLightHighContrast = Color(0xFFFFFFFF)
val tertiaryContainerLightHighContrast = Color(0xFF294E5F)
val onTertiaryContainerLightHighContrast = Color(0xFFFFFFFF)
val errorLightHighContrast = Color(0xFF600004)
val onErrorLightHighContrast = Color(0xFFFFFFFF)
val errorContainerLightHighContrast = Color(0xFF98000A)
val onErrorContainerLightHighContrast = Color(0xFFFFFFFF)
val backgroundLightHighContrast = Color(0xFFF5FBF6)
val onBackgroundLightHighContrast = Color(0xFF171D1A)
val surfaceLightHighContrast = Color(0xFFF5FBF6)
val onSurfaceLightHighContrast = Color(0xFF000000)
val surfaceVariantLightHighContrast = Color(0xFFDBE5DE)
val onSurfaceVariantLightHighContrast = Color(0xFF000000)
val outlineLightHighContrast = Color(0xFF252E2A)
val outlineVariantLightHighContrast = Color(0xFF424B47)
val scrimLightHighContrast = Color(0xFF000000)
val inverseSurfaceLightHighContrast = Color(0xFF2C322F)
val inverseOnSurfaceLightHighContrast = Color(0xFFFFFFFF)
val inversePrimaryLightHighContrast = Color(0xFF8AD6B8)
val surfaceDimLightHighContrast = Color(0xFFB4BAB6)
val surfaceBrightLightHighContrast = Color(0xFFF5FBF6)
val surfaceContainerLowestLightHighContrast = Color(0xFFFFFFFF)
val surfaceContainerLowLightHighContrast = Color(0xFFECF2ED)
val surfaceContainerLightHighContrast = Color(0xFFDEE4DF)
val surfaceContainerHighLightHighContrast = Color(0xFFD0D6D1)
val surfaceContainerHighestLightHighContrast = Color(0xFFC2C8C3)

val primaryDark = Color(0xFF8AD6B8)
val onPrimaryDark = Color(0xFF003829)
val primaryContainerDark = Color(0xFF00513D)
val onPrimaryContainerDark = Color(0xFFA5F2D3)
val secondaryDark = Color(0xFFB3CCC0)
val onSecondaryDark = Color(0xFF1E352C)
val secondaryContainerDark = Color(0xFF354C42)
val onSecondaryContainerDark = Color(0xFFCEE9DB)
val tertiaryDark = Color(0xFFA7CCE1)
val onTertiaryDark = Color(0xFF0A3445)
val tertiaryContainerDark = Color(0xFF264B5C)
val onTertiaryContainerDark = Color(0xFFC2E8FD)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF0F1512)
val onBackgroundDark = Color(0xFFDEE4DF)
val surfaceDark = Color(0xFF0F1512)
val onSurfaceDark = Color(0xFFDEE4DF)
val surfaceVariantDark = Color(0xFF404944)
val onSurfaceVariantDark = Color(0xFFBFC9C3)
val outlineDark = Color(0xFF89938D)
val outlineVariantDark = Color(0xFF404944)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFDEE4DF)
val inverseOnSurfaceDark = Color(0xFF2C322F)
val inversePrimaryDark = Color(0xFF186B52)
val surfaceDimDark = Color(0xFF0F1512)
val surfaceBrightDark = Color(0xFF343B37)
val surfaceContainerLowestDark = Color(0xFF0A0F0D)
val surfaceContainerLowDark = Color(0xFF171D1A)
val surfaceContainerDark = Color(0xFF1B211E)
val surfaceContainerHighDark = Color(0xFF252B28)
val surfaceContainerHighestDark = Color(0xFF303633)

val primaryDarkMediumContrast = Color(0xFF9FECCD)
val onPrimaryDarkMediumContrast = Color(0xFF002C1F)
val primaryContainerDarkMediumContrast = Color(0xFF539E83)
val onPrimaryContainerDarkMediumContrast = Color(0xFF000000)
val secondaryDarkMediumContrast = Color(0xFFC8E2D5)
val onSecondaryDarkMediumContrast = Color(0xFF132A21)
val secondaryContainerDarkMediumContrast = Color(0xFF7D968B)
val onSecondaryContainerDarkMediumContrast = Color(0xFF000000)
val tertiaryDarkMediumContrast = Color(0xFFBCE2F7)
val onTertiaryDarkMediumContrast = Color(0xFF002939)
val tertiaryContainerDarkMediumContrast = Color(0xFF7296A9)
val onTertiaryContainerDarkMediumContrast = Color(0xFF000000)
val errorDarkMediumContrast = Color(0xFFFFD2CC)
val onErrorDarkMediumContrast = Color(0xFF540003)
val errorContainerDarkMediumContrast = Color(0xFFFF5449)
val onErrorContainerDarkMediumContrast = Color(0xFF000000)
val backgroundDarkMediumContrast = Color(0xFF0F1512)
val onBackgroundDarkMediumContrast = Color(0xFFDEE4DF)
val surfaceDarkMediumContrast = Color(0xFF0F1512)
val onSurfaceDarkMediumContrast = Color(0xFFFFFFFF)
val surfaceVariantDarkMediumContrast = Color(0xFF404944)
val onSurfaceVariantDarkMediumContrast = Color(0xFFD5DFD8)
val outlineDarkMediumContrast = Color(0xFFAAB4AE)
val outlineVariantDarkMediumContrast = Color(0xFF89938D)
val scrimDarkMediumContrast = Color(0xFF000000)
val inverseSurfaceDarkMediumContrast = Color(0xFFDEE4DF)
val inverseOnSurfaceDarkMediumContrast = Color(0xFF252B28)
val inversePrimaryDarkMediumContrast = Color(0xFF00523E)
val surfaceDimDarkMediumContrast = Color(0xFF0F1512)
val surfaceBrightDarkMediumContrast = Color(0xFF404642)
val surfaceContainerLowestDarkMediumContrast = Color(0xFF040806)
val surfaceContainerLowDarkMediumContrast = Color(0xFF191F1C)
val surfaceContainerDarkMediumContrast = Color(0xFF232926)
val surfaceContainerHighDarkMediumContrast = Color(0xFF2E3431)
val surfaceContainerHighestDarkMediumContrast = Color(0xFF393F3C)

val primaryDarkHighContrast = Color(0xFFB7FFE1)
val onPrimaryDarkHighContrast = Color(0xFF000000)
val primaryContainerDarkHighContrast = Color(0xFF86D2B4)
val onPrimaryContainerDarkHighContrast = Color(0xFF000E08)
val secondaryDarkHighContrast = Color(0xFFDCF6E9)
val onSecondaryDarkHighContrast = Color(0xFF000000)
val secondaryContainerDarkHighContrast = Color(0xFFAFC8BC)
val onSecondaryContainerDarkHighContrast = Color(0xFF000E08)
val tertiaryDarkHighContrast = Color(0xFFE0F3FF)
val onTertiaryDarkHighContrast = Color(0xFF000000)
val tertiaryContainerDarkHighContrast = Color(0xFFA3C8DD)
val onTertiaryContainerDarkHighContrast = Color(0xFF000D14)
val errorDarkHighContrast = Color(0xFFFFECE9)
val onErrorDarkHighContrast = Color(0xFF000000)
val errorContainerDarkHighContrast = Color(0xFFFFAEA4)
val onErrorContainerDarkHighContrast = Color(0xFF220001)
val backgroundDarkHighContrast = Color(0xFF0F1512)
val onBackgroundDarkHighContrast = Color(0xFFDEE4DF)
val surfaceDarkHighContrast = Color(0xFF0F1512)
val onSurfaceDarkHighContrast = Color(0xFFFFFFFF)
val surfaceVariantDarkHighContrast = Color(0xFF404944)
val onSurfaceVariantDarkHighContrast = Color(0xFFFFFFFF)
val outlineDarkHighContrast = Color(0xFFE9F2EC)
val outlineVariantDarkHighContrast = Color(0xFFBBC5BF)
val scrimDarkHighContrast = Color(0xFF000000)
val inverseSurfaceDarkHighContrast = Color(0xFFDEE4DF)
val inverseOnSurfaceDarkHighContrast = Color(0xFF000000)
val inversePrimaryDarkHighContrast = Color(0xFF00523E)
val surfaceDimDarkHighContrast = Color(0xFF0F1512)
val surfaceBrightDarkHighContrast = Color(0xFF4B514E)
val surfaceContainerLowestDarkHighContrast = Color(0xFF000000)
val surfaceContainerLowDarkHighContrast = Color(0xFF1B211E)
val surfaceContainerDarkHighContrast = Color(0xFF2C322F)
val surfaceContainerHighDarkHighContrast = Color(0xFF373D39)
val surfaceContainerHighestDarkHighContrast = Color(0xFF424845)

val customColorLight = Color(0xFF3A608F)
val onCustomColorLight = Color(0xFFFFFFFF)
val customColorContainerLight = Color(0xFFD3E3FF)
val onCustomColorContainerLight = Color(0xFF204876)

val customColorLightMediumContrast = Color(0xFF063764)
val onCustomColorLightMediumContrast = Color(0xFFFFFFFF)
val customColorContainerLightMediumContrast = Color(0xFF4A6F9F)
val onCustomColorContainerLightMediumContrast = Color(0xFFFFFFFF)

val customColorLightHighContrast = Color(0xFF002D55)
val onCustomColorLightHighContrast = Color(0xFFFFFFFF)
val customColorContainerLightHighContrast = Color(0xFF234A78)
val onCustomColorContainerLightHighContrast = Color(0xFFFFFFFF)

val customColorDark = Color(0xFFA4C9FE)
val onCustomColorDark = Color(0xFF00315D)
val customColorContainerDark = Color(0xFF204876)
val onCustomColorContainerDark = Color(0xFFD3E3FF)

val customColorDarkMediumContrast = Color(0xFFC9DEFF)
val onCustomColorDarkMediumContrast = Color(0xFF00264A)
val customColorContainerDarkMediumContrast = Color(0xFF6E93C5)
val onCustomColorContainerDarkMediumContrast = Color(0xFF000000)

val customColorDarkHighContrast = Color(0xFFE9F0FF)
val onCustomColorDarkHighContrast = Color(0xFF000000)
val customColorContainerDarkHighContrast = Color(0xFFA0C5FA)
val onCustomColorContainerDarkHighContrast = Color(0xFF000B1D)

fun ColorScheme.primaryAndSecondaryGradient(): Brush =
    Brush.verticalGradient(
        colors =
            listOf(
                primary,
                secondary,
                tertiary,
            ),
    )

@Preview(name = "Full Light Color Set", device = TV_1080p)
@Composable
private fun MaterialColorSystemFullLightPreview() {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary",
                listOf(
                    Triple("Primary", primaryLight, onPrimaryLight),
                    Triple("On Primary", onPrimaryLight, primaryLight),
                    Triple("Primary Container", primaryContainerLight, onPrimaryContainerLight),
                    Triple("On Primary Container", onPrimaryContainerLight, primaryContainerLight),
                ),
            )
            ColorGroupSection(
                "Secondary",
                listOf(
                    Triple("Secondary", secondaryLight, onSecondaryLight),
                    Triple("On Secondary", onSecondaryLight, secondaryLight),
                    Triple("Secondary Container", secondaryContainerLight, onSecondaryContainerLight),
                    Triple("On Secondary Container", onSecondaryContainerLight, secondaryContainerLight),
                ),
            )
            ColorGroupSection(
                "Tertiary",
                listOf(
                    Triple("Tertiary", tertiaryLight, onTertiaryLight),
                    Triple("On Tertiary", onTertiaryLight, tertiaryLight),
                    Triple("Tertiary Container", tertiaryContainerLight, onTertiaryContainerLight),
                    Triple("On Tertiary Container", onTertiaryContainerLight, tertiaryContainerLight),
                ),
            )
            ColorGroupSection(
                "Error",
                listOf(
                    Triple("Error", errorLight, onErrorLight),
                    Triple("On Error", onErrorLight, errorLight),
                    Triple("Error Container", errorContainerLight, onErrorContainerLight),
                    Triple("On Error Container", onErrorContainerLight, errorContainerLight),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary Fixed",
                listOf(
                    Triple("Primary Fixed", primaryContainerLight, onPrimaryContainerLight),
                    Triple("Primary Fixed Dim", primaryLight, onPrimaryLight),
                    Triple("On Primary Fixed", onPrimaryContainerLight, primaryContainerLight),
                    Triple("On Primary Fixed Variant", onPrimaryLight, primaryLight),
                ),
            )
            ColorGroupSection(
                "Secondary Fixed",
                listOf(
                    Triple("Secondary Fixed", secondaryContainerLight, onSecondaryContainerLight),
                    Triple("Secondary Fixed Dim", secondaryLight, onSecondaryLight),
                    Triple("On Secondary Fixed", onSecondaryContainerLight, secondaryContainerLight),
                    Triple("On Secondary Fixed Variant", onSecondaryLight, secondaryLight),
                ),
            )
            ColorGroupSection(
                "Tertiary Fixed",
                listOf(
                    Triple("Tertiary Fixed", tertiaryContainerLight, onTertiaryContainerLight),
                    Triple("Tertiary Fixed Dim", tertiaryLight, onTertiaryLight),
                    Triple("On Tertiary Fixed", onTertiaryContainerLight, tertiaryContainerLight),
                    Triple("On Tertiary Fixed Variant", onTertiaryLight, tertiaryLight),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Surfaces",
                listOf(
                    Triple("Surface Dim", surfaceDimLight, onSurfaceLight),
                    Triple("Surface", surfaceLight, onSurfaceLight),
                    Triple("Surface Bright", surfaceBrightLight, onSurfaceLight),
                ),
            )

            ColorGroupSection(
                "Surface Containers",
                listOf(
                    Triple("Lowest", surfaceContainerLowestLight, onSurfaceLight),
                    Triple("Low", surfaceContainerLowLight, onSurfaceLight),
                    Triple("Container", surfaceContainerLight, onSurfaceLight),
                    Triple("High", surfaceContainerHighLight, onSurfaceLight),
                    Triple("Highest", surfaceContainerHighestLight, onSurfaceLight),
                ),
            )

            ColorGroupSection(
                "On Surface & Outline",
                listOf(
                    Triple("On Surface", onSurfaceLight, surfaceLight),
                    Triple("On Surface Variant", onSurfaceVariantLight, surfaceVariantLight),
                    Triple("Outline", outlineLight, onSurfaceLight),
                    Triple("Outline Variant", outlineVariantLight, onSurfaceLight),
                ),
            )

            ColorGroupSection(
                "Misc",
                listOf(
                    Triple("Inverse Surface", inverseSurfaceLight, inverseOnSurfaceLight),
                    Triple("Inverse On Surface", inverseOnSurfaceLight, inverseSurfaceLight),
                    Triple("Inverse Primary", inversePrimaryLight, onPrimaryLight),
                    Triple("Scrim", scrimLight, onPrimaryLight),
                ),
            )
        }
    }
}

@Preview(name = "Full Dark Color Set", device = TV_1080p)
@Composable
private fun MaterialColorSystemFullDarkPreview() {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary",
                listOf(
                    Triple("Primary", primaryDark, onPrimaryDark),
                    Triple("On Primary", onPrimaryDark, primaryDark),
                    Triple("Primary Container", primaryContainerDark, onPrimaryContainerDark),
                    Triple("On Primary Container", onPrimaryContainerDark, primaryContainerDark),
                ),
            )
            ColorGroupSection(
                "Secondary",
                listOf(
                    Triple("Secondary", secondaryDark, onSecondaryDark),
                    Triple("On Secondary", onSecondaryDark, secondaryDark),
                    Triple("Secondary Container", secondaryContainerDark, onSecondaryContainerDark),
                    Triple("On Secondary Container", onSecondaryContainerDark, secondaryContainerDark),
                ),
            )
            ColorGroupSection(
                "Tertiary",
                listOf(
                    Triple("Tertiary", tertiaryDark, onTertiaryDark),
                    Triple("On Tertiary", onTertiaryDark, tertiaryDark),
                    Triple("Tertiary Container", tertiaryContainerDark, onTertiaryContainerDark),
                    Triple("On Tertiary Container", onTertiaryContainerDark, tertiaryContainerDark),
                ),
            )
            ColorGroupSection(
                "Error",
                listOf(
                    Triple("Error", errorDark, onErrorDark),
                    Triple("On Error", onErrorDark, errorDark),
                    Triple("Error Container", errorContainerDark, onErrorContainerDark),
                    Triple("On Error Container", onErrorContainerDark, errorContainerDark),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary Fixed",
                listOf(
                    Triple("Primary Fixed", primaryContainerDark, onPrimaryContainerDark),
                    Triple("Primary Fixed Dim", primaryDark, onPrimaryDark),
                    Triple("On Primary Fixed", onPrimaryContainerDark, primaryContainerDark),
                    Triple("On Primary Fixed Variant", onPrimaryDark, primaryDark),
                ),
            )
            ColorGroupSection(
                "Secondary Fixed",
                listOf(
                    Triple("Secondary Fixed", secondaryContainerDark, onSecondaryContainerDark),
                    Triple("Secondary Fixed Dim", secondaryDark, onSecondaryDark),
                    Triple("On Secondary Fixed", onSecondaryContainerDark, secondaryContainerDark),
                    Triple("On Secondary Fixed Variant", onSecondaryDark, secondaryDark),
                ),
            )
            ColorGroupSection(
                "Tertiary Fixed",
                listOf(
                    Triple("Tertiary Fixed", tertiaryContainerDark, onTertiaryContainerDark),
                    Triple("Tertiary Fixed Dim", tertiaryDark, onTertiaryDark),
                    Triple("On Tertiary Fixed", onTertiaryContainerDark, tertiaryContainerDark),
                    Triple("On Tertiary Fixed Variant", onTertiaryDark, tertiaryDark),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Surfaces",
                listOf(
                    Triple("Surface Dim", surfaceDimDark, onSurfaceDark),
                    Triple("Surface", surfaceDark, onSurfaceDark),
                    Triple("Surface Bright", surfaceBrightDark, onSurfaceDark),
                ),
            )

            ColorGroupSection(
                "Surface Containers",
                listOf(
                    Triple("Lowest", surfaceContainerLowestDark, onSurfaceDark),
                    Triple("Low", surfaceContainerLowDark, onSurfaceDark),
                    Triple("Container", surfaceContainerDark, onSurfaceDark),
                    Triple("High", surfaceContainerHighDark, onSurfaceDark),
                    Triple("Highest", surfaceContainerHighestDark, onSurfaceDark),
                ),
            )

            ColorGroupSection(
                "On Surface & Outline",
                listOf(
                    Triple("On Surface", onSurfaceDark, surfaceDark),
                    Triple("On Surface Variant", onSurfaceVariantDark, surfaceVariantDark),
                    Triple("Outline", outlineDark, onSurfaceDark),
                    Triple("Outline Variant", outlineVariantDark, onSurfaceDark),
                ),
            )

            ColorGroupSection(
                "Misc",
                listOf(
                    Triple("Inverse Surface", inverseSurfaceDark, inverseOnSurfaceDark),
                    Triple("Inverse On Surface", inverseOnSurfaceDark, inverseSurfaceDark),
                    Triple("Inverse Primary", inversePrimaryDark, onPrimaryDark),
                    Triple("Scrim", scrimDark, onPrimaryDark),
                ),
            )
        }
    }
}

@Preview(name = "Full Light Medium Contrast Color Set", device = TV_1080p)
@Composable
private fun MaterialColorSystemMediumContrastLightPreview() {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary",
                listOf(
                    Triple("Primary", primaryLightMediumContrast, onPrimaryLightMediumContrast),
                    Triple("On Primary", onPrimaryLightMediumContrast, primaryLightMediumContrast),
                    Triple("Primary Container", primaryContainerLightMediumContrast, onPrimaryContainerLightMediumContrast),
                    Triple("On Primary Container", onPrimaryContainerLightMediumContrast, primaryContainerLightMediumContrast),
                ),
            )
            ColorGroupSection(
                "Secondary",
                listOf(
                    Triple("Secondary", secondaryLightMediumContrast, onSecondaryLightMediumContrast),
                    Triple("On Secondary", onSecondaryLightMediumContrast, secondaryLightMediumContrast),
                    Triple("Secondary Container", secondaryContainerLightMediumContrast, onSecondaryContainerLightMediumContrast),
                    Triple("On Secondary Container", onSecondaryContainerLightMediumContrast, secondaryContainerLightMediumContrast),
                ),
            )
            ColorGroupSection(
                "Tertiary",
                listOf(
                    Triple("Tertiary", tertiaryLightMediumContrast, onTertiaryLightMediumContrast),
                    Triple("On Tertiary", onTertiaryLightMediumContrast, tertiaryLightMediumContrast),
                    Triple("Tertiary Container", tertiaryContainerLightMediumContrast, onTertiaryContainerLightMediumContrast),
                    Triple("On Tertiary Container", onTertiaryContainerLightMediumContrast, tertiaryContainerLightMediumContrast),
                ),
            )
            ColorGroupSection(
                "Error",
                listOf(
                    Triple("Error", errorLightMediumContrast, onErrorLightMediumContrast),
                    Triple("On Error", onErrorLightMediumContrast, errorLightMediumContrast),
                    Triple("Error Container", errorContainerLightMediumContrast, onErrorContainerLightMediumContrast),
                    Triple("On Error Container", onErrorContainerLightMediumContrast, errorContainerLightMediumContrast),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary Fixed",
                listOf(
                    Triple("Primary Fixed", primaryContainerLightMediumContrast, onPrimaryContainerLightMediumContrast),
                    Triple("Primary Fixed Dim", primaryLightMediumContrast, onPrimaryLightMediumContrast),
                    Triple("On Primary Fixed", onPrimaryContainerLightMediumContrast, primaryContainerLightMediumContrast),
                    Triple("On Primary Fixed Variant", onPrimaryLightMediumContrast, primaryLightMediumContrast),
                ),
            )
            ColorGroupSection(
                "Secondary Fixed",
                listOf(
                    Triple("Secondary Fixed", secondaryContainerLightMediumContrast, onSecondaryContainerLightMediumContrast),
                    Triple("Secondary Fixed Dim", secondaryLightMediumContrast, onSecondaryLightMediumContrast),
                    Triple("On Secondary Fixed", onSecondaryContainerLightMediumContrast, secondaryContainerLightMediumContrast),
                    Triple("On Secondary Fixed Variant", onSecondaryLightMediumContrast, secondaryLightMediumContrast),
                ),
            )
            ColorGroupSection(
                "Tertiary Fixed",
                listOf(
                    Triple("Tertiary Fixed", tertiaryContainerLightMediumContrast, onTertiaryContainerLightMediumContrast),
                    Triple("Tertiary Fixed Dim", tertiaryLightMediumContrast, onTertiaryLightMediumContrast),
                    Triple("On Tertiary Fixed", onTertiaryContainerLightMediumContrast, tertiaryContainerLightMediumContrast),
                    Triple("On Tertiary Fixed Variant", onTertiaryLightMediumContrast, tertiaryLightMediumContrast),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Surfaces",
                listOf(
                    Triple("Surface Dim", surfaceDimLightMediumContrast, onSurfaceLightMediumContrast),
                    Triple("Surface", surfaceLightMediumContrast, onSurfaceLightMediumContrast),
                    Triple("Surface Bright", surfaceBrightLightMediumContrast, onSurfaceLightMediumContrast),
                ),
            )

            ColorGroupSection(
                "Surface Containers",
                listOf(
                    Triple("Lowest", surfaceContainerLowestLightMediumContrast, onSurfaceLightMediumContrast),
                    Triple("Low", surfaceContainerLowLightMediumContrast, onSurfaceLightMediumContrast),
                    Triple("Container", surfaceContainerLightMediumContrast, onSurfaceLightMediumContrast),
                    Triple("High", surfaceContainerHighLightMediumContrast, onSurfaceLightMediumContrast),
                    Triple("Highest", surfaceContainerHighestLightMediumContrast, onSurfaceLightMediumContrast),
                ),
            )

            ColorGroupSection(
                "On Surface & Outline",
                listOf(
                    Triple("On Surface", onSurfaceLightMediumContrast, surfaceLightMediumContrast),
                    Triple("On Surface Variant", onSurfaceVariantLightMediumContrast, surfaceVariantLightMediumContrast),
                    Triple("Outline", outlineLightMediumContrast, onSurfaceLightMediumContrast),
                    Triple("Outline Variant", outlineVariantLightMediumContrast, onSurfaceLightMediumContrast),
                ),
            )

            ColorGroupSection(
                "Misc",
                listOf(
                    Triple("Inverse Surface", inverseSurfaceLightMediumContrast, inverseOnSurfaceLightMediumContrast),
                    Triple("Inverse On Surface", inverseOnSurfaceLightMediumContrast, inverseSurfaceLightMediumContrast),
                    Triple("Inverse Primary", inversePrimaryLightMediumContrast, onPrimaryLightMediumContrast),
                    Triple("Scrim", scrimLightMediumContrast, onPrimaryLightMediumContrast),
                ),
            )
        }
    }
}

@Preview(name = "Full Dark Medium Contrast Color Set", device = TV_1080p)
@Composable
private fun MaterialColorSystemMediumContrastDarkPreview() {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary",
                listOf(
                    Triple("Primary", primaryDarkMediumContrast, onPrimaryDarkMediumContrast),
                    Triple("On Primary", onPrimaryDarkMediumContrast, primaryDarkMediumContrast),
                    Triple("Primary Container", primaryContainerDarkMediumContrast, onPrimaryContainerDarkMediumContrast),
                    Triple("On Primary Container", onPrimaryContainerDarkMediumContrast, primaryContainerDarkMediumContrast),
                ),
            )
            ColorGroupSection(
                "Secondary",
                listOf(
                    Triple("Secondary", secondaryDarkMediumContrast, onSecondaryDarkMediumContrast),
                    Triple("On Secondary", onSecondaryDarkMediumContrast, secondaryDarkMediumContrast),
                    Triple("Secondary Container", secondaryContainerDarkMediumContrast, onSecondaryContainerDarkMediumContrast),
                    Triple("On Secondary Container", onSecondaryContainerDarkMediumContrast, secondaryContainerDarkMediumContrast),
                ),
            )
            ColorGroupSection(
                "Tertiary",
                listOf(
                    Triple("Tertiary", tertiaryDarkMediumContrast, onTertiaryDarkMediumContrast),
                    Triple("On Tertiary", onTertiaryDarkMediumContrast, tertiaryDarkMediumContrast),
                    Triple("Tertiary Container", tertiaryContainerDarkMediumContrast, onTertiaryContainerDarkMediumContrast),
                    Triple("On Tertiary Container", onTertiaryContainerDarkMediumContrast, tertiaryContainerDarkMediumContrast),
                ),
            )
            ColorGroupSection(
                "Error",
                listOf(
                    Triple("Error", errorDarkMediumContrast, onErrorDarkMediumContrast),
                    Triple("On Error", onErrorDarkMediumContrast, errorDarkMediumContrast),
                    Triple("Error Container", errorContainerDarkMediumContrast, onErrorContainerDarkMediumContrast),
                    Triple("On Error Container", onErrorContainerDarkMediumContrast, errorContainerDarkMediumContrast),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary Fixed",
                listOf(
                    Triple("Primary Fixed", primaryContainerDarkMediumContrast, onPrimaryContainerDarkMediumContrast),
                    Triple("Primary Fixed Dim", primaryDarkMediumContrast, onPrimaryDarkMediumContrast),
                    Triple("On Primary Fixed", onPrimaryContainerDarkMediumContrast, primaryContainerDarkMediumContrast),
                    Triple("On Primary Fixed Variant", onPrimaryDarkMediumContrast, primaryDarkMediumContrast),
                ),
            )
            ColorGroupSection(
                "Secondary Fixed",
                listOf(
                    Triple("Secondary Fixed", secondaryContainerDarkMediumContrast, onSecondaryContainerDarkMediumContrast),
                    Triple("Secondary Fixed Dim", secondaryDarkMediumContrast, onSecondaryDarkMediumContrast),
                    Triple("On Secondary Fixed", onSecondaryContainerDarkMediumContrast, secondaryContainerDarkMediumContrast),
                    Triple("On Secondary Fixed Variant", onSecondaryDarkMediumContrast, secondaryDarkMediumContrast),
                ),
            )
            ColorGroupSection(
                "Tertiary Fixed",
                listOf(
                    Triple("Tertiary Fixed", tertiaryContainerDarkMediumContrast, onTertiaryContainerDarkMediumContrast),
                    Triple("Tertiary Fixed Dim", tertiaryDarkMediumContrast, onTertiaryDarkMediumContrast),
                    Triple("On Tertiary Fixed", onTertiaryContainerDarkMediumContrast, tertiaryContainerDarkMediumContrast),
                    Triple("On Tertiary Fixed Variant", onTertiaryDarkMediumContrast, tertiaryDarkMediumContrast),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Surfaces",
                listOf(
                    Triple("Surface Dim", surfaceDimDarkMediumContrast, onSurfaceDarkMediumContrast),
                    Triple("Surface", surfaceDarkMediumContrast, onSurfaceDarkMediumContrast),
                    Triple("Surface Bright", surfaceBrightDarkMediumContrast, onSurfaceDarkMediumContrast),
                ),
            )

            ColorGroupSection(
                "Surface Containers",
                listOf(
                    Triple("Lowest", surfaceContainerLowestDarkMediumContrast, onSurfaceDarkMediumContrast),
                    Triple("Low", surfaceContainerLowDarkMediumContrast, onSurfaceDarkMediumContrast),
                    Triple("Container", surfaceContainerDarkMediumContrast, onSurfaceDarkMediumContrast),
                    Triple("High", surfaceContainerHighDarkMediumContrast, onSurfaceDarkMediumContrast),
                    Triple("Highest", surfaceContainerHighestDarkMediumContrast, onSurfaceDarkMediumContrast),
                ),
            )

            ColorGroupSection(
                "On Surface & Outline",
                listOf(
                    Triple("On Surface", onSurfaceDarkMediumContrast, surfaceDarkMediumContrast),
                    Triple("On Surface Variant", onSurfaceVariantDarkMediumContrast, surfaceVariantDarkMediumContrast),
                    Triple("Outline", outlineDarkMediumContrast, onSurfaceDarkMediumContrast),
                    Triple("Outline Variant", outlineVariantDarkMediumContrast, onSurfaceDarkMediumContrast),
                ),
            )

            ColorGroupSection(
                "Misc",
                listOf(
                    Triple("Inverse Surface", inverseSurfaceDarkMediumContrast, inverseOnSurfaceDarkMediumContrast),
                    Triple("Inverse On Surface", inverseOnSurfaceDarkMediumContrast, inverseSurfaceDarkMediumContrast),
                    Triple("Inverse Primary", inversePrimaryDarkMediumContrast, onPrimaryDarkMediumContrast),
                    Triple("Scrim", scrimDarkMediumContrast, onPrimaryDarkMediumContrast),
                ),
            )
        }
    }
}

@Preview(name = "Full Light High Contrast Color Set", device = TV_1080p)
@Composable
private fun MaterialColorSystemHighContrastLightPreview() {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary",
                listOf(
                    Triple("Primary", primaryLightHighContrast, onPrimaryLightHighContrast),
                    Triple("On Primary", onPrimaryLightHighContrast, primaryLightHighContrast),
                    Triple("Primary Container", primaryContainerLightHighContrast, onPrimaryContainerLightHighContrast),
                    Triple("On Primary Container", onPrimaryContainerLightHighContrast, primaryContainerLightHighContrast),
                ),
            )
            ColorGroupSection(
                "Secondary",
                listOf(
                    Triple("Secondary", secondaryLightHighContrast, onSecondaryLightHighContrast),
                    Triple("On Secondary", onSecondaryLightHighContrast, secondaryLightHighContrast),
                    Triple("Secondary Container", secondaryContainerLightHighContrast, onSecondaryContainerLightHighContrast),
                    Triple("On Secondary Container", onSecondaryContainerLightHighContrast, secondaryContainerLightHighContrast),
                ),
            )
            ColorGroupSection(
                "Tertiary",
                listOf(
                    Triple("Tertiary", tertiaryLightHighContrast, onTertiaryLightHighContrast),
                    Triple("On Tertiary", onTertiaryLightHighContrast, tertiaryLightHighContrast),
                    Triple("Tertiary Container", tertiaryContainerLightHighContrast, onTertiaryContainerLightHighContrast),
                    Triple("On Tertiary Container", onTertiaryContainerLightHighContrast, tertiaryContainerLightHighContrast),
                ),
            )
            ColorGroupSection(
                "Error",
                listOf(
                    Triple("Error", errorLightHighContrast, onErrorLightHighContrast),
                    Triple("On Error", onErrorLightHighContrast, errorLightHighContrast),
                    Triple("Error Container", errorContainerLightHighContrast, onErrorContainerLightHighContrast),
                    Triple("On Error Container", onErrorContainerLightHighContrast, errorContainerLightHighContrast),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary Fixed",
                listOf(
                    Triple("Primary Fixed", primaryContainerLightHighContrast, onPrimaryContainerLightHighContrast),
                    Triple("Primary Fixed Dim", primaryLightHighContrast, onPrimaryLightHighContrast),
                    Triple("On Primary Fixed", onPrimaryContainerLightHighContrast, primaryContainerLightHighContrast),
                    Triple("On Primary Fixed Variant", onPrimaryLightHighContrast, primaryLightHighContrast),
                ),
            )
            ColorGroupSection(
                "Secondary Fixed",
                listOf(
                    Triple("Secondary Fixed", secondaryContainerLightHighContrast, onSecondaryContainerLightHighContrast),
                    Triple("Secondary Fixed Dim", secondaryLightHighContrast, onSecondaryLightHighContrast),
                    Triple("On Secondary Fixed", onSecondaryContainerLightHighContrast, secondaryContainerLightHighContrast),
                    Triple("On Secondary Fixed Variant", onSecondaryLightHighContrast, secondaryLightHighContrast),
                ),
            )
            ColorGroupSection(
                "Tertiary Fixed",
                listOf(
                    Triple("Tertiary Fixed", tertiaryContainerLightHighContrast, onTertiaryContainerLightHighContrast),
                    Triple("Tertiary Fixed Dim", tertiaryLightHighContrast, onTertiaryLightHighContrast),
                    Triple("On Tertiary Fixed", onTertiaryContainerLightHighContrast, tertiaryContainerLightHighContrast),
                    Triple("On Tertiary Fixed Variant", onTertiaryLightHighContrast, tertiaryLightHighContrast),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Surfaces",
                listOf(
                    Triple("Surface Dim", surfaceDimLightHighContrast, onSurfaceLightHighContrast),
                    Triple("Surface", surfaceLightHighContrast, onSurfaceLightHighContrast),
                    Triple("Surface Bright", surfaceBrightLightHighContrast, onSurfaceLightHighContrast),
                ),
            )

            ColorGroupSection(
                "Surface Containers",
                listOf(
                    Triple("Lowest", surfaceContainerLowestLightHighContrast, onSurfaceLightHighContrast),
                    Triple("Low", surfaceContainerLowLightHighContrast, onSurfaceLightHighContrast),
                    Triple("Container", surfaceContainerLightHighContrast, onSurfaceLightHighContrast),
                    Triple("High", surfaceContainerHighLightHighContrast, onSurfaceLightHighContrast),
                    Triple("Highest", surfaceContainerHighestLightHighContrast, onSurfaceLightHighContrast),
                ),
            )

            ColorGroupSection(
                "On Surface & Outline",
                listOf(
                    Triple("On Surface", onSurfaceLightHighContrast, surfaceLightHighContrast),
                    Triple("On Surface Variant", onSurfaceVariantLightHighContrast, surfaceVariantLightHighContrast),
                    Triple("Outline", outlineLightHighContrast, onSurfaceLightHighContrast),
                    Triple("Outline Variant", outlineVariantLightHighContrast, onSurfaceLightHighContrast),
                ),
            )

            ColorGroupSection(
                "Misc",
                listOf(
                    Triple("Inverse Surface", inverseSurfaceLightHighContrast, inverseOnSurfaceLightHighContrast),
                    Triple("Inverse On Surface", inverseOnSurfaceLightHighContrast, inverseSurfaceLightHighContrast),
                    Triple("Inverse Primary", inversePrimaryLightHighContrast, onPrimaryLightHighContrast),
                    Triple("Scrim", scrimLightHighContrast, onPrimaryLightHighContrast),
                ),
            )
        }
    }
}

@Preview(name = "Full Dark High Contrast Set", device = TV_1080p)
@Composable
private fun MaterialColorSystemHighContrastDarkPreview() {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary",
                listOf(
                    Triple("Primary", primaryDarkHighContrast, onPrimaryDarkHighContrast),
                    Triple("On Primary", onPrimaryDarkHighContrast, primaryDarkHighContrast),
                    Triple("Primary Container", primaryContainerDarkHighContrast, onPrimaryContainerDarkHighContrast),
                    Triple("On Primary Container", onPrimaryContainerDarkHighContrast, primaryContainerDarkHighContrast),
                ),
            )
            ColorGroupSection(
                "Secondary",
                listOf(
                    Triple("Secondary", secondaryDarkHighContrast, onSecondaryDarkHighContrast),
                    Triple("On Secondary", onSecondaryDarkHighContrast, secondaryDarkHighContrast),
                    Triple("Secondary Container", secondaryContainerDarkHighContrast, onSecondaryContainerDarkHighContrast),
                    Triple("On Secondary Container", onSecondaryContainerDarkHighContrast, secondaryContainerDarkHighContrast),
                ),
            )
            ColorGroupSection(
                "Tertiary",
                listOf(
                    Triple("Tertiary", tertiaryDarkHighContrast, onTertiaryDarkHighContrast),
                    Triple("On Tertiary", onTertiaryDarkHighContrast, tertiaryDarkHighContrast),
                    Triple("Tertiary Container", tertiaryContainerDarkHighContrast, onTertiaryContainerDarkHighContrast),
                    Triple("On Tertiary Container", onTertiaryContainerDarkHighContrast, tertiaryContainerDarkHighContrast),
                ),
            )
            ColorGroupSection(
                "Error",
                listOf(
                    Triple("Error", errorDarkHighContrast, onErrorDarkHighContrast),
                    Triple("On Error", onErrorDarkHighContrast, errorDarkHighContrast),
                    Triple("Error Container", errorContainerDarkHighContrast, onErrorContainerDarkHighContrast),
                    Triple("On Error Container", onErrorContainerDarkHighContrast, errorContainerDarkHighContrast),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Primary Fixed",
                listOf(
                    Triple("Primary Fixed", primaryContainerDarkHighContrast, onPrimaryContainerDarkHighContrast),
                    Triple("Primary Fixed Dim", primaryDarkHighContrast, onPrimaryDarkHighContrast),
                    Triple("On Primary Fixed", onPrimaryContainerDarkHighContrast, primaryContainerDarkHighContrast),
                    Triple("On Primary Fixed Variant", onPrimaryDarkHighContrast, primaryDarkHighContrast),
                ),
            )
            ColorGroupSection(
                "Secondary Fixed",
                listOf(
                    Triple("Secondary Fixed", secondaryContainerDarkHighContrast, onSecondaryContainerDarkHighContrast),
                    Triple("Secondary Fixed Dim", secondaryDarkHighContrast, onSecondaryDarkHighContrast),
                    Triple("On Secondary Fixed", onSecondaryContainerDarkHighContrast, secondaryContainerDarkHighContrast),
                    Triple("On Secondary Fixed Variant", onSecondaryDarkHighContrast, secondaryDarkHighContrast),
                ),
            )
            ColorGroupSection(
                "Tertiary Fixed",
                listOf(
                    Triple("Tertiary Fixed", tertiaryContainerDarkHighContrast, onTertiaryContainerDarkHighContrast),
                    Triple("Tertiary Fixed Dim", tertiaryDarkHighContrast, onTertiaryDarkHighContrast),
                    Triple("On Tertiary Fixed", onTertiaryContainerDarkHighContrast, tertiaryContainerDarkHighContrast),
                    Triple("On Tertiary Fixed Variant", onTertiaryDarkHighContrast, tertiaryDarkHighContrast),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Surfaces",
                listOf(
                    Triple("Surface Dim", surfaceDimDarkHighContrast, onSurfaceDarkHighContrast),
                    Triple("Surface", surfaceDarkHighContrast, onSurfaceDarkHighContrast),
                    Triple("Surface Bright", surfaceBrightDarkHighContrast, onSurfaceDarkHighContrast),
                ),
            )

            ColorGroupSection(
                "Surface Containers",
                listOf(
                    Triple("Lowest", surfaceContainerLowestDarkHighContrast, onSurfaceDarkHighContrast),
                    Triple("Low", surfaceContainerLowDarkHighContrast, onSurfaceDarkHighContrast),
                    Triple("Container", surfaceContainerDarkHighContrast, onSurfaceDarkHighContrast),
                    Triple("High", surfaceContainerHighDarkHighContrast, onSurfaceDarkHighContrast),
                    Triple("Highest", surfaceContainerHighestDarkHighContrast, onSurfaceDarkHighContrast),
                ),
            )

            ColorGroupSection(
                "On Surface & Outline",
                listOf(
                    Triple("On Surface", onSurfaceDarkHighContrast, surfaceDarkHighContrast),
                    Triple("On Surface Variant", onSurfaceVariantDarkHighContrast, surfaceVariantDarkHighContrast),
                    Triple("Outline", outlineDarkHighContrast, onSurfaceDarkHighContrast),
                    Triple("Outline Variant", outlineVariantDarkHighContrast, onSurfaceDarkHighContrast),
                ),
            )

            ColorGroupSection(
                "Misc",
                listOf(
                    Triple("Inverse Surface", inverseSurfaceDarkHighContrast, inverseOnSurfaceDarkHighContrast),
                    Triple("Inverse On Surface", inverseOnSurfaceDarkHighContrast, inverseSurfaceDarkHighContrast),
                    Triple("Inverse Primary", inversePrimaryDarkHighContrast, onPrimaryDarkHighContrast),
                    Triple("Scrim", scrimDarkHighContrast, primaryDarkHighContrast),
                ),
            )
        }
    }
}

@Preview(name = "Full Custom Color Set", device = TV_1080p)
@Composable
private fun MaterialColorSystemFullCustomLightPreview() {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Custom Light",
                listOf(
                    Triple("Custom", customColorLight, onCustomColorLight),
                    Triple("On Custom", onCustomColorLight, customColorLight),
                    Triple("Custom Container", customColorContainerLight, onCustomColorContainerLight),
                    Triple("On Custom Container", onCustomColorContainerLight, customColorContainerLight),
                ),
            )

            ColorGroupSection(
                "Custom Dark",
                listOf(
                    Triple("Custom", customColorDark, onCustomColorDark),
                    Triple("On Custom", onCustomColorDark, customColorDark),
                    Triple("Custom Container", customColorContainerDark, onCustomColorContainerDark),
                    Triple("On Custom Container", onCustomColorContainerDark, customColorContainerLight),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Custom Light Medium Contrast",
                listOf(
                    Triple("Custom", customColorLightMediumContrast, onCustomColorLightMediumContrast),
                    Triple("On Custom", onCustomColorLightMediumContrast, customColorLightMediumContrast),
                    Triple(
                        "Custom Container",
                        customColorContainerLightMediumContrast,
                        onCustomColorContainerLightMediumContrast,
                    ),
                    Triple(
                        "On Custom Container",
                        onCustomColorContainerLightMediumContrast,
                        customColorContainerLightMediumContrast,
                    ),
                ),
            )

            ColorGroupSection(
                "Custom Dark Medium Contrast",
                listOf(
                    Triple("Custom", customColorDarkMediumContrast, onCustomColorDarkMediumContrast),
                    Triple("On Custom", onCustomColorDarkMediumContrast, customColorDarkMediumContrast),
                    Triple(
                        "Custom Container",
                        customColorContainerDarkMediumContrast,
                        onCustomColorContainerDarkMediumContrast,
                    ),
                    Triple(
                        "On Custom Container",
                        onCustomColorContainerDarkMediumContrast,
                        customColorContainerDarkMediumContrast,
                    ),
                ),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorGroupSection(
                "Custom Light High Contrast",
                listOf(
                    Triple("Custom", customColorLightHighContrast, onCustomColorLightHighContrast),
                    Triple("On Custom", onCustomColorLightHighContrast, customColorLightHighContrast),
                    Triple(
                        "Custom Container",
                        customColorContainerLightHighContrast,
                        onCustomColorContainerLightHighContrast,
                    ),
                    Triple(
                        "On Custom Container",
                        onCustomColorContainerLightHighContrast,
                        customColorContainerLightHighContrast,
                    ),
                ),
            )

            ColorGroupSection(
                "Custom Dark High Contrast",
                listOf(
                    Triple("Custom", customColorDarkHighContrast, onCustomColorDarkHighContrast),
                    Triple("On Custom", onCustomColorDarkHighContrast, customColorDarkHighContrast),
                    Triple(
                        "Custom Container",
                        customColorContainerDarkHighContrast,
                        onCustomColorContainerDarkHighContrast,
                    ),
                    Triple(
                        "On Custom Container",
                        onCustomColorContainerDarkHighContrast,
                        customColorContainerDarkHighContrast,
                    ),
                ),
            )
        }
    }
}

@Composable
private fun ColorGroupSection(
    title: String,
    swatches: List<Triple<String, Color, Color>>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Column {
            swatches.chunked(2).forEach { column ->
                Column {
                    column.forEach { (label, bg, fg) ->
                        Box(
                            modifier =
                                Modifier
                                    .height(60.dp)
                                    .width(240.dp)
                                    .background(bg),
                        ) {
                            Text(
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                                text = label,
                                color = fg,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

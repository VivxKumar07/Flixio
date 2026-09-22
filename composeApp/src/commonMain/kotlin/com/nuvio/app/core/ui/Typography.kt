package com.nuvio.app.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.clash_display_bold
import nuvio.composeapp.generated.resources.clash_display_medium
import nuvio.composeapp.generated.resources.clash_display_regular
import nuvio.composeapp.generated.resources.clash_display_semibold
import nuvio.composeapp.generated.resources.jetbrains_sans_bold
import nuvio.composeapp.generated.resources.jetbrains_sans_regular
import nuvio.composeapp.generated.resources.jetbrains_sans_semibold
import org.jetbrains.compose.resources.Font

private var cachedClashDisplayFontFamily: FontFamily? = null
private var cachedBodyFontFamily: FontFamily? = null

/**
 * Clash Display font family - primary display and brand font for Flixio.
 * Used for the Flixio wordmark, major headings, hero titles, and prominent titles.
 */
val ClashDisplayFontFamily: FontFamily
    @Composable
    get() {
        val cached = cachedClashDisplayFontFamily
        if (cached != null) return cached
        val created = FontFamily(
            Font(Res.font.clash_display_bold, FontWeight.Bold, FontStyle.Normal),
            Font(Res.font.clash_display_semibold, FontWeight.SemiBold, FontStyle.Normal),
            Font(Res.font.clash_display_medium, FontWeight.Medium, FontStyle.Normal),
            Font(Res.font.clash_display_regular, FontWeight.Normal, FontStyle.Normal),
        )
        cachedClashDisplayFontFamily = created
        return created
    }

/**
 * Highly readable body font family for paragraphs, lists, and small metadata.
 */
val BodyFontFamily: FontFamily
    @Composable
    get() {
        val cached = cachedBodyFontFamily
        if (cached != null) return cached
        val created = FontFamily(
            Font(Res.font.jetbrains_sans_bold, FontWeight.Bold, FontStyle.Normal),
            Font(Res.font.jetbrains_sans_semibold, FontWeight.SemiBold, FontStyle.Normal),
            Font(Res.font.jetbrains_sans_regular, FontWeight.Normal, FontStyle.Normal),
        )
        cachedBodyFontFamily = created
        return created
    }

/**
 * Secondary / body typography family for UI and supporting text.
 */
val ManropeFontFamily: FontFamily
    @Composable
    get() = BodyFontFamily

/**
 * Single source of truth for the application's primary brand font family.
 */
val AppFontFamily: FontFamily
    @Composable
    get() = ClashDisplayFontFamily

/**
 * Centralized Flixio Material 3 Typography scale.
 * Major headings and prominent titles use Clash Display; body and metadata use BodyFontFamily.
 */
val FlixioTypography: Typography
    @Composable
    get() = Typography(
        displayLarge = TextStyle(
            fontFamily = ClashDisplayFontFamily,
            fontSize = NuvioTokens.Type.pageDisplay,
            lineHeight = NuvioTokens.LineHeight.pageDisplay,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = NuvioTokens.LetterSpacing.pageDisplay,
        ),
        headlineLarge = TextStyle(
            fontFamily = ClashDisplayFontFamily,
            fontSize = NuvioTokens.Type.headline,
            lineHeight = NuvioTokens.LineHeight.headline,
            fontWeight = FontWeight.Medium,
            letterSpacing = NuvioTokens.LetterSpacing.headline,
        ),
        titleLarge = TextStyle(
            fontFamily = ClashDisplayFontFamily,
            fontSize = NuvioTokens.Type.titleSm,
            lineHeight = NuvioTokens.LineHeight.materialTitleLarge,
            fontWeight = FontWeight.Medium,
            letterSpacing = NuvioTokens.LetterSpacing.title,
        ),
        titleMedium = TextStyle(
            fontFamily = ClashDisplayFontFamily,
            fontSize = NuvioTokens.Type.bodyLg,
            lineHeight = NuvioTokens.LineHeight.bodyMd,
            fontWeight = FontWeight.Medium,
            letterSpacing = NuvioTokens.LetterSpacing.title,
        ),
        bodyLarge = TextStyle(
            fontFamily = BodyFontFamily,
            fontSize = NuvioTokens.Type.bodyApp,
            lineHeight = NuvioTokens.LineHeight.bodyApp,
            fontWeight = FontWeight.Normal,
            letterSpacing = NuvioTokens.LetterSpacing.body,
        ),
        bodyMedium = TextStyle(
            fontFamily = BodyFontFamily,
            fontSize = NuvioTokens.Type.bodyMd,
            lineHeight = NuvioTokens.LineHeight.bodyMd,
            fontWeight = FontWeight.Normal,
            letterSpacing = NuvioTokens.LetterSpacing.body,
        ),
        labelLarge = TextStyle(
            fontFamily = ClashDisplayFontFamily,
            fontSize = NuvioTokens.Type.bodyMd,
            lineHeight = NuvioTokens.LineHeight.bodySm,
            fontWeight = FontWeight.Medium,
            letterSpacing = NuvioTokens.LetterSpacing.title,
        ),
        labelMedium = TextStyle(
            fontFamily = BodyFontFamily,
            fontSize = NuvioTokens.Type.labelSm,
            lineHeight = NuvioTokens.LineHeight.labelXs,
            fontWeight = FontWeight.Medium,
            letterSpacing = NuvioTokens.LetterSpacing.label,
        ),
    )

/**
 * Backward compatibility alias for [FlixioTypography].
 */
val NuvioTypography: Typography
    @Composable
    get() = FlixioTypography

/**
 * Centralized Flixio custom type scale tokens.
 * Hero titles, displays, and prominent titles use Clash Display;
 * Body text and small metadata use BodyFontFamily for optimal readability.
 */
val FlixioTypeTokens: NuvioTypeScale
    @Composable
    get() = NuvioTypeScale(
        labelXs = TextStyle(
            fontFamily = BodyFontFamily,
            fontSize = NuvioTokens.Type.labelXs,
            lineHeight = NuvioTokens.LineHeight.labelXs,
            fontWeight = FontWeight.Medium,
            letterSpacing = NuvioTokens.LetterSpacing.body,
        ),
        labelSm = TextStyle(
            fontFamily = BodyFontFamily,
            fontSize = NuvioTokens.Type.labelSm,
            lineHeight = NuvioTokens.LineHeight.labelSm,
            fontWeight = FontWeight.Medium,
            letterSpacing = NuvioTokens.LetterSpacing.body,
        ),
        bodySm = TextStyle(
            fontFamily = BodyFontFamily,
            fontSize = NuvioTokens.Type.bodySm,
            lineHeight = NuvioTokens.LineHeight.bodySm,
            fontWeight = FontWeight.Normal,
            letterSpacing = NuvioTokens.LetterSpacing.body,
        ),
        bodyMd = TextStyle(
            fontFamily = BodyFontFamily,
            fontSize = NuvioTokens.Type.bodyMd,
            lineHeight = NuvioTokens.LineHeight.bodyMd,
            fontWeight = FontWeight.Normal,
            letterSpacing = NuvioTokens.LetterSpacing.body,
        ),
        bodyLg = TextStyle(
            fontFamily = BodyFontFamily,
            fontSize = NuvioTokens.Type.bodyLg,
            lineHeight = NuvioTokens.LineHeight.bodyLg,
            fontWeight = FontWeight.Normal,
            letterSpacing = NuvioTokens.LetterSpacing.body,
        ),
        titleSm = TextStyle(
            fontFamily = ClashDisplayFontFamily,
            fontSize = NuvioTokens.Type.titleSm,
            lineHeight = NuvioTokens.LineHeight.titleSm,
            fontWeight = FontWeight.Medium,
            letterSpacing = NuvioTokens.LetterSpacing.title,
        ),
        titleMd = TextStyle(
            fontFamily = ClashDisplayFontFamily,
            fontSize = NuvioTokens.Type.titleMd,
            lineHeight = NuvioTokens.LineHeight.titleMd,
            fontWeight = FontWeight.Medium,
            letterSpacing = NuvioTokens.LetterSpacing.title,
        ),
        titleLg = TextStyle(
            fontFamily = ClashDisplayFontFamily,
            fontSize = NuvioTokens.Type.titleLg,
            lineHeight = NuvioTokens.LineHeight.titleLg,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = NuvioTokens.LetterSpacing.title,
        ),
        displaySm = TextStyle(
            fontFamily = ClashDisplayFontFamily,
            fontSize = NuvioTokens.Type.displaySm,
            lineHeight = NuvioTokens.LineHeight.displaySm,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = NuvioTokens.LetterSpacing.headline,
        ),
        displayMd = TextStyle(
            fontFamily = ClashDisplayFontFamily,
            fontSize = NuvioTokens.Type.displayMd,
            lineHeight = NuvioTokens.LineHeight.displayMd,
            fontWeight = FontWeight.Bold,
            letterSpacing = NuvioTokens.LetterSpacing.pageDisplay,
        ),
    )

/**
 * Backward compatibility alias for [FlixioTypeTokens].
 */
val NuvioTypeTokens: NuvioTypeScale
    @Composable
    get() = FlixioTypeTokens

val MaterialTheme.flixioTypeScale: NuvioTypeScale
    @Composable
    get() = LocalNuvioTypeScale.current

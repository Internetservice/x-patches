/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.dynamiccolor

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.string

/**
 * X blue, 0xFF1D9BF0, as a Compose color long.
 */
internal const val X_BLUE_LITERAL = 4280130544L

/**
 * Any method of the design token class (X 12.10 and older).
 */
internal object StaticColorClassFingerprint : Fingerprint(
    filters = listOf(
        string("StaticColor"),
    ),
)

/**
 * Method of the design token class that loads the X blue color literal (X 12.10 and older).
 */
internal object DesignTokenFingerprint : Fingerprint(
    classFingerprint = StaticColorClassFingerprint,
    filters = listOf(
        literal(X_BLUE_LITERAL),
    ),
)

/**
 * Static initializers of the color palettes that load the X blue color literal (X 12.30 and newer).
 */
internal object PaletteInitializerFingerprint : Fingerprint(
    name = "<clinit>",
    filters = listOf(
        literal(X_BLUE_LITERAL),
    ),
)

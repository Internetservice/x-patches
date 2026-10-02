/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.dynamiccolor

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal

/**
 * X blue, 0xFF1D9BF0, as a Compose color long.
 */
internal const val X_BLUE_LITERAL = 4280130544L

/**
 * Static initializers of the color palettes that load the X blue color literal.
 */
internal object PaletteInitializerFingerprint : Fingerprint(
    name = "<clinit>",
    filters = listOf(
        literal(X_BLUE_LITERAL),
    ),
)

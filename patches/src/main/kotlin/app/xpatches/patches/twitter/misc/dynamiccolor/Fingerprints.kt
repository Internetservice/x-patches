/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.dynamiccolor

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.string

/**
 * Any method of the design token class.
 */
private object StaticColorClassFingerprint : Fingerprint(
    filters = listOf(
        string("StaticColor"),
    ),
)

/**
 * Method of the design token class that loads the X blue (0xFF1D9BF0) color literal.
 */
internal object DesignTokenFingerprint : Fingerprint(
    classFingerprint = StaticColorClassFingerprint,
    filters = listOf(
        literal(4280130544L),
    ),
)

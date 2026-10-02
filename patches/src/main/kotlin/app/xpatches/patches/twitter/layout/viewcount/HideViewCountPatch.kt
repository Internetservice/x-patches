/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.layout.viewcount

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X_ANY

@Suppress("unused")
val hideViewCountPatch = bytecodePatch(
    name = "Hide view count",
    description = "Hides the view count of posts.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_X_ANY)

    execute {
        ViewCountsEnabledFingerprint.method.returnEarly(false)
    }
}

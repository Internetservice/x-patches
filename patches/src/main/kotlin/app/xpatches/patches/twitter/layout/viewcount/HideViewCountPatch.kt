/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.layout.viewcount

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X_UNTIL_12_10

@Suppress("unused")
val hideViewCountPatch = bytecodePatch(
    name = "Hide view count",
    description = "Hides the view count of posts. The feature switch this relies on was removed in X 12.30.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_X_UNTIL_12_10)

    execute {
        ViewCountsEnabledFingerprint.method.returnEarly(false)
    }
}

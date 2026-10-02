/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.layout.viewcount

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.xpatches.patches.twitter.misc.hook.json.addJsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHookPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

private const val HOOK_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/hook/patch/viewcount/HideViewCountHook;"

@Suppress("unused")
val hideViewCountPatch = bytecodePatch(
    name = "Hide view count",
    description = "Hides the view count of posts.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(jsonHookPatch)

    execute {
        // X 12.10 and older: a feature switch controls the view counts.
        ViewCountsEnabledFingerprint.methodOrNull?.returnEarly(false)

        // X 12.30 and newer: the counts are removed from the responses.
        addJsonHook(jsonHook(HOOK_CLASS_DESCRIPTOR))
    }
}

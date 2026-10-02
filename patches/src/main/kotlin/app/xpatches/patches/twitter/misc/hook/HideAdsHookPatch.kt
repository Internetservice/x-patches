/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.hook

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.featureswitches.featureSwitchesHookPatch
import app.xpatches.patches.twitter.misc.hook.json.addJsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHookPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

private const val TOGGLE_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/toggles/HideAdsPatch;"
private const val HOOK_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/hook/patch/ads/HideAdsHook;"

@Suppress("unused")
val hideAdsHookPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides promoted posts, promoted trends, video pre-rolls and third party ads in the timelines.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(jsonHookPatch, featureSwitchesHookPatch)

    execute {
        // Promoted content in the responses.
        addJsonHook(jsonHook(HOOK_CLASS_DESCRIPTOR))

        // Third party ads served through the SSP, controlled by feature switches.
        setExtensionIsPatchIncluded(TOGGLE_CLASS_DESCRIPTOR)
    }
}

/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.premium

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.featureswitches.featureSwitchesHookPatch
import app.xpatches.patches.twitter.misc.hook.json.addJsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHookPatch
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

private const val TOGGLE_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/toggles/RemovePremiumUpsellPatch;"
private const val HOOK_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/hook/patch/premium/HidePremiumUpsellHook;"

@Suppress("unused")
val removePremiumUpsellPatch = bytecodePatch(
    name = "Remove premium upsell",
    description = "Removes the premium upsell sheets, the premium prompts in timelines and the upsell cards in the drawer and on profiles.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(featureSwitchesHookPatch, jsonHookPatch, settingsPatch)

    execute {
        // Upsell cards and prompts controlled by feature switches.
        setExtensionIsPatchIncluded(TOGGLE_CLASS_DESCRIPTOR)

        // Premium prompts injected into timelines.
        addJsonHook(jsonHook(HOOK_CLASS_DESCRIPTOR))
        setExtensionIsPatchIncluded(HOOK_CLASS_DESCRIPTOR)

        // Upsell sheets presented by the app.
        ShowPremiumUpsellFingerprint.method.returnEarly()
    }
}

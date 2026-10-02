/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.spaces

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.featureswitches.featureSwitchesHookPatch
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

private const val TOGGLE_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/toggles/HideSpacesAndLivePatch;"

@Suppress("unused")
val hideSpacesAndLivePatch = bytecodePatch(
    name = "Hide Spaces and live",
    description = "Turns off Spaces and the live stream pills and avatar rings.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(featureSwitchesHookPatch, settingsPatch)

    execute {
        setExtensionIsPatchIncluded(TOGGLE_CLASS_DESCRIPTOR)
    }
}

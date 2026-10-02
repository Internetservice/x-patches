/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.home

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.featureswitches.featureSwitchesHookPatch
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

private const val TOGGLE_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/toggles/KeepTimelinePositionPatch;"

@Suppress("unused")
val keepTimelinePositionPatch = bytecodePatch(
    name = "Keep timeline position",
    description = "Stops the app from jumping back to the top of \"For you\" and refreshing when it is reopened.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(featureSwitchesHookPatch, settingsPatch)

    execute {
        setExtensionIsPatchIncluded(TOGGLE_CLASS_DESCRIPTOR)
    }
}

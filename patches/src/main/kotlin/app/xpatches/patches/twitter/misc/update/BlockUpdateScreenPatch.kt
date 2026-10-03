/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.update

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.featureswitches.featureSwitchesHookPatch
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

@Suppress("unused")
val blockUpdateScreenPatch = bytecodePatch(
    name = "Block update screen",
    description = "Blocks the in-app update prompts and the \"Update your X app\" screen.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(featureSwitchesHookPatch, settingsPatch)

    execute {
        setExtensionIsPatchIncluded("Lapp/xpatches/extension/twitter/patches/toggles/BlockUpdateScreenPatch;")
    }
}

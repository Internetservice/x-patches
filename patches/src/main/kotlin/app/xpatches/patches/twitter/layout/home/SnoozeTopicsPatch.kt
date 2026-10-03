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

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/home/SnoozeTopicsPatch;"

/**
 * Snoozing topics in "For you" is handled by the client: the snoozed topics are kept until an
 * expiration taken from the co_timeline_reset_period_minutes feature switch, which the
 * extension overrides with the duration chosen in the settings.
 */
@Suppress("unused")
val snoozeTopicsPatch = bytecodePatch(
    name = "Snooze topics longer",
    description = "Lets you snooze topics in \"For you\" for longer than X allows, up to forever.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(featureSwitchesHookPatch, settingsPatch)

    execute {
        setExtensionIsPatchIncluded(EXTENSION_CLASS_DESCRIPTOR)
    }
}

/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.analytics

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.featureswitches.featureSwitchesHookPatch
import app.xpatches.patches.twitter.misc.hook.okhttp.customNetworkInterceptorPatch
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

private const val TOGGLE_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/toggles/DisableAnalyticsPatch;"

@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Drops the client event uploads (scribes) X sends about your activity in the app.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(customNetworkInterceptorPatch, featureSwitchesHookPatch, settingsPatch)

    execute {
        setExtensionIsPatchIncluded(TOGGLE_CLASS_DESCRIPTOR)
    }
}

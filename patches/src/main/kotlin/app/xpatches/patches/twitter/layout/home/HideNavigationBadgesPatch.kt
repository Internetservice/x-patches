/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.home

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.featureswitches.featureSwitchesHookPatch
import app.xpatches.patches.twitter.misc.hook.okhttp.customNetworkInterceptorPatch
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

/**
 * The badge counts come from a dedicated endpoint, which the network interceptor answers with zeros.
 */
@Suppress("unused")
val hideNavigationBadgesPatch = bytecodePatch(
    name = "Hide navigation bar badges",
    description = "Hides the unread counts and dots on the navigation bar icons.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(customNetworkInterceptorPatch, featureSwitchesHookPatch, settingsPatch)

    execute {
        setExtensionIsPatchIncluded("Lapp/xpatches/extension/twitter/patches/toggles/HideNavigationBadgesPatch;")
    }
}

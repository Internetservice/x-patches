/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.hook

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.featureswitches.featureSwitchesHookPatch
import app.xpatches.patches.twitter.misc.hook.json.addJsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHookPatch
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

private const val HOOK_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/hook/patch/translate/ForceTranslateHook;"

/**
 * Translations are Grok powered and enabled per account through feature switches, which are
 * forced on along with the translatable flags of the posts.
 */
@Suppress("unused")
val forceTranslatePatch = bytecodePatch(
    name = "Force enable translate",
    description = "Offers the translate action on every post, also when X has not enabled translations " +
            "for the account, and can turn on the automatic translation of posts. Off by default in the X Patches settings.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(jsonHookPatch, featureSwitchesHookPatch, settingsPatch)

    execute {
        addJsonHook(jsonHook(HOOK_CLASS_DESCRIPTOR))
        setExtensionIsPatchIncluded(HOOK_CLASS_DESCRIPTOR)
    }
}

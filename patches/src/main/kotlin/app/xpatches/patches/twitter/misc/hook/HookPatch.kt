/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.hook

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.xpatches.patches.twitter.misc.hook.json.addJsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHookPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

/**
 * A patch that registers an extension JSON hook class.
 *
 * @param name The patch name.
 * @param description The patch description.
 * @param hookClassDescriptor The extension class extending BaseJsonHook.
 * @param compatibility The app versions the patch is declared for.
 * @param default If the patch is enabled by default.
 */
internal fun hookPatch(
    name: String,
    description: String,
    hookClassDescriptor: String,
    compatibility: Compatibility = COMPATIBILITY_X,
    default: Boolean = true,
) = bytecodePatch(
    name = name,
    description = description,
    default = default,
) {
    compatibleWith(compatibility)

    dependsOn(jsonHookPatch)

    execute {
        addJsonHook(jsonHook(hookClassDescriptor))
    }
}

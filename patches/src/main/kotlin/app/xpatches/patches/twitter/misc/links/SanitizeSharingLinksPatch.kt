/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.links

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X

@Suppress("unused")
val sanitizeSharingLinksPatch = bytecodePatch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from shared links.",
) {
    compatibleWith(COMPATIBILITY_X)

    execute {
        SanitizeSharingLinksFingerprint.method.addInstructions(
            0,
            """
                # Method takes in a link (string, param 0) and then appends the tracking query params,
                # so all we need to do is return back the passed-in string
                return-object p0
            """,
        )
    }
}

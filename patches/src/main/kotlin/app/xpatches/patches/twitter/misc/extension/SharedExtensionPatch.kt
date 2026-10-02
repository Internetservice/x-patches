/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.extension

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.all.misc.extension.ExtensionHook

internal const val EXTENSION_UTILS_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/Utils;"

internal object MainActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/x/android/main/MainActivity;",
    name = "onCreate",
    returnType = "V",
)

/**
 * Merges the X extension into the app and passes the application context to it.
 */
val sharedExtensionPatch = bytecodePatch(
    default = false,
) {
    extendWith("extensions/twitter.mpe")

    execute {
        // Verify the extension was merged.
        classDefBy(EXTENSION_UTILS_CLASS_DESCRIPTOR)
    }

    finalize {
        // Hooked in finalize so the context is set before any other patched code runs.
        ExtensionHook(MainActivityOnCreateFingerprint)(EXTENSION_UTILS_CLASS_DESCRIPTOR)
    }
}

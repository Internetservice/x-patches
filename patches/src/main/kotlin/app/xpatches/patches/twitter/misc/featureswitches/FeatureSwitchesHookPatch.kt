/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.featureswitches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.xpatches.patches.twitter.misc.extension.sharedExtensionPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X_12_30

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/xpatches/extension/twitter/patches/featureswitches/FeatureSwitchesPatch;"

/**
 * Lets the extension override the boolean feature switches of the app.
 * Patches turn switches off by enabling their toggle in the extension.
 */
val featureSwitchesHookPatch = bytecodePatch(
    default = false,
) {
    compatibleWith(COMPATIBILITY_X_12_30)

    dependsOn(sharedExtensionPatch)

    execute {
        listOf(
            GetBooleanFeatureSwitchFingerprint,
            PeekBooleanFeatureSwitchFingerprint,
        ).forEach { fingerprint: Fingerprint ->
            fingerprint.methodOrNull?.apply {
                val implementation = implementation ?: throw PatchException("Feature switch method has no code")
                // p0 = this, p1 = key, p2 = default value. One local register is needed.
                if (implementation.registerCount - 3 < 1) {
                    throw PatchException("Feature switch method has no free register")
                }

                addInstructionsWithLabels(
                    0,
                    """
                        invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->getBooleanOverride(Ljava/lang/String;)Ljava/lang/Boolean;
                        move-result-object v0
                        if-eqz v0, :original
                        invoke-virtual { v0 }, Ljava/lang/Boolean;->booleanValue()Z
                        move-result v0
                        return v0
                        :original
                        nop
                    """,
                )
            } ?: if (fingerprint === GetBooleanFeatureSwitchFingerprint) {
                throw fingerprint.patchException()
            } else {
                // peekBoolean is optional.
            }
        }
    }
}

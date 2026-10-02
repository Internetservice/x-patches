/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.interaction.downloads

import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X_12
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val unlockDownloadsPatch = bytecodePatch(
    name = "Unlock downloads",
    description = "Unlocks the ability to download any video. GIFs can be downloaded via the menu on long press.",
) {
    compatibleWith(COMPATIBILITY_X_12)

    execute {
        /**
         * Allow downloads for non-premium users.
         * Return early makes the method return true for all users.
         * This method returns a boolean value that indicates whether the user can download the video of subscriptionsFeatures Interface.
         *
         * X has two identical methods, one without "legacy" and one with "legacy".
         * Don't know what legacy actually does, but it's patched anyway.
         */
        val subscriptionsFeaturesDefiningClass =
            SubscriptionsFeaturesFingerprint.originalMethod.definingClass.substringBefore("$") + ";"

        canDownloadVideoFingerprint(subscriptionsFeaturesDefiningClass, legacy = false).method.returnEarly(true)
        canDownloadVideoFingerprint(subscriptionsFeaturesDefiningClass, legacy = true).method.returnEarly(true)

        // Some media videos have different download button that directly uses subscriptionsFeatures.
        mediaGalleryDownloadFingerprint(subscriptionsFeaturesDefiningClass).let {
            it.method.apply {
                listOf(
                    0 to false, // Don't fall back to offline video.
                    2 to true, // Make user can download video.
                ).forEach { (matchIndex, boolean) ->
                    // The move-result right after the matched interface call.
                    val canUserDownloadVideoIndex = it.instructionMatches[matchIndex].index + 1
                    val canUserDownloadVideoRegister =
                        getInstruction<OneRegisterInstruction>(canUserDownloadVideoIndex).registerA
                    val bit = if (boolean) 1 else 0
                    replaceInstruction(canUserDownloadVideoIndex, "const/4 v$canUserDownloadVideoRegister, 0x$bit")
                }
            }
        }

        // Download action for long-press download button.
        postMediaActionFingerprint(subscriptionsFeaturesDefiningClass).let {
            it.method.apply {
                // Create download button for GIF.
                val isDownloadableIndex = it.instructionMatches[8].index + 1
                val isDownloadableRegister = getInstruction<OneRegisterInstruction>(isDownloadableIndex).registerA
                replaceInstruction(isDownloadableIndex, "const/4 v$isDownloadableRegister, 0x1")

                // Replace the boolean that blocks non-premium users from download.
                val canUserDownloadVideoIndex = it.instructionMatches.last().index + 1
                val canUserDownloadVideoRegister =
                    getInstruction<OneRegisterInstruction>(canUserDownloadVideoIndex).registerA
                replaceInstruction(canUserDownloadVideoIndex, "const/4 v$canUserDownloadVideoRegister, 0x1")
            }
        }
    }
}

/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.interaction.downloads

import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.morphe.util.returnEarly
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X_12
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val unlockDownloadsPatch = bytecodePatch(
    name = "Unlock downloads",
    description = "Unlocks the ability to download any video. GIFs can be downloaded via the menu on long press.",
) {
    compatibleWith(COMPATIBILITY_X_12)

    execute {
        /**
         * The media gallery download method checks the subscription features before downloading.
         * Up to X 12.10 the features are an interface nested next to the NotePostFeatures class.
         * From X 12.30 on it is a class reading the premium feature switches directly.
         * Either way it is called from the gallery download method, which narrows the candidates.
         */
        val subscriptionsFeaturesCandidates = MediaGalleryDownloadFingerprint.originalMethod.implementation!!
            .instructions
            .mapNotNull { instruction -> instruction.getReference<MethodReference>() }
            .filter { it.returnType == "Z" }
            .map { it.definingClass }
            .distinct()

        val notePostFeaturesClass = SubscriptionsFeaturesFingerprint.originalMethod.definingClass
        val legacySubscriptionsFeaturesClass = if (notePostFeaturesClass.contains('$')) {
            notePostFeaturesClass.substringBefore('$') + ";"
        } else {
            null
        }

        val subscriptionsFeaturesClass = subscriptionsFeaturesCandidates.firstOrNull { it == legacySubscriptionsFeaturesClass }
            ?: subscriptionsFeaturesCandidates.firstOrNull { classDefByOrNull(it)?.readsSubscriptionFeatures() == true }
            ?: throw PatchException("Could not find the subscription features class in $subscriptionsFeaturesCandidates")

        /**
         * Allow downloads for non-premium users.
         * Return early makes the method return true for all users.
         * This method returns a boolean value that indicates whether the user can download the video of subscriptionsFeatures Interface.
         *
         * X has two identical methods, one without "legacy" and one with "legacy".
         * Don't know what legacy actually does, but it's patched anyway.
         *
         * X 12.30 inlined these, the callers check the subscription features directly.
         */
        listOf(false, true).forEach { legacy ->
            canDownloadVideoFingerprint(subscriptionsFeaturesClass, legacy).methodOrNull?.returnEarly(true)
        }

        // Some media videos have different download button that directly uses subscriptionsFeatures.
        mediaGalleryDownloadFingerprint(subscriptionsFeaturesClass).let {
            it.method.apply {
                listOf(
                    0 to false, // Don't fall back to offline video.
                    2 to true, // Make user can download video.
                ).forEach { (matchIndex, boolean) ->
                    // The move-result right after the matched subscription feature call.
                    val canUserDownloadVideoIndex = it.instructionMatches[matchIndex].index + 1
                    val canUserDownloadVideoRegister =
                        getInstruction<OneRegisterInstruction>(canUserDownloadVideoIndex).registerA
                    val bit = if (boolean) 1 else 0
                    replaceInstruction(canUserDownloadVideoIndex, "const/4 v$canUserDownloadVideoRegister, 0x$bit")
                }
            }
        }

        // Download action for long-press download button (X 12.10 and older).
        postMediaActionFingerprint(subscriptionsFeaturesClass).let {
            it.methodOrNull?.apply {
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

/**
 * @return If any method of this class reads a premium or subscription feature switch.
 */
private fun ClassDef.readsSubscriptionFeatures() = methods.any { method ->
    method.implementation?.instructions?.any { instruction ->
        val string = (instruction as? ReferenceInstruction)?.reference as? StringReference
        string != null && (string.string.contains("premium") || string.string.contains("subscriptions_feature"))
    } == true
}

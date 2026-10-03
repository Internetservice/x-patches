/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.interaction.downloads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.findInstructionIndicesReversed
import app.morphe.util.findMutableMethodOf
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.indexOfFirstInstructionReversedOrThrow
import app.morphe.util.indexOfFirstStringInstructionOrThrow
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.extension.sharedExtensionPatch
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/downloads/UnlockDownloadsPatch;"

@Suppress("unused")
val unlockDownloadsPatch = bytecodePatch(
    name = "Unlock downloads",
    description = "Unlocks the ability to download any video, including videos whose author disallowed downloads, " +
            "and lets you pick the quality or copy the video link before downloading. GIFs can be downloaded via the menu on long press.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(sharedExtensionPatch, settingsPatch)

    execute {
        setExtensionIsPatchIncluded(EXTENSION_CLASS_DESCRIPTOR)

        // The download actions are only offered when the media model says it is downloadable,
        // which the author of the post controls. Force the flag when the models are built.
        forceDownloadable(MediaContentVideoToStringFingerprint)
        forceDownloadable(MediaContentGifToStringFingerprint)

        /**
         * The media gallery download method checks the subscription features before downloading.
         * The features class reads the premium feature switches directly and is called from
         * the gallery download method, which narrows the candidates.
         */
        val subscriptionsFeaturesCandidates = MediaGalleryDownloadFingerprint.originalMethod.implementation!!
            .instructions
            .mapNotNull { instruction -> instruction.getReference<MethodReference>() }
            .filter { it.returnType == "Z" }
            .map { it.definingClass }
            .distinct()

        val subscriptionsFeaturesClass = subscriptionsFeaturesCandidates
            .firstOrNull { classDefByOrNull(it)?.readsSubscriptionFeatures() == true }
            ?: throw PatchException("Could not find the subscription features class in $subscriptionsFeaturesCandidates")

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

        // The premium checks are inlined into the download actions,
        // which raise the video download upsell when they fail.
        unlockVideoDownloadUpsells(subscriptionsFeaturesClass)

        // Offer the quality picker before the download starts. Inserted last, as it shifts
        // the instruction indices of the matches above.
        MediaGalleryDownloadFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->showQualityPicker(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :download
                return-void
                :download
                nop
            """,
        )
    }
}

/**
 * Routes every write of the "isDownloadable" field of the media model through the extension,
 * which forces it on when the setting is on.
 *
 * @param toStringFingerprint The toString of the media model, which reveals the field.
 */
private fun BytecodePatchContext.forceDownloadable(toStringFingerprint: Fingerprint) {
    val field = toStringFingerprint.originalMethod.let { method ->
        val labelIndex = method.indexOfFirstStringInstructionOrThrow(IS_DOWNLOADABLE_STRING)
        val fieldIndex = method.indexOfFirstInstructionOrThrow(labelIndex, Opcode.IGET_BOOLEAN)
        method.getInstruction(fieldIndex).getReference<FieldReference>()
            ?: throw PatchException("Could not find the isDownloadable field")
    }

    val constructors = toStringFingerprint.classDef.methods.filter { it.name == "<init>" }
    var writes = 0

    constructors.forEach { constructor ->
        constructor.findInstructionIndicesReversed {
            opcode == Opcode.IPUT_BOOLEAN && getReference<FieldReference>().let {
                it?.definingClass == field.definingClass && it.name == field.name
            }
        }.forEach { writeIndex ->
            val register = constructor.getInstruction<TwoRegisterInstruction>(writeIndex).registerA
            constructor.addInstructions(
                writeIndex,
                """
                    invoke-static/range { v$register .. v$register }, $EXTENSION_CLASS_DESCRIPTOR->isDownloadable(Z)Z
                    move-result v$register
                """,
            )
            writes++
        }
    }

    if (writes == 0) throw PatchException("Could not find any write of the isDownloadable field")
}

/**
 * Forces the premium checks to pass in every method that raises the video download upsell,
 * directly or through a lambda class it instantiates.
 */
private fun BytecodePatchContext.unlockVideoDownloadUpsells(subscriptionsFeaturesClass: String) {
    val upsellType = UpsellFeatureKeyFingerprint.originalMethod.let { method ->
        val keyIndex = method.indexOfFirstStringInstructionOrThrow(VIDEO_DOWNLOAD_UPSELL_KEY)
        val instanceOfIndex = method.indexOfFirstInstructionReversedOrThrow(keyIndex, Opcode.INSTANCE_OF)
        method.getInstruction(instanceOfIndex).getReference<TypeReference>()?.type
            ?: throw PatchException("Could not find the video download upsell type")
    }

    // The premium status checks of the subscription features class, as opposed to
    // feature switches such as offline videos.
    val premiumChecks = classDefBy(subscriptionsFeaturesClass).methods
        .filter { method -> method.returnType == "Z" && method.referencesString { it.contains("premium") } }
        .map { it.signature() }
        .toSet()

    if (premiumChecks.isEmpty()) throw PatchException("Could not find the premium checks")

    fun Instruction.isPremiumCheck(): Boolean {
        val reference = getReference<MethodReference>() ?: return false
        return reference.definingClass == subscriptionsFeaturesClass && reference.signature() in premiumChecks
    }

    // Classes whose methods reference the upsell instance, such as the post options menu
    // and the small lambdas the video tab uses to pick an upsell.
    val raisingClasses = mutableSetOf<String>()
    classDefForEach { classDef ->
        if (classDef.type == upsellType) return@classDefForEach
        val referencesUpsell = classDef.methods.any { method ->
            method.implementation?.instructions?.any { instruction ->
                instruction.opcode == Opcode.SGET_OBJECT &&
                        instruction.getReference<FieldReference>()?.definingClass == upsellType
            } == true
        }
        if (referencesUpsell) raisingClasses += classDef.type
    }

    // Methods of those classes, plus the methods instantiating those classes.
    val targets = mutableListOf<Pair<ClassDef, Method>>()
    classDefForEach { classDef ->
        classDef.methods.forEach { method ->
            val instructions = method.implementation?.instructions ?: return@forEach
            val raisesUpsell = classDef.type in raisingClasses || instructions.any { instruction ->
                instruction.opcode == Opcode.NEW_INSTANCE &&
                        instruction.getReference<TypeReference>()?.type in raisingClasses
            }
            if (raisesUpsell && instructions.any { it.isPremiumCheck() }) {
                targets += classDef to method
            }
        }
    }

    if (targets.isEmpty()) throw PatchException("Could not find any method raising the video download upsell")

    targets.forEach { (classDef, method) ->
        mutableClassDefBy(classDef).findMutableMethodOf(method).apply {
            findInstructionIndicesReversed { isPremiumCheck() }.forEach { checkIndex ->
                val resultIndex = checkIndex + 1
                val resultInstruction = getInstruction<OneRegisterInstruction>(resultIndex)
                if (resultInstruction.opcode == Opcode.MOVE_RESULT) {
                    replaceInstruction(resultIndex, "const/4 v${resultInstruction.registerA}, 0x1")
                }
            }
        }
    }
}

private fun MethodReference.signature() = "$name(${parameterTypes.joinToString("")})$returnType"

/**
 * @return If any const-string of this method satisfies [predicate].
 */
private fun Method.referencesString(predicate: (String) -> Boolean) = implementation?.instructions?.any { instruction ->
    val string = (instruction as? ReferenceInstruction)?.reference as? StringReference
    string != null && predicate(string.string)
} == true

/**
 * @return If any method of this class reads a premium or subscription feature switch.
 */
private fun ClassDef.readsSubscriptionFeatures() = methods.any { method ->
    method.referencesString { it.contains("premium") || it.contains("subscriptions_feature") }
}

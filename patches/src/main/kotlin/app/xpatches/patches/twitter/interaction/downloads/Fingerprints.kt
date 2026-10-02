/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.interaction.downloads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import app.xpatches.patches.twitter.shared.fieldAccessFilter
import app.xpatches.patches.twitter.shared.hasAccessFlags
import app.xpatches.patches.twitter.shared.methodCallFilter
import app.xpatches.patches.twitter.shared.typeReferenceFilter
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * toString() of a features data class nested inside the subscriptions features interface.
 */
internal object SubscriptionsFeaturesFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(
        string("NotePostFeatures(maxWeightedCharacterLength="),
        string(", isRichCompositionEnabled="),
        string(", isPostStormEnabled="),
        string(")"),
    ),
    custom = { method, _ -> method.hasAccessFlags(AccessFlags.PUBLIC) },
)

/**
 * Method returning if the user can download a video.
 *
 * X has two identical methods, one without "legacy" and one with "legacy".
 */
internal fun canDownloadVideoFingerprint(subscriptionsFeaturesDefiningClass: String, legacy: Boolean) = Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        fieldAccessFilter(Opcode.IGET_OBJECT) {
            it.type == subscriptionsFeaturesDefiningClass && it.definingClass.contains("legacy") == legacy
        },
        methodCall(
            definingClass = subscriptionsFeaturesDefiningClass,
            opcode = Opcode.INVOKE_INTERFACE,
            location = MatchAfterImmediately(),
        ),
        opcode(Opcode.MOVE_RESULT, MatchAfterImmediately()),
        opcode(Opcode.RETURN, MatchAfterImmediately()),
    ),
    custom = { method, _ -> method.hasAccessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL) },
)

/**
 * Media gallery download button that directly uses the subscriptions features.
 */
internal fun mediaGalleryDownloadFingerprint(subscriptionsFeaturesDefiningClass: String) = Fingerprint(
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = subscriptionsFeaturesDefiningClass,
            returnType = "Z",
            opcode = Opcode.INVOKE_INTERFACE,
        ),
        string("offline_videos_download_fallback"),
        methodCall(
            definingClass = subscriptionsFeaturesDefiningClass,
            returnType = "Z",
            opcode = Opcode.INVOKE_INTERFACE,
        ),
        string("offline_videos_download_fallback"),
        string("video_download"),
    ),
    custom = { method, _ -> method.hasAccessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL) },
)

/**
 * Download action for the long-press media menu.
 */
internal fun postMediaActionFingerprint(subscriptionsFeaturesDefiningClass: String): Fingerprint {
    var mediaConfigGifDefiningClass = ""

    return Fingerprint(
        name = "invoke",
        filters = listOf(
            string("postEvent"),
            string("click"),
            string("video"),
            string("gif"),
            string("photo"),
            opcode(Opcode.INSTANCE_OF), // MediaContentImage
            opcode(Opcode.INSTANCE_OF), // MediaContentVideo
            // MediaContentGif
            typeReferenceFilter(Opcode.INSTANCE_OF) { type ->
                mediaConfigGifDefiningClass = type
                true
            },
            // Boolean for creating download button for GIF.
            methodCallFilter(Opcode.INVOKE_VIRTUAL) {
                it.definingClass == mediaConfigGifDefiningClass
            },
            string("save"),
            // Boolean for checking if user can download video
            methodCall(
                definingClass = subscriptionsFeaturesDefiningClass,
                returnType = "Z",
                opcode = Opcode.INVOKE_INTERFACE_RANGE,
            ),
        ),
        custom = { method, _ -> method.hasAccessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL) },
    )
}

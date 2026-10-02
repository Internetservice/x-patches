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

internal const val OFFLINE_VIDEO_FALLBACK_STRING = "offline_videos_download_fallback"
internal const val VIDEO_DOWNLOAD_STRING = "video_download"

/**
 * toString() of the NotePostFeatures data class. Up to X 12.10 it is nested inside the
 * subscription features interface, which is how that interface is found.
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
 * Media gallery download action. Checks the subscription features before downloading,
 * otherwise falls back to the offline video upsell or the premium upsell.
 *
 * The subscription features class is resolved from this method, see [UnlockDownloadsPatch].
 */
internal object MediaGalleryDownloadFingerprint : Fingerprint(
    returnType = "V",
    strings = listOf(
        OFFLINE_VIDEO_FALLBACK_STRING,
        VIDEO_DOWNLOAD_STRING,
    ),
    custom = { method, _ -> method.hasAccessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL) },
)

/**
 * Same method as [MediaGalleryDownloadFingerprint], matched with the subscription feature calls.
 */
internal fun mediaGalleryDownloadFingerprint(subscriptionsFeaturesClass: String) = Fingerprint(
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = subscriptionsFeaturesClass,
            returnType = "Z",
            opcodes = listOf(Opcode.INVOKE_INTERFACE, Opcode.INVOKE_VIRTUAL),
        ),
        string(OFFLINE_VIDEO_FALLBACK_STRING),
        methodCall(
            definingClass = subscriptionsFeaturesClass,
            returnType = "Z",
            opcodes = listOf(Opcode.INVOKE_INTERFACE, Opcode.INVOKE_VIRTUAL),
        ),
        string(OFFLINE_VIDEO_FALLBACK_STRING),
        string(VIDEO_DOWNLOAD_STRING),
    ),
    custom = { method, _ -> method.hasAccessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL) },
)

// region X 12.10 and older

/**
 * Method returning if the user can download a video (X 12.10 and older).
 *
 * X has two identical methods, one without "legacy" and one with "legacy".
 */
internal fun canDownloadVideoFingerprint(subscriptionsFeaturesClass: String, legacy: Boolean) = Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        fieldAccessFilter(Opcode.IGET_OBJECT) {
            it.type == subscriptionsFeaturesClass && it.definingClass.contains("legacy") == legacy
        },
        methodCall(
            definingClass = subscriptionsFeaturesClass,
            opcode = Opcode.INVOKE_INTERFACE,
            location = MatchAfterImmediately(),
        ),
        opcode(Opcode.MOVE_RESULT, MatchAfterImmediately()),
        opcode(Opcode.RETURN, MatchAfterImmediately()),
    ),
    custom = { method, _ -> method.hasAccessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL) },
)

/**
 * Download action for the long-press media menu (X 12.10 and older).
 */
internal fun postMediaActionFingerprint(subscriptionsFeaturesClass: String): Fingerprint {
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
                definingClass = subscriptionsFeaturesClass,
                returnType = "Z",
                opcode = Opcode.INVOKE_INTERFACE_RANGE,
            ),
        ),
        custom = { method, _ -> method.hasAccessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL) },
    )
}

// endregion

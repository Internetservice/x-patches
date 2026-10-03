/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.interaction.downloads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import app.xpatches.patches.twitter.shared.hasAccessFlags
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val OFFLINE_VIDEO_FALLBACK_STRING = "offline_videos_download_fallback"
internal const val VIDEO_DOWNLOAD_STRING = "video_download"

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

internal const val VIDEO_DOWNLOAD_UPSELL_KEY = "video_download"

/**
 * Maps the premium upsell types to their feature keys. The `instance-of` check right before the
 * "video_download" key reveals the upsell type raised when a non premium user downloads a video.
 */
internal object UpsellFeatureKeyFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    filters = listOf(
        string(VIDEO_DOWNLOAD_UPSELL_KEY),
        string("offline_videos"),
    ),
)

// endregion

internal const val IS_DOWNLOADABLE_STRING = ", isDownloadable="

/**
 * toString of the video media model. The `iget-boolean` after the "isDownloadable" label
 * reveals the field the download actions check.
 */
internal object MediaContentVideoToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    strings = listOf(
        "MediaContentVideo(mediaId=",
        IS_DOWNLOADABLE_STRING,
    ),
)

/**
 * toString of the GIF media model, see [MediaContentVideoToStringFingerprint].
 */
internal object MediaContentGifToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    strings = listOf(
        "MediaContentGif(mediaId=",
        IS_DOWNLOADABLE_STRING,
    ),
)

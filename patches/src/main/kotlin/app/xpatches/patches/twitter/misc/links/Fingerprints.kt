/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.links

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/xpatches/extension/twitter/patches/links/CustomizeSharingLinkPatch;"

internal const val STATUS_LINK_PREFIX = "https://x.com/i/status/"
internal const val LISTS_LINK_PREFIX = "https://x.com/i/lists/"
internal const val TRENDING_LINK_PREFIX = "https://x.com/i/trending/"

/**
 * Method that appends the tracking query parameters to a shared link (up to X 12.10).
 */
internal object SanitizeSharingLinksFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    strings = listOf(
        "<this>",
        "shareParam",
        "sessionToken",
    ),
)

internal object LinkSharingDomainHelperFingerprint : Fingerprint(
    definingClass = EXTENSION_CLASS_DESCRIPTOR,
    name = "getShareDomain",
)

internal object ReturnUsernameHelperFingerprint : Fingerprint(
    definingClass = EXTENSION_CLASS_DESCRIPTOR,
    name = "isReturnUsernameEnabled",
)

// region X 12.10 and older

/**
 * Formats the share link for internal sharing, such as sharing through XChat.
 */
internal object LinkInternalShareSheetFingerprint : Fingerprint(
    filters = listOf(
        string("tweet-"),
        string(STATUS_LINK_PREFIX),
        fieldAccess(type = "Lcom/x/models/ContextualPost;"),
    ),
)

/**
 * Formats the share link for external sharing, such as "Copy link" or "Share via...".
 */
internal object LinkExternalShareSheetFingerprint : Fingerprint(
    filters = listOf(
        opcode(Opcode.IF_EQZ),
        string(LISTS_LINK_PREFIX),
        string(TRENDING_LINK_PREFIX),
        string(STATUS_LINK_PREFIX),
    ),
)

// endregion

// region X 12.30 and newer

/**
 * Builds the share intent for "Copy link" / "Share via...".
 * The post link is built as `"https://x.com/i/status/" + id` right before the intent is created.
 */
internal object ShareIntentFingerprint : Fingerprint(
    returnType = "Landroid/content/Intent;",
    strings = listOf(
        LISTS_LINK_PREFIX,
        TRENDING_LINK_PREFIX,
        STATUS_LINK_PREFIX,
    ),
)

/**
 * Constructor of the XChat share sheet component. The link that is shared is stored in a String field
 * right after it is resolved from the post URL or built from the post id.
 */
internal object ChatShareSheetConstructorFingerprint : Fingerprint(
    name = "<init>",
    returnType = "V",
    strings = listOf(
        LISTS_LINK_PREFIX,
        TRENDING_LINK_PREFIX,
        STATUS_LINK_PREFIX,
    ),
)

// endregion

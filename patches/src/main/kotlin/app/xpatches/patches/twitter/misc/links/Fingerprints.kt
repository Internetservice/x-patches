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

/**
 * Method that appends the tracking query parameters to a shared link.
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

/**
 * Formats the share link for internal sharing, such as sharing through XChat.
 */
internal object LinkInternalShareSheetFingerprint : Fingerprint(
    filters = listOf(
        string("tweet-"),
        string("https://x.com/i/status/"),
        fieldAccess(type = "Lcom/x/models/ContextualPost;"),
    ),
)

/**
 * Formats the share link for external sharing, such as "Copy link" or "Share via...".
 */
internal object LinkExternalShareSheetFingerprint : Fingerprint(
    filters = listOf(
        opcode(Opcode.IF_EQZ),
        string("https://x.com/i/lists/"),
        string("https://x.com/i/trending/"),
        string("https://x.com/i/status/"),
    ),
)

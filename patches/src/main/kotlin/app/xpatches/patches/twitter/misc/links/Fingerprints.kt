/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.links

import app.morphe.patcher.Fingerprint

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/xpatches/extension/twitter/patches/links/CustomizeSharingLinkPatch;"

internal const val STATUS_LINK_PREFIX = "https://x.com/i/status/"
internal const val LISTS_LINK_PREFIX = "https://x.com/i/lists/"
internal const val TRENDING_LINK_PREFIX = "https://x.com/i/trending/"

internal object LinkSharingDomainHelperFingerprint : Fingerprint(
    definingClass = EXTENSION_CLASS_DESCRIPTOR,
    name = "defaultShareDomain",
)

internal object ReturnUsernameHelperFingerprint : Fingerprint(
    definingClass = EXTENSION_CLASS_DESCRIPTOR,
    name = "defaultReturnUsername",
)

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


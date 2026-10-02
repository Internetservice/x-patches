/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.layout.viewcount

import app.morphe.patcher.Fingerprint

internal object ViewCountsEnabledFingerprint : Fingerprint(
    returnType = "Z",
    strings = listOf("view_counts_public_visibility_enabled"),
)

/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string

/**
 * Presents a premium upsell sheet and logs the impression.
 */
internal object ShowPremiumUpsellFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("subscriptions:marketing:unified-upsell::"),
    ),
)

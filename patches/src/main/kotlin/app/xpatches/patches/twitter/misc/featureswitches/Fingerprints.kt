/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.featureswitches

import app.morphe.patcher.Fingerprint

private const val FEATURE_SWITCHES_REPOSITORY_CLASS = "Lcom/x/featureswitches/FeatureSwitchesRepositoryImpl;"

/**
 * Resolves a boolean feature switch, logging the impression.
 */
internal object GetBooleanFeatureSwitchFingerprint : Fingerprint(
    definingClass = FEATURE_SWITCHES_REPOSITORY_CLASS,
    name = "getBoolean",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;", "Z"),
)

/**
 * Resolves a boolean feature switch without logging the impression.
 */
internal object PeekBooleanFeatureSwitchFingerprint : Fingerprint(
    definingClass = FEATURE_SWITCHES_REPOSITORY_CLASS,
    name = "peekBoolean",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;", "Z"),
)

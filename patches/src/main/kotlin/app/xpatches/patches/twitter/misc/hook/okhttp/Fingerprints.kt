/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.hook.okhttp

import app.morphe.patcher.Fingerprint

internal object OkHttpClientBuilderBuildFingerprint : Fingerprint(
    definingClass = "Lokhttp3/OkHttpClient\$Builder;",
    name = "build",
)

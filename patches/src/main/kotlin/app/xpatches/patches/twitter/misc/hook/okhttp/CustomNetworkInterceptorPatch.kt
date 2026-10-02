/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.hook.okhttp

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.xpatches.patches.twitter.misc.extension.sharedExtensionPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X_12

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/xpatches/extension/twitter/patches/hook/okhttp/CustomNetworkInterceptorPatch;"

/**
 * Registers the extension network interceptor on every OkHttp client the app builds.
 */
val customNetworkInterceptorPatch = bytecodePatch(
    default = false,
) {
    compatibleWith(COMPATIBILITY_X_12)

    dependsOn(sharedExtensionPatch)

    execute {
        OkHttpClientBuilderBuildFingerprint.method.addInstructions(
            0,
            $$"""
                new-instance v0, $$EXTENSION_CLASS_DESCRIPTOR
                invoke-direct { v0 }, $$EXTENSION_CLASS_DESCRIPTOR-><init>()V
                invoke-virtual { p0, v0 }, Lokhttp3/OkHttpClient$Builder;->addNetworkInterceptor(Lokhttp3/Interceptor;)Lokhttp3/OkHttpClient$Builder;
            """,
        )
    }
}

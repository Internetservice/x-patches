/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.links

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import org.w3c.dom.Element

private const val CUSTOM_LINKS_EXTENSION_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/links/HandleCustomLinksPatch;"

/**
 * The hosts of the link fixing services, which X then handles as x.com links.
 */
internal val CUSTOM_LINK_HOSTS = listOf(
    "fxtwitter.com",
    "vxtwitter.com",
    "fixupx.com",
    "fixvx.com",
    "twittpr.com",
)

internal object MainActivityOnNewIntentFingerprint : Fingerprint(
    definingClass = "Lcom/x/android/main/MainActivity;",
    name = "onNewIntent",
    returnType = "V",
    parameters = listOf("Landroid/content/Intent;"),
)

private val handleCustomLinksResourcePatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val activities = document.getElementsByTagName("activity")
            val mainActivity = (0 until activities.length)
                .map { activities.item(it) as Element }
                .firstOrNull { it.getAttribute("android:name") == "com.x.android.main.MainActivity" }
                ?: throw PatchException("Could not find the main activity in the manifest")

            val filter = document.createElement("intent-filter")
            filter.appendChild(document.createElement("action").apply {
                setAttribute("android:name", "android.intent.action.VIEW")
            })
            listOf("android.intent.category.DEFAULT", "android.intent.category.BROWSABLE").forEach { category ->
                filter.appendChild(document.createElement("category").apply { setAttribute("android:name", category) })
            }
            listOf("http", "https").forEach { scheme ->
                filter.appendChild(document.createElement("data").apply { setAttribute("android:scheme", scheme) })
            }
            CUSTOM_LINK_HOSTS.forEach { host ->
                filter.appendChild(document.createElement("data").apply { setAttribute("android:host", host) })
            }
            mainActivity.appendChild(filter)
        }
    }
}

@Suppress("unused")
val handleCustomLinksPatch = bytecodePatch(
    name = "Handle custom twitter links",
    description = "Opens fxtwitter, vxtwitter, fixupx, fixvx and twittpr links in X. " +
            "On Android 12 and newer the links have to be enabled under \"Open by default\" in the app info.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(settingsPatch, handleCustomLinksResourcePatch)

    execute {
        setExtensionIsPatchIncluded(CUSTOM_LINKS_EXTENSION_CLASS_DESCRIPTOR)

        // Links arriving while X runs. Links starting X are rewritten from the context hook.
        MainActivityOnNewIntentFingerprint.method.addInstructions(
            0,
            "invoke-static/range { p1 .. p1 }, $CUSTOM_LINKS_EXTENSION_CLASS_DESCRIPTOR->rewriteIntent(Landroid/content/Intent;)V",
        )
    }
}

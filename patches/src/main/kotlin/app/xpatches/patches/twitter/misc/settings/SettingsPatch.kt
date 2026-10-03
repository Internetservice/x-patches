/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.settings

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.util.returnEarly
import app.xpatches.patches.twitter.misc.extension.EXTENSION_UTILS_CLASS_DESCRIPTOR
import app.xpatches.patches.twitter.misc.extension.sharedExtensionPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import org.w3c.dom.Element
import java.net.URLDecoder
import java.util.jar.JarFile

private const val SETTINGS_ACTIVITY_CLASS = "app.xpatches.extension.twitter.settings.SettingsActivity"
private const val SHORTCUT_LABEL_RESOURCE = "xpatches_settings_shortcut"

private object PatchesVersionFingerprint : Fingerprint(
    definingClass = EXTENSION_UTILS_CLASS_DESCRIPTOR,
    name = "getPatchesVersion",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
)

/**
 * Tells the extension which version of the patches it runs.
 */
private val settingsBytecodePatch = bytecodePatch(
    default = false,
) {
    dependsOn(sharedExtensionPatch)

    execute {
        PatchesVersionFingerprint.method.returnEarly(patchesVersion())
    }
}

/**
 * @return The version of this patches file, from its manifest.
 */
private fun patchesVersion(): String = runCatching {
    val className = object {}::class.java.enclosingClass.name.replace('.', '/') + ".class"
    val url = object {}::class.java.classLoader?.getResource(className)?.toString()
        ?: return@runCatching null
    if (!url.startsWith("jar:file:")) return@runCatching null
    val path = URLDecoder.decode(url.substring("jar:file:".length, url.lastIndexOf('!')), "UTF-8")
    JarFile(path).use { it.manifest.mainAttributes.getValue("Version") }
}.getOrNull() ?: "unknown"

@Suppress("unused")
val settingsPatch = resourcePatch(
    name = "Settings",
    description = "Adds the X Patches settings screen, reachable from the navigation drawer, by long pressing the app icon or by opening xpatches://settings. Every other patch can be switched off there without patching again.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(settingsBytecodePatch, drawerEntryPatch)

    execute {
        // region Settings activity

        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as Element

            val activity = document.createElement("activity").apply {
                setAttribute("android:name", SETTINGS_ACTIVITY_CLASS)
                setAttribute("android:exported", "true")
                setAttribute("android:label", "X Patches")
                setAttribute("android:theme", "@android:style/Theme.DeviceDefault.DayNight")
            }

            activity.appendChild(
                document.createElement("intent-filter").apply {
                    appendChild(document.createElement("action").apply {
                        setAttribute("android:name", "android.intent.action.VIEW")
                    })
                    appendChild(document.createElement("category").apply {
                        setAttribute("android:name", "android.intent.category.DEFAULT")
                    })
                    appendChild(document.createElement("category").apply {
                        setAttribute("android:name", "android.intent.category.BROWSABLE")
                    })
                    appendChild(document.createElement("data").apply {
                        setAttribute("android:scheme", "xpatches")
                        setAttribute("android:host", "settings")
                    })
                },
            )

            application.appendChild(activity)
        }

        // endregion

        // region Launcher shortcut

        document("res/values/strings.xml").use { document ->
            val string = document.createElement("string")
            string.setAttribute("name", SHORTCUT_LABEL_RESOURCE)
            string.textContent = "X Patches settings"
            document.getElementsByTagName("resources").item(0).appendChild(string)
        }

        val shortcutsPath = "res/xml/shortcuts.xml"
        if (!get(shortcutsPath).exists()) throw PatchException("$shortcutsPath not found")

        document(shortcutsPath).use { document ->
            val shortcuts = document.documentElement
            val packageName = "com.twitter.android"

            val shortcut = document.createElement("shortcut").apply {
                setAttribute("android:shortcutId", "xpatches_settings")
                setAttribute("android:enabled", "true")
                setAttribute("android:icon", "@mipmap/ic_launcher")
                setAttribute("android:shortcutShortLabel", "@string/$SHORTCUT_LABEL_RESOURCE")
                setAttribute("android:shortcutLongLabel", "@string/$SHORTCUT_LABEL_RESOURCE")
                appendChild(document.createElement("intent").apply {
                    setAttribute("android:action", "android.intent.action.VIEW")
                    setAttribute("android:targetPackage", packageName)
                    setAttribute("android:targetClass", SETTINGS_ACTIVITY_CLASS)
                })
            }

            // Launchers show the first few shortcuts, put ours in front.
            shortcuts.insertBefore(shortcut, shortcuts.firstChild)
        }

        // endregion
    }
}

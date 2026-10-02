/*
 * Icon assets from piko (GPLv3) - https://github.com/crimera/piko
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.layout.branding

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.util.ResourceGroup
import app.morphe.util.copyResources
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import org.w3c.dom.Element

private const val ICON_FOREGROUND = "ic_launcher_twitter_foreground"
private const val ICON_BACKGROUND_COLOR = "xpatches_twitter_launcher_background"
private const val TWITTER_BLUE = "#FF1DA1F2"

@Suppress("unused")
val bringBackTwitterPatch = resourcePatch(
    name = "Bring back Twitter",
    description = "Brings back the Twitter bird launcher icon and the Twitter app name.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_X)

    execute {
        // region Launcher icon

        copyResources(
            "twitter/bringbacktwitter",
            *listOf("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")
                .map { ResourceGroup("mipmap-$it", "$ICON_FOREGROUND.webp") }
                .toTypedArray(),
        )

        document("res/values/colors.xml").use { document ->
            val color = document.createElement("color")
            color.setAttribute("name", ICON_BACKGROUND_COLOR)
            color.textContent = TWITTER_BLUE
            document.getElementsByTagName("resources").item(0).appendChild(color)
        }

        listOf("ic_launcher", "ic_launcher_round").forEach { icon ->
            // The decoder drops the -v21 qualifier of the folder.
            val path = listOf("res/mipmap-anydpi/$icon.xml", "res/mipmap-anydpi-v21/$icon.xml")
                .firstOrNull { get(it).exists() }
                ?: throw PatchException("Launcher icon $icon.xml not found")

            document(path).use { document ->
                val adaptiveIcon = document.getElementsByTagName("adaptive-icon").item(0) as Element

                // Themed icons would still show the X, drop the monochrome layer.
                adaptiveIcon.getElementsByTagName("monochrome").item(0)?.let { adaptiveIcon.removeChild(it) }

                (adaptiveIcon.getElementsByTagName("foreground").item(0) as Element)
                    .setAttribute("android:drawable", "@mipmap/$ICON_FOREGROUND")
                (adaptiveIcon.getElementsByTagName("background").item(0) as Element)
                    .setAttribute("android:drawable", "@color/$ICON_BACKGROUND_COLOR")
            }
        }

        // endregion

        // region App name

        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as Element
            application.setAttribute("android:label", "Twitter")
        }

        // The launcher activity may carry its own label.
        document("AndroidManifest.xml").use { document ->
            val activities = document.getElementsByTagName("activity")
            for (i in 0 until activities.length) {
                val activity = activities.item(i) as Element
                if (activity.getAttribute("android:label") == "@string/APP_NAME") {
                    activity.setAttribute("android:label", "Twitter")
                }
            }
        }

        // endregion
    }
}

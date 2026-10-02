/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.dynamiccolor

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.xpatches.patches.twitter.misc.extension.sharedExtensionPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X_12
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.util.findInstructionIndicesReversed
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction
import java.io.FileWriter
import java.nio.file.Files

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/xpatches/extension/twitter/patches/misc/DynamicColorPatch;"

private val dynamicColorBytecodePatch = bytecodePatch(
    default = false,
) {
    compatibleWith(COMPATIBILITY_X_12)

    dependsOn(sharedExtensionPatch)

    execute {
        // Replace the default X (Formerly Twitter) Blue with the user's Material You palette.
        fun MutableMethod.hookLiteral(literalIndex: Int) {
            // const-wide, the literal occupies this register and the next.
            val literalRegister = getInstruction<OneRegisterInstruction>(literalIndex).registerA

            addInstructions(
                literalIndex + 1,
                """
                    invoke-static/range { v$literalRegister .. v${literalRegister + 1} }, $EXTENSION_CLASS_DESCRIPTOR->getDynamicColor(J)J
                    move-result-wide v$literalRegister
                """,
            )
        }

        // A fingerprint with an unresolvable class fingerprint throws instead of returning null,
        // so the design token class is probed first.
        if (StaticColorClassFingerprint.matchOrNull() != null) {
            // X 12.10 and older: a single design token method.
            DesignTokenFingerprint.let {
                it.method.hookLiteral(it.instructionMatches.first().index)
            }
        } else {
            // X 12.30 and newer: the blue is loaded in the static initializers of the Compose palettes.
            val initializers = PaletteInitializerFingerprint.matchAllOrNull()
                ?: throw PatchException("Could not find any color palette using X blue")

            initializers.forEach { match ->
                match.method.apply {
                    findInstructionIndicesReversed {
                        this is WideLiteralInstruction && wideLiteral == X_BLUE_LITERAL
                    }.forEach { literalIndex -> hookLiteral(literalIndex) }
                }
            }
        }
    }
}

@Suppress("unused")
val dynamicColorPatch = resourcePatch(
    name = "Dynamic color",
    description = "Replaces the default X (Formerly Twitter) Blue with the user's Material You palette.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(dynamicColorBytecodePatch)

    execute {
        val resDirectory = get("res")
        if (!resDirectory.isDirectory) throw PatchException("The res folder can not be found.")

        val valuesV31Directory = resDirectory.resolve("values-v31")
        if (!valuesV31Directory.isDirectory) Files.createDirectories(valuesV31Directory.toPath())

        val valuesNightV31Directory = resDirectory.resolve("values-night-v31")
        if (!valuesNightV31Directory.isDirectory) Files.createDirectories(valuesNightV31Directory.toPath())

        listOf(valuesV31Directory, valuesNightV31Directory).forEach { directory ->
            val colorsXml = directory.resolve("colors.xml")

            if (!colorsXml.exists()) {
                FileWriter(colorsXml).use {
                    it.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><resources></resources>")
                }
            }
        }

        document("res/values-v31/colors.xml").use { document ->
            arrayOf(
                "ps__twitter_blue" to "@color/twitter_blue",
                "ps__twitter_blue_pressed" to "@color/twitter_blue_fill_pressed",
                "twitter_blue" to "@android:color/system_accent1_400",
                "twitter_blue_fill_pressed" to "@android:color/system_accent1_300",
                "twitter_blue_opacity_30" to "@android:color/system_accent1_100",
                "twitter_blue_opacity_50" to "@android:color/system_accent1_200",
                "twitter_blue_opacity_58" to "@android:color/system_accent1_300",
                "deep_transparent_twitter_blue" to "@android:color/system_accent1_200",
            ).forEach { (k, v) ->
                val colorElement = document.createElement("color")

                colorElement.setAttribute("name", k)
                colorElement.textContent = v

                document.getElementsByTagName("resources").item(0).appendChild(colorElement)
            }
        }

        document("res/values-night-v31/colors.xml").use { document ->
            arrayOf(
                "twitter_blue" to "@android:color/system_accent1_200",
                "twitter_blue_fill_pressed" to "@android:color/system_accent1_300",
                "twitter_blue_opacity_30" to "@android:color/system_accent1_50",
                "twitter_blue_opacity_50" to "@android:color/system_accent1_100",
                "twitter_blue_opacity_58" to "@android:color/system_accent1_200",
                "deep_transparent_twitter_blue" to "@android:color/system_accent1_200",
            ).forEach { (k, v) ->
                val colorElement = document.createElement("color")

                colorElement.setAttribute("name", k)
                colorElement.textContent = v

                document.getElementsByTagName("resources").item(0).appendChild(colorElement)
            }
        }
    }
}

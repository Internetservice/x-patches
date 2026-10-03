/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.drawer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.findMutableMethodOf
import app.morphe.util.getReference
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.settings.DrawerContentFingerprint
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/drawer/CustomizeDrawerPatch;"

@Suppress("unused")
val customizeDrawerPatch = bytecodePatch(
    name = "Customize side bar items",
    description = "Lets you hide entries of the side menu, such as Premium, Communities or Spaces, from the X Patches settings.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(settingsPatch)

    execute {
        setExtensionIsPatchIncluded(EXTENSION_CLASS_DESCRIPTOR)

        // Every entry of the drawer is a composable taking its title first,
        // the entries with and without a trailing badge are separate composables.
        val itemComposables = DrawerContentFingerprint.originalMethod.implementation!!.instructions
            .mapNotNull { it.getReference<MethodReference>() }
            .filter { reference ->
                reference.returnType == "V" &&
                        reference.parameterTypes.firstOrNull() == "Ljava/lang/String;" &&
                        reference.parameterTypes.size >= 7 &&
                        reference.parameterTypes.takeLast(2) == listOf("I", "I") &&
                        reference.parameterTypes.getOrNull(reference.parameterTypes.size - 3)?.endsWith("/Composer;") == true
            }
            .distinctBy { "${it.definingClass}${it.name}${it.parameterTypes}" }

        if (itemComposables.isEmpty()) throw PatchException("Could not find the drawer item composables")

        itemComposables.forEach { reference ->
            val classDef = classDefBy(reference.definingClass)
            val method = classDef.methods.first {
                it.name == reference.name && it.parameterTypes == reference.parameterTypes
            }
            mutableClassDefBy(classDef).findMutableMethodOf(method).addInstructionsWithLabels(
                0,
                """
                    invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->isHidden(Ljava/lang/String;)Z
                    move-result v0
                    if-eqz v0, :show
                    return-void
                    :show
                    nop
                """,
            )
        }
    }
}

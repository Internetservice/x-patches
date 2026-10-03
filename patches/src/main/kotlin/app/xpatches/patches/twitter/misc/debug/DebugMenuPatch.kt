/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.debug

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.resource.ResourceType
import app.morphe.patcher.resourceLiteral
import app.morphe.util.indexOfFirstInstructionReversedOrThrow
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/toggles/DebugMenuPatch;"

/**
 * The drawer content, which shows the "Debug Menu" entry behind a flag of the drawer.
 */
internal object DrawerDebugMenuFingerprint : Fingerprint(
    filters = listOf(
        resourceLiteral(ResourceType.STRING, "drawer_debug_menu_title"),
    ),
)

@Suppress("unused")
val debugMenuPatch = bytecodePatch(
    name = "Enable debug menu",
    description = "Shows the hidden Debug Menu entry of X in the side menu. Off by default in the X Patches settings.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(settingsPatch)

    execute {
        setExtensionIsPatchIncluded(EXTENSION_CLASS_DESCRIPTOR)

        DrawerDebugMenuFingerprint.let {
            val titleIndex = it.instructionMatches.first().index
            it.method.apply {
                // The flag read right before the entry is composed.
                val flagIndex = indexOfFirstInstructionReversedOrThrow(titleIndex, Opcode.IGET_BOOLEAN)
                val register = getInstruction<TwoRegisterInstruction>(flagIndex).registerA
                addInstructions(
                    flagIndex + 1,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->showDebugMenu(Z)Z
                        move-result v$register
                    """,
                )
            }
        }
    }
}

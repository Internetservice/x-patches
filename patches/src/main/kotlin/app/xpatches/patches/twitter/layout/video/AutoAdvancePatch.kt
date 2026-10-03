/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.video

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.indexOfFirstStringInstructionOrThrow
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/toggles/AutoAdvancePatch;"
private const val AUTO_ADVANCE_KEY = "persistent_video_settings_auto_advance_enabled"

@Suppress("unused")
val autoAdvancePatch = bytecodePatch(
    name = "Control video auto advance",
    description = "Lets you stop the immersive video player from advancing to the next video on its own.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(settingsPatch)

    execute {
        setExtensionIsPatchIncluded(EXTENSION_CLASS_DESCRIPTOR)

        PersistentVideoSettingsLoadFingerprint.method.apply {
            val keyIndex = indexOfFirstStringInstructionOrThrow(AUTO_ADVANCE_KEY)
            val resultIndex = indexOfFirstInstructionOrThrow(keyIndex, Opcode.MOVE_RESULT)
            val register = getInstruction<OneRegisterInstruction>(resultIndex).registerA
            addInstructions(
                resultIndex + 1,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->isAutoAdvanceEnabled(Z)Z
                    move-result v$register
                """,
            )
        }
    }
}

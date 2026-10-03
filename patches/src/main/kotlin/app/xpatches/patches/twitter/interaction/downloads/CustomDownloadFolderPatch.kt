/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.interaction.downloads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/downloads/CustomDownloadFolderPatch;"

/**
 * Builds the download manager request, saving media to Download/X.
 */
internal object DownloadRequestFingerprint : Fingerprint(
    returnType = "Landroid/app/DownloadManager\$Request;",
    strings = listOf("X/", "XWS-FileSaving"),
)

@Suppress("unused")
val customDownloadFolderPatch = bytecodePatch(
    name = "Custom download folder",
    description = "Lets you choose the folder media is downloaded to, instead of Download/X.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(settingsPatch)

    execute {
        setExtensionIsPatchIncluded(EXTENSION_CLASS_DESCRIPTOR)

        DownloadRequestFingerprint.method.apply {
            val destinationIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.name == "setDestinationInExternalPublicDir"
            }
            val call = getInstruction<FiveRegisterInstruction>(destinationIndex)
            val directoryRegister = call.registerD
            val subPathRegister = call.registerE
            addInstructions(
                destinationIndex,
                """
                    invoke-static/range { v$directoryRegister .. v$directoryRegister }, $EXTENSION_CLASS_DESCRIPTOR->downloadDirectory(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$directoryRegister
                    invoke-static/range { v$subPathRegister .. v$subPathRegister }, $EXTENSION_CLASS_DESCRIPTOR->downloadSubPath(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$subPathRegister
                """,
            )
        }
    }
}

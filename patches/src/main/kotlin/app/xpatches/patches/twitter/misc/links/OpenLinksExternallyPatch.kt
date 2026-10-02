/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.links

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import app.morphe.util.getReference
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.extension.sharedExtensionPatch
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val TOGGLE_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/toggles/OpenLinksExternallyPatch;"

/**
 * Resolves the link opening mode preference to its enum value.
 * The external browser constant is loaded right after the preference value is compared.
 */
private object LinkOpeningModeFingerprint : Fingerprint(
    filters = listOf(
        string("navigation_link_opening_mode"),
        string("external_browser"),
        opcode(Opcode.SGET_OBJECT, MatchAfterWithin(8)),
    ),
)

@Suppress("unused")
val openLinksExternallyPatch = bytecodePatch(
    name = "Open links externally",
    description = "Always opens links in the external browser instead of the in-app browser, regardless of the link opening setting.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(sharedExtensionPatch, settingsPatch)

    execute {
        setExtensionIsPatchIncluded(TOGGLE_CLASS_DESCRIPTOR)

        LinkOpeningModeFingerprint.let {
            it.method.apply {
                val externalBrowser = it.instructionMatches.last().instruction.getReference<FieldReference>()
                    ?: throw PatchException("Could not find the external browser constant")

                if (implementation!!.registerCount - 1 < 1) throw PatchException("No free register")

                addInstructionsWithLabels(
                    0,
                    """
                        invoke-static { }, $TOGGLE_CLASS_DESCRIPTOR->openLinksExternally()Z
                        move-result v0
                        if-eqz v0, :original
                        sget-object v0, ${externalBrowser.definingClass}->${externalBrowser.name}:${externalBrowser.type}
                        return-object v0
                        :original
                        nop
                    """,
                )
            }
        }
    }
}

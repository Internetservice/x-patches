/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.settings

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.resource.ResourceType
import app.morphe.patcher.resourceLiteral
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.indexOfFirstInstructionReversedOrThrow
import app.xpatches.patches.twitter.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val ACTION_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/settings/OpenSettingsAction;"
private const val ENTRY_TITLE = "X Patches"

/**
 * The navigation drawer content. Its bottom section emits "Settings and privacy" and
 * "Help Center" through the drawer item composable.
 */
private object DrawerContentFingerprint : Fingerprint(
    filters = listOf(
        resourceLiteral(ResourceType.STRING, "drawer_settings_title"),
        resourceLiteral(ResourceType.STRING, "help_center"),
    ),
)

/**
 * Adds an "X Patches" entry after "Help Center" in the navigation drawer, built with the same
 * composable as the entries around it.
 */
internal val drawerEntryPatch = bytecodePatch(
    default = false,
) {
    dependsOn(sharedExtensionPatch)

    execute {
        DrawerContentFingerprint.let {
            it.method.apply {
                val settingsTitleIndex = it.instructionMatches[0].index
                val helpCenterTitleIndex = it.instructionMatches[1].index

                // The drawer item composable call of the Help Center entry:
                // invoke-static/range { title, icon, onClick, modifier, trailing, composer, changed, defaults }
                val itemCallIndex = indexOfFirstInstructionOrThrow(helpCenterTitleIndex, Opcode.INVOKE_STATIC_RANGE)
                val itemCall = getInstruction<RegisterRangeInstruction>(itemCallIndex)
                val itemComposable = itemCall.getReference<MethodReference>()
                    ?: throw PatchException("Could not find the drawer item composable")
                if (itemCall.registerCount != 8) {
                    throw PatchException("Unexpected drawer item composable: $itemComposable")
                }
                val r = itemCall.startRegister

                // The composer is restored from the call registers right after the call.
                val restore = getInstruction<TwoRegisterInstruction>(itemCallIndex + 1)
                if (restore.opcode != Opcode.MOVE_OBJECT || restore.registerB != r + 5) {
                    throw PatchException("Unexpected instruction after the drawer item call: $restore")
                }
                val composerRegister = restore.registerA

                // The defaults mask the surrounding entries pass.
                val defaultsIndex = indexOfFirstInstructionReversedOrThrow(itemCallIndex) {
                    this is WideLiteralInstruction && (this as OneRegisterInstruction).registerA == r + 7
                }
                val defaults = getInstruction<WideLiteralInstruction>(defaultsIndex).wideLiteral

                // The icon of the Settings and privacy entry.
                val iconIndex = indexOfFirstInstructionOrThrow(settingsTitleIndex, Opcode.SGET_OBJECT)
                val icon = getInstruction(iconIndex).getReference<FieldReference>()
                    ?: throw PatchException("Could not find the settings icon")

                val parameters = itemComposable.parameterTypes.joinToString("")
                addInstructions(
                    itemCallIndex + 2,
                    """
                        const-string v$r, "$ENTRY_TITLE"
                        sget-object v${r + 1}, ${icon.definingClass}->${icon.name}:${icon.type}
                        new-instance v${r + 2}, $ACTION_CLASS_DESCRIPTOR
                        invoke-direct { v${r + 2} }, $ACTION_CLASS_DESCRIPTOR-><init>()V
                        const/16 v${r + 3}, 0x0
                        const/16 v${r + 4}, 0x0
                        move-object/from16 v${r + 5}, v$composerRegister
                        const/16 v${r + 6}, 0x0
                        const/16 v${r + 7}, $defaults
                        invoke-static/range { v$r .. v${r + 7} }, ${itemComposable.definingClass}->${itemComposable.name}($parameters)${itemComposable.returnType}
                        move-object/from16 v$composerRegister, v${r + 5}
                    """,
                )
            }
        }
    }
}

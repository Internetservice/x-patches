/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.video

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.hook.json.addJsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHook
import app.xpatches.patches.twitter.misc.hook.json.jsonHookPatch
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val HOOK_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/hook/patch/immersive/HideImmersiveFeedHook;"
private const val PAGER_STATE = "Landroidx/compose/foundation/pager/d;"
private val REMEMBER_PAGER_STATE_PARAMETERS = listOf("I", "Lkotlin/jvm/functions/Function0;", "Landroidx/compose/runtime/Composer;", "I", "I")

private fun Method.remembersPagerState() = implementation?.instructions?.any { instruction ->
    instruction.getReference<MethodReference>()?.let {
        it.returnType == PAGER_STATE && it.parameterTypes == REMEMBER_PAGER_STATE_PARAMETERS
    } == true
} == true

/**
 * The composable of the immersive video player: a large lambda remembering the pager state
 * of the vertical feed, with the opened video as its first page.
 */
internal object ImmersivePagerFingerprint : Fingerprint(
    name = "invoke",
    custom = { method, classDef ->
        "Lkotlin/jvm/functions/Function2;" in classDef.interfaces &&
                classDef.fields.count() > 40 &&
                method.remembersPagerState()
    },
)

@Suppress("unused")
val hideImmersiveFeedPatch = bytecodePatch(
    name = "Hide immersive feed",
    description = "Keeps the full screen video player on the video you opened, with no swiping to more videos.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(jsonHookPatch, settingsPatch)

    execute {
        setExtensionIsPatchIncluded(HOOK_CLASS_DESCRIPTOR)

        // The feeds loaded for the player are stripped down to the opened video.
        addJsonHook(jsonHook(HOOK_CLASS_DESCRIPTOR))

        // The page count of the vertical pager is limited to one, so there is nothing to swipe to.
        ImmersivePagerFingerprint.method.apply {
            val rememberIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_STATIC && getReference<MethodReference>()?.let {
                    it.returnType == PAGER_STATE && it.parameterTypes == REMEMBER_PAGER_STATE_PARAMETERS
                } == true
            }
            val pageCountRegister = getInstruction<FiveRegisterInstruction>(rememberIndex).registerD
            addInstructions(
                rememberIndex,
                """
                    invoke-static/range { v$pageCountRegister .. v$pageCountRegister }, $HOOK_CLASS_DESCRIPTOR->limitPageCount(Ljava/lang/Object;)Ljava/lang/Object;
                    move-result-object v$pageCountRegister
                    check-cast v$pageCountRegister, Lkotlin/jvm/functions/Function0;
                """,
            )
        }
    }
}

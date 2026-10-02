/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.hook.json

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.Opcode

private const val JSON_HOOK_CLASS_NAMESPACE = "app/xpatches/extension/twitter/patches/hook/json"
internal const val JSON_HOOK_PATCH_CLASS_DESCRIPTOR = "L$JSON_HOOK_CLASS_NAMESPACE/JsonHookPatch;"
internal const val BASE_JSON_HOOK_CLASS_DESCRIPTOR = "L$JSON_HOOK_CLASS_NAMESPACE/BaseJsonHook;"
internal const val DUMMY_HOOK_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/hook/patch/dummy/DummyHook;"

/**
 * Static initializer of the extension JsonHookPatch class, matched at `hooks.add(DummyHook.INSTANCE)`.
 */
internal object JsonHookPatchFingerprint : Fingerprint(
    definingClass = JSON_HOOK_PATCH_CLASS_DESCRIPTOR,
    name = "<clinit>",
    filters = listOf(
        // Get DummyHook object.
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = DUMMY_HOOK_CLASS_DESCRIPTOR,
            name = "INSTANCE",
        ),
        // Add hook to the hooks list.
        methodCall(
            smali = "Ljava/util/List;->add(Ljava/lang/Object;)Z",
            opcodes = listOf(Opcode.INVOKE_INTERFACE),
            location = InstructionLocation.MatchAfterImmediately(),
        ),
    ),
)

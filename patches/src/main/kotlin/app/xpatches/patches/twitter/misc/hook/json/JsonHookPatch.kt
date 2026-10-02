/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.hook.json

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.xpatches.patches.twitter.misc.extension.sharedExtensionPatch
import app.xpatches.patches.twitter.misc.hook.okhttp.customNetworkInterceptorPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import java.io.InvalidClassException

/**
 * Add a hook to the [jsonHookPatch].
 * Will not add the hook if it's already added.
 *
 * @param jsonHook The [JsonHook] to add.
 */
fun BytecodePatchContext.addJsonHook(jsonHook: JsonHook) {
    if (jsonHook.added) return

    JsonHookPatchFingerprint.let {
        // Insert the hook right after `hooks.add(DummyHook.INSTANCE)`,
        // reusing the registers of that call.
        val addIndex = it.instructionMatches.last().index
        val addInstruction = it.method.getInstruction<FiveRegisterInstruction>(addIndex)
        val listRegister = addInstruction.registerC
        val hookRegister = addInstruction.registerD

        it.method.addInstructions(
            addIndex + 1,
            """
                sget-object v$hookRegister, ${jsonHook.descriptor}->INSTANCE:${jsonHook.descriptor}
                invoke-interface { v$listRegister, v$hookRegister }, Ljava/util/List;->add(Ljava/lang/Object;)Z
            """,
        )
    }

    jsonHook.added = true
}

/**
 * Routes the JSON responses of the app through the extension hooks.
 */
val jsonHookPatch = bytecodePatch(
    description = "Hooks the stream which reads JSON responses.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(
        sharedExtensionPatch,
        customNetworkInterceptorPatch,
    )

    execute {
        JsonHookPatchFingerprint.methodOrNull
            ?: throw PatchException("Unexpected extension.")
    }

    finalize {
        // All hooks are added by now. Remove the 2 instructions that add DummyHook.
        JsonHookPatchFingerprint.let {
            val getDummyHookIndex = it.instructionMatches.first().index
            it.method.removeInstructions(getDummyHookIndex, 2)
        }
    }
}

class JsonHook internal constructor(
    internal val descriptor: String,
) {
    internal var added = false
}

/**
 * Create a hook class.
 * The class has to extend on **BaseJsonHook**.
 * The class has to be a Kotlin object class, or at least have an INSTANCE field of itself.
 *
 * @param descriptor The class descriptor of the hook.
 * @throws InvalidClassException If the class is not a hook class.
 */
fun BytecodePatchContext.jsonHook(descriptor: String): JsonHook {
    classDefBy(descriptor).also { classDef ->
        if (
            classDef.superclass != BASE_JSON_HOOK_CLASS_DESCRIPTOR ||
            !classDef.fields.any { field -> field.name == "INSTANCE" }
        ) {
            throw InvalidClassException(classDef.type, "Not a hook class")
        }
    }

    return JsonHook(descriptor)
}

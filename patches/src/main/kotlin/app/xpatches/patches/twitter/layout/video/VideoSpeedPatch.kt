/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.video

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.findMutableMethodOf
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.indexOfFirstInstructionReversedOrThrow
import app.morphe.util.indexOfFirstStringInstructionOrThrow
import app.morphe.util.returnEarly
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/video/VideoSpeedPatch;"

@Suppress("unused")
val videoSpeedPatch = bytecodePatch(
    name = "Video speed",
    description = "Remembers the playback speed for every video and lets you set your own speed levels, " +
            "such as 0.25, 0.5, 0.75, 1, 1.5, 2, 2.5 and 3.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(settingsPatch)

    execute {
        setExtensionIsPatchIncluded(EXTENSION_CLASS_DESCRIPTOR)

        // region Remember the speed: the stored "locked speed" flag is forced on.

        PersistentVideoSettingsLoadFingerprint.method.apply {
            val keyIndex = indexOfFirstStringInstructionOrThrow(LOCKED_SPEED_KEY)
            val resultIndex = indexOfFirstInstructionOrThrow(keyIndex, Opcode.MOVE_RESULT)
            val register = getInstruction<OneRegisterInstruction>(resultIndex).registerA
            addInstructions(
                resultIndex + 1,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->isSpeedLocked(Z)Z
                    move-result v$register
                """,
            )
        }

        // endregion

        // region Speed levels: the enum values are rebuilt from the settings.

        val enumClass = PlaybackSpeedEnumFingerprint.classDef
        val enumType = enumClass.type
        val clinit = PlaybackSpeedEnumFingerprint.method

        // The extension initializes the enum itself when a level is needed before X did.
        EnumClassNameFingerprint.method.returnEarly(enumType.substring(1, enumType.length - 1).replace('/', '.'))

        val valuesField = enumClass.fields.firstOrNull { it.type == "[$enumType" && AccessFlags.STATIC.isSet(it.accessFlags) }
            ?: throw PatchException("Could not find the values array of the speed enum")
        val entriesField = enumClass.fields.firstOrNull { it.type == "Lkotlin/enums/EnumEntries;" }
            ?: throw PatchException("Could not find the entries of the speed enum")
        val constructor = enumClass.methods.firstOrNull { it.name == "<init>" && it.parameterTypes == listOf("Ljava/lang/String;", "I", "F") }
            ?: throw PatchException("Could not find the constructor of the speed enum")

        val entriesIndex = clinit.indexOfFirstInstructionOrThrow { getReference<FieldReference>()?.name == entriesField.name }
        val entriesFactory = clinit.getInstruction(clinit.indexOfFirstInstructionReversedOrThrow(entriesIndex, Opcode.INVOKE_STATIC))
            .getReference<MethodReference>() ?: throw PatchException("Could not find the entries factory of the speed enum")
        val entriesFactoryDescriptor = "${entriesFactory.definingClass}->${entriesFactory.name}(${entriesFactory.parameterTypes.joinToString("")})${entriesFactory.returnType}"

        // Only four registers are available, so the array is held by the extension while it is filled.
        val returnIndex = clinit.indexOfFirstInstructionOrThrow(entriesIndex, Opcode.RETURN_VOID)
        clinit.addInstructionsWithLabels(
            returnIndex,
            """
                sget-object v0, $enumType->${valuesField.name}:[$enumType
                invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->customSpeedCount([Ljava/lang/Object;)I
                move-result v1
                if-eqz v1, :keep
                new-array v0, v1, [$enumType
                invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->setSpeeds([Ljava/lang/Object;)V
                const/4 v2, 0x0
                :next
                if-ge v2, v1, :built
                new-instance v0, $enumType
                invoke-static { v2 }, $EXTENSION_CLASS_DESCRIPTOR->customSpeedName(I)Ljava/lang/String;
                move-result-object v1
                invoke-static { v2 }, $EXTENSION_CLASS_DESCRIPTOR->customSpeedValue(I)F
                move-result v3
                invoke-direct { v0, v1, v2, v3 }, $enumType-><init>(Ljava/lang/String;IF)V
                invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->speeds()[Ljava/lang/Object;
                move-result-object v1
                aput-object v0, v1, v2
                add-int/lit8 v2, v2, 0x1
                invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->customSpeedCount()I
                move-result v1
                goto :next
                :built
                invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->speeds()[Ljava/lang/Object;
                move-result-object v0
                check-cast v0, [$enumType
                sput-object v0, $enumType->${valuesField.name}:[$enumType
                invoke-static { v0 }, $entriesFactoryDescriptor
                move-result-object v0
                sput-object v0, $enumType->${entriesField.name}:Lkotlin/enums/EnumEntries;
                :keep
                nop
            """,
        )

        // Names stored before the levels changed are mapped to a current level.
        enumClass.methods.firstOrNull { it.name == "valueOf" }?.addInstructions(
            0,
            """
                invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->speedName(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p0
            """,
        ) ?: throw PatchException("Could not find valueOf of the speed enum")

        // The levels X refers to directly, such as the default, are resolved among the current levels.
        val constantNames = enumClass.fields.filter { it.type == enumType && AccessFlags.STATIC.isSet(it.accessFlags) }.map { it.name }.toSet()
        classDefForEach { classDef ->
            if (classDef.type == enumType) return@classDefForEach
            classDef.methods.forEach { method ->
                val instructions = method.implementation?.instructions ?: return@forEach
                val usesConstant = instructions.any { instruction ->
                    instruction.opcode == Opcode.SGET_OBJECT && instruction.getReference<FieldReference>().let {
                        it?.definingClass == enumType && it.name in constantNames
                    }
                }
                if (!usesConstant) return@forEach

                mutableClassDefBy(classDef).findMutableMethodOf(method).apply {
                    val mutableInstructions = implementation!!.instructions
                    (mutableInstructions.size - 1 downTo 0).forEach { index ->
                        val instruction = mutableInstructions[index]
                        val field = instruction.getReference<FieldReference>()
                        if (instruction.opcode != Opcode.SGET_OBJECT || field?.definingClass != enumType || field.name !in constantNames) return@forEach
                        val register = (instruction as OneRegisterInstruction).registerA
                        if (register > 255) return@forEach
                        replaceInstruction(index, "const-string v$register, \"${field.name}\"")
                        addInstructions(
                            index + 1,
                            """
                                invoke-static/range { v$register .. v$register }, $EXTENSION_CLASS_DESCRIPTOR->speedNamed(Ljava/lang/String;)Ljava/lang/Object;
                                move-result-object v$register
                                check-cast v$register, $enumType
                            """,
                        )
                    }
                }
            }
        }

        // endregion
    }
}

/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.shared

import app.morphe.patcher.InstructionFilter
import app.morphe.patcher.InstructionLocation
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

/**
 * Instruction filter backed by a plain predicate, for conditions the built in filters cannot express.
 */
internal fun instructionFilter(
    instructionLocation: InstructionLocation = InstructionLocation.MatchAfterAnywhere(),
    predicate: (Instruction) -> Boolean,
) = object : InstructionFilter {
    override val location = instructionLocation

    override fun matches(enclosingMethod: Method, instruction: Instruction) = predicate(instruction)
}

/**
 * Field access with [opcode] whose field reference satisfies [predicate].
 */
internal fun fieldAccessFilter(
    opcode: Opcode,
    instructionLocation: InstructionLocation = InstructionLocation.MatchAfterAnywhere(),
    predicate: (FieldReference) -> Boolean,
) = instructionFilter(instructionLocation) { instruction ->
    instruction.opcode == opcode &&
            (instruction as? ReferenceInstruction)?.reference.let { it is FieldReference && predicate(it) }
}

/**
 * Method call with [opcode] whose method reference satisfies [predicate].
 */
internal fun methodCallFilter(
    opcode: Opcode,
    instructionLocation: InstructionLocation = InstructionLocation.MatchAfterAnywhere(),
    predicate: (MethodReference) -> Boolean,
) = instructionFilter(instructionLocation) { instruction ->
    instruction.opcode == opcode &&
            (instruction as? ReferenceInstruction)?.reference.let { it is MethodReference && predicate(it) }
}

/**
 * Instruction with [opcode] whose type reference satisfies [predicate].
 */
internal fun typeReferenceFilter(
    opcode: Opcode,
    instructionLocation: InstructionLocation = InstructionLocation.MatchAfterAnywhere(),
    predicate: (String) -> Boolean,
) = instructionFilter(instructionLocation) { instruction ->
    instruction.opcode == opcode &&
            (instruction as? ReferenceInstruction)?.reference.let { it is TypeReference && predicate(it.type) }
}

/**
 * @return If all of [flags] are set on this method. Other flags may also be set.
 */
internal fun Method.hasAccessFlags(vararg flags: AccessFlags): Boolean {
    val mask = flags.fold(0) { acc, flag -> acc or flag.value }
    return accessFlags and mask == mask
}

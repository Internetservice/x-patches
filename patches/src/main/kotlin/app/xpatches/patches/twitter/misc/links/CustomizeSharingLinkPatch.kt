/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.links

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.indexOfFirstInstructionReversedOrThrow
import app.morphe.util.indexOfFirstStringInstructionOrThrow
import app.morphe.util.returnEarly
import app.xpatches.patches.twitter.misc.extension.sharedExtensionPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.logging.Logger

@Suppress("unused")
val customizeSharingLinkPatch = bytecodePatch(
    name = "Customize sharing link",
    description = "Changes the domain name used when sharing links, and optionally includes the username in the link.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(sharedExtensionPatch)

    val returnUsernameOption = booleanOption(
        key = "returnUsername",
        default = true,
        title = "Return username",
        description = "Whether to include the username in the shared link.",
    )

    val domainNameOption = stringOption(
        key = "domainName",
        default = "x.com",
        values = mapOf(
            "Default" to "x.com",
            "FxTwitter" to "fxtwitter.com",
        ),
        title = "Domain name",
        description = "The domain name to use when sharing links.",
        required = true,
    ) { domain ->
        // Do a courtesy check if the host can be resolved.
        // If it does not resolve, then print a warning but use the host anyway.
        // Unresolvable hosts should not be rejected, since the patching environment
        // may not allow network connections or the network may be down.
        try {
            InetAddress.getByName(domain)
        } catch (_: UnknownHostException) {
            Logger.getLogger(this::class.java.name).warning(
                "Host \"$domain\" did not resolve to any domain.",
            )
        } catch (_: Exception) {
            // Must ignore any kind of exception. Trying to resolve network
            // on Manager throws android.os.NetworkOnMainThreadException
        }

        true
    }

    execute {
        val returnUsername = returnUsernameOption.value!!
        val domainName = domainNameOption.value!!

        // Replace the isReturnUsernameEnabled in the link sharing extension methods.
        ReturnUsernameHelperFingerprint.method.returnEarly(returnUsername)

        // Replace the domain name in the link sharing extension methods.
        LinkSharingDomainHelperFingerprint.method.returnEarly(domainName)

        // The XChat share sheet of 12.10 is the only method combining the "tweet-" prefix
        // with the post link, which makes it a reliable version marker.
        if (LinkInternalShareSheetFingerprint.matchOrNull() != null) {
            patchLegacyShareSheets()
        } else {
            patchShareSheets()
        }
    }
}

/**
 * X 12.10 and older.
 */
private fun BytecodePatchContext.patchLegacyShareSheets() {
    // Formats share link such as sharing through XChat.
    LinkInternalShareSheetFingerprint.let {
        it.method.apply {
            val statusStringIndex = it.instructionMatches[1].index
            val statusStringRegister = getInstruction<OneRegisterInstruction>(statusStringIndex).registerA

            val contextualPostIndex = it.instructionMatches[2].index
            val contextualPostRegister = getInstruction<TwoRegisterInstruction>(contextualPostIndex).registerA

            addInstructions(
                contextualPostIndex + 1,
                """
                    invoke-static/range { v$contextualPostRegister .. v$contextualPostRegister }, $EXTENSION_CLASS_DESCRIPTOR->formatInternalShareSheetLink(Ljava/lang/Object;)Ljava/lang/String;
                    move-result-object v$statusStringRegister
                """,
            )
        }
    }

    // Formats share link such as "Copy link" or "Share via..." etc.
    LinkExternalShareSheetFingerprint.let {
        it.method.apply {
            val rootContextualPostIndex = it.instructionMatches[0].index
            val rootContextualPostRegister = getInstruction<OneRegisterInstruction>(rootContextualPostIndex).registerA

            val statusStringIndex = it.instructionMatches[3].index
            val statusStringRegister = getInstruction<OneRegisterInstruction>(statusStringIndex).registerA

            addInstructions(
                statusStringIndex + 1,
                """
                    invoke-static/range { v$rootContextualPostRegister .. v$rootContextualPostRegister }, $EXTENSION_CLASS_DESCRIPTOR->formatExternalShareSheetLink(Ljava/lang/Object;)Ljava/lang/String;
                    move-result-object v$statusStringRegister
                """,
            )
        }
    }
}

/**
 * X 12.30 and newer.
 */
private fun BytecodePatchContext.patchShareSheets() {
    // "Copy link" / "Share via..." share sheet.
    ShareIntentFingerprint.method.apply {
        val statusStringIndex = indexOfFirstStringInstructionOrThrow(STATUS_LINK_PREFIX)

        // The share request object is cast to the post share request type right before the link is built.
        val shareRequestIndex = indexOfFirstInstructionReversedOrThrow(statusStringIndex, Opcode.CHECK_CAST)
        val shareRequestRegister = getInstruction<OneRegisterInstruction>(shareRequestIndex).registerA

        // The link is the result of StringBuilder.toString() right after the status prefix.
        val toStringIndex = indexOfFirstInstructionOrThrow(
            statusStringIndex,
            methodCall(smali = "Ljava/lang/StringBuilder;->toString()Ljava/lang/String;"),
        )
        val linkIndex = toStringIndex + 1
        val linkInstruction = getInstruction<OneRegisterInstruction>(linkIndex)
        if (linkInstruction.opcode != Opcode.MOVE_RESULT_OBJECT) {
            throw PatchException("Unexpected instruction after StringBuilder.toString(): $linkInstruction")
        }
        val linkRegister = linkInstruction.registerA

        // Modify from the last index to the first to keep the indexes valid.
        addInstructions(
            linkIndex + 1,
            """
                invoke-static/range { v$linkRegister .. v$linkRegister }, $EXTENSION_CLASS_DESCRIPTOR->formatExternalShareSheetLink(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$linkRegister
            """,
        )
        addInstructions(
            shareRequestIndex + 1,
            """
                invoke-static/range { v$shareRequestRegister .. v$shareRequestRegister }, $EXTENSION_CLASS_DESCRIPTOR->setShareRequest(Ljava/lang/Object;)V
            """,
        )
    }

    // XChat share sheet.
    ChatShareSheetConstructorFingerprint.method.apply {
        val statusStringIndex = indexOfFirstStringInstructionOrThrow(STATUS_LINK_PREFIX)

        // The resolved link is stored in a String field. The store is a branch target,
        // so the link is re-formatted and stored again right after it.
        val storeIndex = indexOfFirstInstructionOrThrow(
            statusStringIndex,
            fieldAccess(opcode = Opcode.IPUT_OBJECT, type = "Ljava/lang/String;"),
        )
        val storeInstruction = getInstruction<TwoRegisterInstruction>(storeIndex)
        val linkRegister = storeInstruction.registerA
        val componentRegister = storeInstruction.registerB
        val linkField = storeInstruction.getReference<FieldReference>()
            ?: throw PatchException("Could not find the share link field")

        addInstructions(
            storeIndex + 1,
            """
                invoke-static/range { v$linkRegister .. v$linkRegister }, $EXTENSION_CLASS_DESCRIPTOR->formatShareUrl(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$linkRegister
                iput-object v$linkRegister, v$componentRegister, ${linkField.definingClass}->${linkField.name}:${linkField.type}
            """,
        )
    }
}

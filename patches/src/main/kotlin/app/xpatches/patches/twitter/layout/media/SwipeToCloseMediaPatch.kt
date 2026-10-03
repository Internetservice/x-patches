/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.media

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.morphe.util.setExtensionIsPatchIncluded
import app.xpatches.patches.twitter.misc.settings.settingsPatch
import app.xpatches.patches.twitter.shared.Constants.COMPATIBILITY_X
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/xpatches/extension/twitter/patches/media/SwipeToCloseMediaPatch;"

/**
 * The event handler of the media viewer, which opens photos and videos of a post full screen.
 */
internal object MediaViewerEventFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("L"),
    strings = listOf("gallery", "photo", "gif", "save"),
    custom = { method, _ -> method.name != "<init>" },
)

/**
 * The event handler of the immersive video tab.
 */
internal object VideoTabEventFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("L"),
    strings = listOf("long_press_to_speed_up", "double_tap_to_like"),
)

private fun extensionStringFingerprint(name: String) = Fingerprint(
    definingClass = EXTENSION_CLASS_DESCRIPTOR,
    name = name,
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
)

@Suppress("unused")
val swipeToCloseMediaPatch = bytecodePatch(
    name = "Swipe to close media",
    description = "Closes a full screen photo or video by swiping right, on the first photo of a post or anywhere on a video.",
) {
    compatibleWith(COMPATIBILITY_X)

    dependsOn(settingsPatch)

    execute {
        setExtensionIsPatchIncluded(EXTENSION_CLASS_DESCRIPTOR)

        // The events are identified by the names their toString prints, among the classes
        // implementing the event interface of the handler, as the names repeat across features.
        val mediaEventInterface = MediaViewerEventFingerprint.originalMethod.parameterTypes[0]
        val videoEventInterface = VideoTabEventFingerprint.originalMethod.parameterTypes[0]
        mapOf(
            "mediaChangedEvent" to ("DidChangeVisibleMedia(" to mediaEventInterface),
            "mediaCloseEvent" to ("DidClickBackButton" to mediaEventInterface),
            "pageChangedEvent" to ("PageChanged(" to videoEventInterface),
            "videoCloseEvent" to ("CloseClicked" to videoEventInterface),
        ).forEach { (method, event) ->
            val (toStringPrefix, eventInterface) = event
            extensionStringFingerprint(method).method.returnEarly(javaClassName(classWithToString(toStringPrefix, eventInterface)))
        }

        listOf(MediaViewerEventFingerprint, VideoTabEventFingerprint).forEach { fingerprint ->
            fingerprint.method.addInstructions(
                0,
                "invoke-static/range { p0 .. p1 }, $EXTENSION_CLASS_DESCRIPTOR->onViewerEvent(Ljava/lang/Object;Ljava/lang/Object;)V",
            )
        }
    }
}

/**
 * @return The type of the class implementing [eventInterface] whose toString starts with [prefix].
 */
private fun BytecodePatchContext.classWithToString(prefix: String, eventInterface: CharSequence): String {
    var found: String? = null
    classDefForEach { classDef ->
        if (found != null || eventInterface !in classDef.interfaces) return@classDefForEach
        val toString = classDef.methods.firstOrNull { it.name == "toString" } ?: return@classDefForEach
        val startsWithPrefix = toString.implementation?.instructions?.any { instruction ->
            ((instruction as? ReferenceInstruction)?.reference as? StringReference)?.string?.startsWith(prefix) == true
        } == true
        if (startsWithPrefix) found = classDef.type
    }
    return found ?: throw PatchException("Could not find the $eventInterface event printing $prefix")
}

private fun javaClassName(type: String) = type.substring(1, type.length - 1).replace('/', '.')

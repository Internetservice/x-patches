/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.media

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
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

/**
 * canConsumePanChange of the zoomable state of the Telephoto library, which the photo viewer
 * uses. Asked before every drag of a photo whether the zoomed content can move that way.
 */
internal object ZoomablePanCheckFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("J"),
    strings = listOf("panDelta"),
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
            "immersiveEvent" to ("DidSwipeToImmersive" to mediaEventInterface),
        ).forEach { (method, event) ->
            val (toStringPrefix, eventInterface) = event
            // The swipe to the immersive player is gone from newer builds.
            val optional = method == "immersiveEvent"
            val type = classWithToStringOrNull(toStringPrefix, eventInterface)
                ?: if (optional) null else throw PatchException("Could not find the $eventInterface event printing $toStringPrefix")
            extensionStringFingerprint(method).method.returnEarly(type?.let(::javaClassName) ?: "")
        }

        // Dragging a zoomed in photo around is not a swipe. The zoom state reports its zoom
        // fraction to the extension whenever a drag is about to move the photo.
        ZoomablePanCheckFingerprint.let { fingerprint ->
            val method = fingerprint.method
            val zoomFraction = fingerprint.classDef.methods.single {
                it.returnType == "Ljava/lang/Float;" && it.parameterTypes.isEmpty()
            }
            val implementation = method.implementation ?: throw PatchException("Pan check has no code")
            // Three parameter registers: this and the Offset long. v0 is free scratch.
            if (implementation.registerCount < 4) throw PatchException("Pan check has no scratch register")
            method.addInstructions(
                0,
                """
                    invoke-virtual { p0 }, ${fingerprint.classDef.type}->${zoomFraction.name}()Ljava/lang/Float;
                    move-result-object v0
                    invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->onZoomFraction(Ljava/lang/Float;)V
                """,
            )
        }

        // The handlers tell the extension about every event, which may consume it,
        // such as the swipe up to the immersive player when it is hidden.
        listOf(MediaViewerEventFingerprint, VideoTabEventFingerprint).forEach { fingerprint ->
            fingerprint.method.addInstructionsWithLabels(
                0,
                """
                    invoke-static/range { p0 .. p1 }, $EXTENSION_CLASS_DESCRIPTOR->onViewerEvent(Ljava/lang/Object;Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :handle
                    return-void
                    :handle
                    nop
                """,
            )
        }
    }
}

/**
 * @return The type of the class implementing [eventInterface] whose toString starts with [prefix].
 */
private fun BytecodePatchContext.classWithToStringOrNull(prefix: String, eventInterface: CharSequence): String? {
    var found: String? = null
    classDefForEach { classDef ->
        if (found != null || eventInterface !in classDef.interfaces) return@classDefForEach
        val toString = classDef.methods.firstOrNull { it.name == "toString" } ?: return@classDefForEach
        val startsWithPrefix = toString.implementation?.instructions?.any { instruction ->
            ((instruction as? ReferenceInstruction)?.reference as? StringReference)?.string?.startsWith(prefix) == true
        } == true
        if (startsWithPrefix) found = classDef.type
    }
    return found
}

private fun javaClassName(type: String) = type.substring(1, type.length - 1).replace('/', '.')

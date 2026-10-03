/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.video

import app.morphe.patcher.Fingerprint
import app.xpatches.patches.twitter.shared.hasAccessFlags
import com.android.tools.smali.dexlib2.AccessFlags

internal const val LOCKED_SPEED_KEY = "persistent_video_settings_has_locked_playback_speed"

/**
 * Loads the persistent video settings from the shared preferences, including the playback
 * speed and whether it is locked, which makes X keep it for every video.
 */
internal object PersistentVideoSettingsLoadFingerprint : Fingerprint(
    name = "<init>",
    returnType = "V",
    strings = listOf(
        LOCKED_SPEED_KEY,
        "persistent_video_settings_playback_speed",
    ),
)

/**
 * The playback speed enum of the video player: X_5, X1, X1_25, X1_5, X2, X2_5 and X3.
 */
internal object PlaybackSpeedEnumFingerprint : Fingerprint(
    name = "<clinit>",
    returnType = "V",
    strings = listOf("X_5", "X1", "X1_25", "X1_5", "X2"),
    custom = { method, classDef ->
        method.hasAccessFlags(AccessFlags.STATIC) && classDef.superclass == "Ljava/lang/Enum;"
    },
)

/**
 * The extension method returning the class name of the speed enum, filled in during patching.
 */
internal object EnumClassNameFingerprint : Fingerprint(
    definingClass = "Lapp/xpatches/extension/twitter/patches/video/VideoSpeedPatch;",
    name = "enumClassName",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
)

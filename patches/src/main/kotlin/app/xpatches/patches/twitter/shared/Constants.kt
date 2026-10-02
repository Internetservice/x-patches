/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    private const val X_PACKAGE_NAME = "com.twitter.android"
    private const val X_APP_NAME = "X"
    private const val X_ICON_COLOR = 0x000000

    // Verified with these patches, newest first.
    private val TARGET_12_10_1 = AppTarget(version = "12.10.1-release.0")
    // Versions verified upstream by ReVanced.
    private val TARGET_12_10_0 = AppTarget(version = "12.10.0-release.0")
    private val TARGET_12_8_0 = AppTarget(version = "12.8.0-release.0")
    private val TARGET_11_80_0 = AppTarget(version = "11.80.0-release.0")

    // Newer releases are not verified but are expected to work until X changes the hooked code.
    private val TARGET_ANY_EXPERIMENTAL = AppTarget(version = null, isExperimental = true)

    /**
     * All supported X versions.
     */
    val COMPATIBILITY_X = Compatibility(
        name = X_APP_NAME,
        packageName = X_PACKAGE_NAME,
        apkFileType = ApkFileType.APK,
        appIconColor = X_ICON_COLOR,
        targets = listOf(
            TARGET_ANY_EXPERIMENTAL,
            TARGET_12_10_1,
            TARGET_12_10_0,
            TARGET_12_8_0,
            TARGET_11_80_0,
        )
    )

    /**
     * Patches whose hooks only exist in the 12.x code base.
     */
    val COMPATIBILITY_X_12 = Compatibility(
        name = X_APP_NAME,
        packageName = X_PACKAGE_NAME,
        apkFileType = ApkFileType.APK,
        appIconColor = X_ICON_COLOR,
        targets = listOf(
            TARGET_ANY_EXPERIMENTAL,
            TARGET_12_10_1,
            TARGET_12_10_0,
            TARGET_12_8_0,
        )
    )

    /**
     * Patches that do not depend on a specific version.
     */
    val COMPATIBILITY_X_ANY = Compatibility(
        name = X_APP_NAME,
        packageName = X_PACKAGE_NAME,
        apkFileType = ApkFileType.APK,
        appIconColor = X_ICON_COLOR,
    )
}

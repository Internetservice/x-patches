/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.shared

import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.Method

/**
 * @return If all of [flags] are set on this method. Other flags may also be set.
 */
internal fun Method.hasAccessFlags(vararg flags: AccessFlags): Boolean {
    val mask = flags.fold(0) { acc, flag -> acc or flag.value }
    return accessFlags and mask == mask
}

/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.hook

@Suppress("unused")
val showSensitiveMediaPatch = hookPatch(
    name = "Show sensitive media",
    description = "Shows media marked as sensitive directly, without the warning overlays. Off by default in the X Patches settings.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/sensitive/ShowSensitiveMediaHook;",
)

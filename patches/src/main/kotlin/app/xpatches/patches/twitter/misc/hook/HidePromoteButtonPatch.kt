/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.hook

@Suppress("unused")
val hidePromoteButtonPatch = hookPatch(
    name = "Hide promote button",
    description = "Hides the \"Promote\" button on your own posts.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/promote/HidePromoteButtonHook;",
)

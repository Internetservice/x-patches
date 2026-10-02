/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.hook

@Suppress("unused")
val forceHdVideoPatch = hookPatch(
    name = "Force HD video",
    description = "Plays videos in their highest available quality by removing the lower quality variants.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/video/ForceHdVideoHook;",
    default = false,
)

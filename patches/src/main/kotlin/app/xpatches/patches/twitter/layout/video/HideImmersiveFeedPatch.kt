/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.video

import app.xpatches.patches.twitter.misc.hook.hookPatch

/**
 * The immersive player fills its vertical feed from the "ImmersiveViewer" mixer timelines,
 * with the opened video pinned first. Keeping only that entry leaves nothing to swipe to.
 */
@Suppress("unused")
val hideImmersiveFeedPatch = hookPatch(
    name = "Hide immersive feed",
    description = "Keeps the full screen video player on the video you opened, with no swiping to more videos.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/immersive/HideImmersiveFeedHook;",
)

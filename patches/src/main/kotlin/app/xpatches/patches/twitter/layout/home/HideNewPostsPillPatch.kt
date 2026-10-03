/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.home

import app.xpatches.patches.twitter.misc.hook.hookPatch

@Suppress("unused")
val hideNewPostsPillPatch = hookPatch(
    name = "Hide new posts pill",
    description = "Hides the \"New posts\" pill at the top of the timelines.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/home/HideNewPostsPillHook;",
)

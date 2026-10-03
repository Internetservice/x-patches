/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.replies

import app.xpatches.patches.twitter.misc.hook.hookPatch

@Suppress("unused")
val hideHiddenRepliesPatch = hookPatch(
    name = "Hide hidden replies",
    description = "Hides the \"Show more replies\" and \"Show additional replies\" prompts under posts.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/replies/HideHiddenRepliesHook;",
)

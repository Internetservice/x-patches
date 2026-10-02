/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.hook

@Suppress("unused")
val hideSuggestedContentPatch = hookPatch(
    name = "Hide suggested content",
    description = "Hides the suggestion modules X injects into timelines: communities to join, related posts under a post, Today's news stories and the top people module in search.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/suggested/HideSuggestedContentHook;",
)

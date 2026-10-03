/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.hook

@Suppress("unused")
val hideSocialContextPatch = hookPatch(
    name = "Hide social context",
    description = "Hides the context lines above posts, such as \"X follows\", \"Liked by\" and \"You might like\".",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/social/HideSocialContextHook;",
)

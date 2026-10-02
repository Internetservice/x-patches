/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.hook

@Suppress("unused")
val hideVerifiedBadgesPatch = hookPatch(
    name = "Hide verified badges",
    description = "Hides the verification checkmarks and affiliation badges of users.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/verified/HideVerifiedBadgesHook;",
    default = false,
)

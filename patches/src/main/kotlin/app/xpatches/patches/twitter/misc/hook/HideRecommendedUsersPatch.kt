/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.patches.twitter.misc.hook

@Suppress("unused")
val hideRecommendedUsersPatch = hookPatch(
    name = "Hide recommended users",
    description = "Hides the 'Who to follow' recommendations in the timelines.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/recommendation/RecommendedUsersHook;",
)

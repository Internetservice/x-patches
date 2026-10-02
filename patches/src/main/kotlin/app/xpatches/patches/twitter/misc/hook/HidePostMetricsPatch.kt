/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.hook

@Suppress("unused")
val hidePostMetricsPatch = hookPatch(
    name = "Hide post metrics",
    description = "Hides the reply, repost, like and bookmark counts of posts.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/metrics/HidePostMetricsHook;",
    default = false,
)

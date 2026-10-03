/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.layout.poll

import app.xpatches.patches.twitter.misc.hook.hookPatch

@Suppress("unused")
val showPollResultsPatch = hookPatch(
    name = "Show poll results",
    description = "Shows the results of polls without voting. Polls are shown as final while the setting is on, so voting is not possible.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/poll/ShowPollResultsHook;",
)

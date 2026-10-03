/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.hook

@Suppress("unused")
val hideCommunityNotesPatch = hookPatch(
    name = "Hide Community Notes",
    description = "Hides the Community Notes attached to posts. Off by default in the X Patches settings.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/notes/HideCommunityNotesHook;",
)

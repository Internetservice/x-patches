/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.patches.twitter.misc.links

import app.xpatches.patches.twitter.misc.hook.hookPatch

@Suppress("unused")
val noShortenedUrlPatch = hookPatch(
    name = "No shortened URL",
    description = "Opens and copies the real link of a post instead of the t.co short link.",
    hookClassDescriptor = "Lapp/xpatches/extension/twitter/patches/hook/patch/links/NoShortenedUrlHook;",
)

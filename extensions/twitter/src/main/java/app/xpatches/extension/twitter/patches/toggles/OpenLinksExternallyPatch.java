/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.toggles;

import app.xpatches.extension.twitter.settings.Settings;

@SuppressWarnings("unused")
public final class OpenLinksExternallyPatch {
    private OpenLinksExternallyPatch() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * Injection point.
     */
    public static boolean openLinksExternally() {
        return Settings.OPEN_LINKS_EXTERNALLY.get();
    }
}

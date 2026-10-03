/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.toggles;

import app.xpatches.extension.twitter.settings.Settings;

@SuppressWarnings("unused")
public final class DebugMenuPatch {
    private DebugMenuPatch() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * Injection point. Called with the flag the drawer shows its Debug Menu entry on.
     */
    public static boolean showDebugMenu(boolean original) {
        return original || Settings.SHOW_DEBUG_MENU.get();
    }
}

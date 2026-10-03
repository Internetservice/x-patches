/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.toggles;

import app.xpatches.extension.twitter.settings.Settings;

@SuppressWarnings("unused")
public final class AutoAdvancePatch {
    private AutoAdvancePatch() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * Injection point. Called with the stored auto advance flag of the video settings.
     */
    public static boolean isAutoAdvanceEnabled(boolean original) {
        return original && Settings.AUTO_ADVANCE_VIDEOS.get();
    }
}

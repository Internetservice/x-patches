/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.promote;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Removes the quick promote eligibility so the "Promote" button is not shown on your own posts.
 */
public final class HidePromoteButtonHook extends BaseJsonHook {
    public static final HidePromoteButtonHook INSTANCE = new HidePromoteButtonHook();

    private HidePromoteButtonHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_PROMOTE_BUTTON.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            if (object.has("quick_promote_eligibility") && !object.isNull("quick_promote_eligibility")) {
                JsonParser.put(object, "quick_promote_eligibility", JSONObject.NULL);
            }
        });
    }
}

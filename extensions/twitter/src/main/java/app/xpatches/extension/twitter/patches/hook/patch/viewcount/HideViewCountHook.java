/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.viewcount;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Removes the view counts of posts and videos from the responses.
 */
public final class HideViewCountHook extends BaseJsonHook {
    public static final HideViewCountHook INSTANCE = new HideViewCountHook();

    private HideViewCountHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_VIEW_COUNT.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            if (object.has("views") && object.optJSONObject("views") != null) {
                JsonParser.put(object, "views", JSONObject.NULL);
            }
            if (object.has("view_count") && !object.isNull("view_count")) {
                JsonParser.put(object, "view_count", JSONObject.NULL);
            }
        });
    }
}

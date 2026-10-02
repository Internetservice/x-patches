/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.social;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Removes the social context lines above posts, such as "X follows", "Liked by" and "You might like".
 */
public final class HideSocialContextHook extends BaseJsonHook {
    public static final HideSocialContextHook INSTANCE = new HideSocialContextHook();

    private HideSocialContextHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_SOCIAL_CONTEXT.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            if (object.has("social_context") && !object.isNull("social_context")) {
                JsonParser.put(object, "social_context", JSONObject.NULL);
            }
        });
    }
}

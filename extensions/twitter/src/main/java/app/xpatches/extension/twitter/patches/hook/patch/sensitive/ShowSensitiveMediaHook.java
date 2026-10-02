/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.sensitive;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Removes the sensitive media warnings and interstitials so media is shown directly.
 */
public final class ShowSensitiveMediaHook extends BaseJsonHook {
    public static final ShowSensitiveMediaHook INSTANCE = new ShowSensitiveMediaHook();

    private static final String[] NULLED_KEYS = {
            "sensitive_media_warning",
            "media_visibility_results",
            "tweet_interstitial",
            "profile_interstitial_type",
    };

    private ShowSensitiveMediaHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.SHOW_SENSITIVE_MEDIA.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            if (object.has("possibly_sensitive")) {
                JsonParser.put(object, "possibly_sensitive", false);
            }
            for (String key : NULLED_KEYS) {
                if (object.has(key) && !object.isNull(key)) {
                    JsonParser.put(object, key, JSONObject.NULL);
                }
            }
        });
    }
}

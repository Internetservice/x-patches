/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.metrics;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Zeroes the engagement counts of posts so the action bar shows the icons without numbers.
 */
public final class HidePostMetricsHook extends BaseJsonHook {
    public static final HidePostMetricsHook INSTANCE = new HidePostMetricsHook();

    private static final String[] COUNT_KEYS = {
            "bookmark_count", "favorite_count", "quote_count", "reply_count", "retweet_count",
    };

    private HidePostMetricsHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_POST_METRICS.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            JSONObject counts = object.optJSONObject("counts");
            if (counts == null) return;
            for (String key : COUNT_KEYS) {
                if (counts.has(key)) JsonParser.put(counts, key, 0);
            }
        });
    }
}

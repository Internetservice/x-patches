/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.links;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Replaces the t.co short link of every link entity by the real link, so opening, copying and
 * sharing a link uses the real one. The post text itself is left alone, as its entity
 * positions depend on it.
 */
public final class NoShortenedUrlHook extends BaseJsonHook {
    public static final NoShortenedUrlHook INSTANCE = new NoShortenedUrlHook();

    private NoShortenedUrlHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.NO_SHORTENED_URL.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            String url = object.optString("url", null);
            String expanded = object.optString("expanded_url", null);
            if (url == null || expanded == null || expanded.isEmpty()) return;
            if (url.startsWith("https://t.co/") || url.startsWith("http://t.co/")) {
                JsonParser.put(object, "url", expanded);
            }
        });
    }
}

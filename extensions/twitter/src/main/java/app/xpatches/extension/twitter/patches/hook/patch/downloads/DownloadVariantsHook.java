/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.downloads;

import org.json.JSONArray;
import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.downloads.UnlockDownloadsPatch;
import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Remembers every quality of the videos in the responses, so the quality picker can offer
 * them. The media models of X only keep the quality they intend to download.
 */
public final class DownloadVariantsHook extends BaseJsonHook {
    public static final DownloadVariantsHook INSTANCE = new DownloadVariantsHook();

    private DownloadVariantsHook() {
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.DOWNLOAD_QUALITY_PICKER.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            JSONArray variants = object.optJSONArray("variants");
            if (variants != null) UnlockDownloadsPatch.rememberJsonVariants(variants);
        });
    }
}

/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.video;

import org.json.JSONArray;
import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.downloads.UnlockDownloadsPatch;
import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Keeps only the highest bitrate MP4 variant of every video, so the player cannot
 * pick a lower quality through adaptive streaming.
 */
public final class ForceHdVideoHook extends BaseJsonHook {
    public static final ForceHdVideoHook INSTANCE = new ForceHdVideoHook();

    private ForceHdVideoHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.FORCE_HD_VIDEO.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            JSONArray variants = object.optJSONArray("variants");
            if (variants == null || variants.length() < 2) return;
            // The other qualities are dropped below, keep them for the download quality picker.
            UnlockDownloadsPatch.rememberJsonVariants(variants);

            JSONObject best = null;
            long bestBitrate = -1;
            for (int i = 0; i < variants.length(); i++) {
                JSONObject variant = variants.optJSONObject(i);
                if (variant == null || !variant.has("bit_rate") || variant.isNull("bit_rate")) continue;
                long bitrate = variant.optLong("bit_rate", -1);
                if (bitrate > bestBitrate) {
                    bestBitrate = bitrate;
                    best = variant;
                }
            }
            if (best == null) return;

            JSONArray replacement = new JSONArray();
            replacement.put(best);
            JsonParser.put(object, "variants", replacement);
        });
    }
}

/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.downloads;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Marks every media as downloadable, so the download action is offered on videos
 * whose author disallowed downloads.
 */
public final class UnlockDownloadsHook extends BaseJsonHook {
    public static final UnlockDownloadsHook INSTANCE = new UnlockDownloadsHook();

    private UnlockDownloadsHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.UNLOCK_DOWNLOADS.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            if (!object.has("allow_download_status")) return;
            JSONObject status = object.optJSONObject("allow_download_status");
            if (status == null) {
                status = new JSONObject();
                JsonParser.put(object, "allow_download_status", status);
            }
            JsonParser.put(status, "allow_download", true);
        });
    }
}

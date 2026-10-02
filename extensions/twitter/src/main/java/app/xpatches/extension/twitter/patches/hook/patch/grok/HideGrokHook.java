/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.grok;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Removes the Grok buttons and attachments from posts.
 */
public final class HideGrokHook extends BaseJsonHook {
    public static final HideGrokHook INSTANCE = new HideGrokHook();

    private HideGrokHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_GROK.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            if (object.has("grok_analysis_button")) {
                JsonParser.put(object, "grok_analysis_button", false);
            }
            if (object.has("grok_share_attachment") && !object.isNull("grok_share_attachment")) {
                JsonParser.put(object, "grok_share_attachment", JSONObject.NULL);
            }
        });
    }
}

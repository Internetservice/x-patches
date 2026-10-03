/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.poll;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Marks the counts of every poll as final, which makes X show the results instead of the
 * voting buttons.
 */
public final class ShowPollResultsHook extends BaseJsonHook {
    public static final ShowPollResultsHook INSTANCE = new ShowPollResultsHook();

    private ShowPollResultsHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.SHOW_POLL_RESULTS.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            // Card binding values: {"key": "counts_are_final", "value": {"boolean_value": false, ...}}.
            if ("counts_are_final".equals(object.optString("key"))) {
                JSONObject value = object.optJSONObject("value");
                if (value != null && value.has("boolean_value")) {
                    JsonParser.put(value, "boolean_value", true);
                }
            }
            if (object.has("counts_are_final") && !object.isNull("counts_are_final")) {
                JsonParser.put(object, "counts_are_final", true);
            }
        });
    }
}

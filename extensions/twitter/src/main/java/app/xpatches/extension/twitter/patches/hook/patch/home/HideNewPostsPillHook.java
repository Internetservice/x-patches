/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.home;

import org.json.JSONArray;
import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Drops the "show alert" timeline instructions, which carry the "New posts" pill.
 */
public final class HideNewPostsPillHook extends BaseJsonHook {
    public static final HideNewPostsPillHook INSTANCE = new HideNewPostsPillHook();

    private HideNewPostsPillHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_NEW_POSTS_PILL.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            JSONArray instructions = object.optJSONArray("instructions");
            if (instructions == null) return;
            for (int i = instructions.length() - 1; i >= 0; i--) {
                JSONObject instruction = instructions.optJSONObject(i);
                if (instruction == null) continue;
                String type = instruction.optString("type", instruction.optString("__typename", ""));
                if (type.equals("TimelineShowAlert")) instructions.remove(i);
            }
        });
    }
}

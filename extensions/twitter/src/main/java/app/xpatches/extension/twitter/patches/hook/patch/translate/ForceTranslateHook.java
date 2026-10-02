/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.translate;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;

/**
 * Marks every post as translatable so the translate action is always offered.
 */
public final class ForceTranslateHook extends BaseJsonHook {
    public static final ForceTranslateHook INSTANCE = new ForceTranslateHook();

    private ForceTranslateHook() {
    }

    @Override
    public void apply(JSONObject json) {
        JsonParser.INSTANCE.forEachObject(json, object -> {
            if (object.has("is_translatable")) JsonParser.put(object, "is_translatable", true);
        });
    }
}

/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.translate;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Marks every post as translatable so the translate action is always offered.
 */
public final class ForceTranslateHook extends BaseJsonHook {
    public static final ForceTranslateHook INSTANCE = new ForceTranslateHook();

    private ForceTranslateHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.FORCE_TRANSLATE.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            if (object.has("is_translatable")) JsonParser.put(object, "is_translatable", true);
        });
    }
}

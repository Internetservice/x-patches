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
        boolean force = Settings.FORCE_TRANSLATE.get();
        boolean auto = Settings.AUTO_TRANSLATE.get();
        if (!force && !auto) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            if (force) {
                if (object.has("is_translatable")) JsonParser.put(object, "is_translatable", true);
                if (object.has("is_community_note_translatable")) JsonParser.put(object, "is_community_note_translatable", true);
            }
            if (auto) {
                if (object.has("is_auto_translate_candidate")) JsonParser.put(object, "is_auto_translate_candidate", true);
                if (object.has("user_auto_translate_language_enabled")) JsonParser.put(object, "user_auto_translate_language_enabled", true);
            }
        });
    }
}

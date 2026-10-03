/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.immersive;

import org.json.JSONArray;
import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonHookPatch;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Strips the immersive viewer timelines down to the opened video, which X pins first.
 */
public final class HideImmersiveFeedHook extends BaseJsonHook {
    public static final HideImmersiveFeedHook INSTANCE = new HideImmersiveFeedHook();

    private HideImmersiveFeedHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * Injection point. Called with the page count lambda of the vertical pager of the player.
     *
     * @return A lambda reporting a single page while the setting is on.
     */
    public static Object limitPageCount(Object original) {
        return new PageCount(original);
    }

    private static final class PageCount implements kotlin.jvm.functions.Function0<Object> {
        private final Object original;

        PageCount(Object original) {
            this.original = original;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object invoke() {
            if (Settings.HIDE_IMMERSIVE_PLAYER.get()) return 1;
            return ((kotlin.jvm.functions.Function0<Object>) original).invoke();
        }
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_IMMERSIVE_PLAYER.get()) return;
        String operation = JsonHookPatch.currentOperation();
        if (operation == null || !operation.startsWith("ImmersiveViewer")) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            JSONArray entries = object.optJSONArray("entries");
            if (entries == null || !object.has("type") && !object.has("__typename")) return;

            boolean kept = false;
            for (int i = entries.length() - 1; i >= 0; i--) {
                JSONObject entry = entries.optJSONObject(i);
                String id = entry == null ? "" : JsonParser.INSTANCE.entryId(entry);
                boolean isVideo = entry != null && !id.startsWith("cursor-");
                // Iterated backwards, so the first video entry is the one kept.
                if (isVideo && isFirstVideo(entries, i)) {
                    kept = true;
                    continue;
                }
                entries.remove(i);
            }
            if (!kept) return;
        });
    }

    private static boolean isFirstVideo(JSONArray entries, int index) {
        for (int i = 0; i < index; i++) {
            JSONObject entry = entries.optJSONObject(i);
            if (entry != null && !JsonParser.INSTANCE.entryId(entry).startsWith("cursor-")) return false;
        }
        return true;
    }
}

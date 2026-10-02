/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.json;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import app.xpatches.extension.twitter.Utils;

public final class JsonParser {
    public static final JsonParser INSTANCE = new JsonParser();

    private JsonParser() {
    }

    public void hidePromotedMetadata(JSONObject json) {
        handleTimeline(json, entry -> removeMatchingItems(entry, this::matchesPromotedEntryId));
    }

    public void hideRecommendedUsers(JSONObject json) {
        handleTimeline(json, entry -> removeMatchingItems(entry, this::matchesWhoToFollowEntryId));
    }

    private void handleTimeline(JSONObject json, Predicate<JSONObject> removePredicate) {
        JSONArray instructions = findJSONArray(json, "instructions");
        if (instructions == null) return;

        forEach(instructions, instruction -> {
            JSONArray entries = instruction.optJSONArray("entries");
            if (entries == null) return;

            List<Integer> entryRemoveIndex = new ArrayList<>();
            forEachIndexed(entries, (entryIndex, entry) -> {
                if (removePredicate.test(entry)) {
                    Log.d(Utils.LOG_TAG, "Removing timeline entry " + entryIndex + " " + entry.optString("entryId"));
                    entryRemoveIndex.add(entryIndex);
                }
            });
            Collections.reverse(entryRemoveIndex);
            entryRemoveIndex.forEach(entries::remove);
        });
    }

    private boolean removeMatchingItems(JSONObject entry, Predicate<JSONObject> matcher) {
        if (matcher.test(entry)) {
            return true;
        }

        JSONArray items = findJSONArray(entry, "items");
        if (items == null) {
            return false;
        }

        List<Integer> itemRemoveIndex = new ArrayList<>();
        forEachIndexed(items, (itemIndex, item) -> {
            if (matcher.test(item)) {
                itemRemoveIndex.add(itemIndex);
            }
        });
        Collections.reverse(itemRemoveIndex);
        itemRemoveIndex.forEach(items::remove);

        return false;
    }

    private boolean matchesPromotedEntryId(JSONObject json) {
        String entryId = json.optString("entryId", json.optString("entry_id"));
        return entryId.contains("promoted-tweet");
    }

    private boolean matchesWhoToFollowEntryId(JSONObject json) {
        String entryId = json.optString("entryId", json.optString("entry_id"));
        return entryId.startsWith("whoToFollow-")
                || entryId.startsWith("who-to-follow-")
                || entryId.startsWith("connect-module-")
                || entryId.startsWith("who-to-subscribe-");
    }

    /**
     * Searches for a JSONArray with the given key in the given JSONObject.
     */
    private JSONArray findJSONArray(JSONObject json, String targetKey) {
        if (json == null) return null;

        JSONArray array = json.optJSONArray(targetKey);
        if (array != null) return array;

        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            Object child = json.opt(keys.next());
            if (child instanceof JSONObject) {
                JSONArray result = findJSONArray((JSONObject) child, targetKey);
                if (result != null) return result;
            }
        }

        return null;
    }

    /**
     * Iterates over a JSONArray and performs the given action for each JSONObject element.
     */
    private void forEach(JSONArray jsonArray, Consumer<JSONObject> action) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.opt(i);
            if (element instanceof JSONObject) {
                action.accept((JSONObject) element);
            }
        }
    }

    /**
     * Iterates over a JSONArray and performs the given action for each JSONObject element,
     * providing the index of the element.
     */
    private void forEachIndexed(JSONArray jsonArray, BiConsumer<Integer, JSONObject> action) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.opt(i);
            if (element instanceof JSONObject) {
                action.accept(i, (JSONObject) element);
            }
        }
    }
}

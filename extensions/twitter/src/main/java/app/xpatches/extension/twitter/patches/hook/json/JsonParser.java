/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Entry filters based on piko (GPLv3) - https://github.com/crimera/piko
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

/**
 * Helpers to edit the GraphQL responses of X. Timelines arrive as
 * {@code instructions[].entries[]} where each entry (or module item) carries an
 * {@code entry_id}, and posts are nested objects with snake_case keys.
 */
public final class JsonParser {
    public static final JsonParser INSTANCE = new JsonParser();

    private JsonParser() {
    }

    /**
     * Sets a value, swallowing the checked exception org.json throws for invalid values.
     */
    public static void put(JSONObject object, String key, Object value) {
        try {
            object.put(key, value);
        } catch (Exception e) {
            Log.w(Utils.LOG_TAG, "Failed to set " + key, e);
        }
    }

    /**
     * @return The entry id of a timeline entry or module item, or an empty string.
     */
    public static String entryId(JSONObject entry) {
        return entry.optString("entryId", entry.optString("entry_id", ""));
    }

    /**
     * @return The first segment of an entry id, such as "promoted" for "promoted-tweet-123".
     */
    public static String entryKind(String entryId) {
        int dash = entryId.indexOf('-');
        return dash < 0 ? entryId : entryId.substring(0, dash);
    }

    /**
     * Removes the timeline entries and module items that satisfy the predicate.
     */
    public void removeTimelineEntries(JSONObject json, Predicate<JSONObject> remove) {
        JSONArray instructions = findJSONArray(json, "instructions");
        if (instructions == null) return;

        forEach(instructions, instruction -> {
            JSONArray entries = instruction.optJSONArray("entries");
            if (entries == null) return;

            List<Integer> entryRemoveIndex = new ArrayList<>();
            forEachIndexed(entries, (entryIndex, entry) -> {
                if (removeMatchingItems(entry, remove)) {
                    Log.d(Utils.LOG_TAG, "Removing timeline entry " + entryId(entry));
                    entryRemoveIndex.add(entryIndex);
                }
            });
            Collections.reverse(entryRemoveIndex);
            entryRemoveIndex.forEach(entries::remove);
        });
    }

    /**
     * @return If the entry itself must be removed. Matching items inside a module entry are
     * removed from the module, which is kept.
     */
    private boolean removeMatchingItems(JSONObject entry, Predicate<JSONObject> remove) {
        if (remove.test(entry)) {
            return true;
        }

        JSONArray items = findJSONArray(entry, "items");
        if (items == null) {
            return false;
        }

        List<Integer> itemRemoveIndex = new ArrayList<>();
        forEachIndexed(items, (itemIndex, item) -> {
            if (remove.test(item)) {
                Log.d(Utils.LOG_TAG, "Removing module item " + entryId(item));
                itemRemoveIndex.add(itemIndex);
            }
        });
        Collections.reverse(itemRemoveIndex);
        itemRemoveIndex.forEach(items::remove);

        return false;
    }

    /**
     * Visits every JSON object below (and including) the given one.
     */
    public void forEachObject(Object node, Consumer<JSONObject> visitor) {
        if (node instanceof JSONObject) {
            JSONObject object = (JSONObject) node;
            visitor.accept(object);
            // Keys may be modified by the visitor, iterate over a snapshot.
            List<String> keys = new ArrayList<>();
            Iterator<String> iterator = object.keys();
            while (iterator.hasNext()) keys.add(iterator.next());
            for (String key : keys) {
                forEachObject(object.opt(key), visitor);
            }
        } else if (node instanceof JSONArray) {
            JSONArray array = (JSONArray) node;
            for (int i = 0; i < array.length(); i++) {
                forEachObject(array.opt(i), visitor);
            }
        }
    }

    /**
     * @return If a key with a non null value exists anywhere below the given object.
     */
    public boolean containsKey(Object node, String key) {
        if (node instanceof JSONObject) {
            JSONObject object = (JSONObject) node;
            if (object.has(key) && !object.isNull(key)) return true;
            Iterator<String> iterator = object.keys();
            while (iterator.hasNext()) {
                if (containsKey(object.opt(iterator.next()), key)) return true;
            }
        } else if (node instanceof JSONArray) {
            JSONArray array = (JSONArray) node;
            for (int i = 0; i < array.length(); i++) {
                if (containsKey(array.opt(i), key)) return true;
            }
        }
        return false;
    }

    /**
     * Searches for a JSONArray with the given key in the given JSONObject.
     */
    public JSONArray findJSONArray(JSONObject json, String targetKey) {
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

    private void forEach(JSONArray jsonArray, Consumer<JSONObject> action) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.opt(i);
            if (element instanceof JSONObject) {
                action.accept((JSONObject) element);
            }
        }
    }

    private void forEachIndexed(JSONArray jsonArray, BiConsumer<Integer, JSONObject> action) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = jsonArray.opt(i);
            if (element instanceof JSONObject) {
                action.accept(i, (JSONObject) element);
            }
        }
    }
}

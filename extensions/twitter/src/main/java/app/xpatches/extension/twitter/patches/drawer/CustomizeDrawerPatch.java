/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.drawer;

import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import app.xpatches.extension.twitter.settings.Settings;

/**
 * Hides entries of the navigation drawer. The entries are known by their titles, which are
 * collected as the drawer shows them, so the settings can offer them.
 */
@SuppressWarnings("unused")
public final class CustomizeDrawerPatch {
    public static final String KEY_SEEN = "drawer_items_seen";
    public static final String KEY_HIDDEN = "drawer_items_hidden";
    /**
     * The entry of the patches themselves, which is never offered for hiding.
     */
    public static final String OWN_ENTRY = "X Patches";

    private static volatile Set<String> hidden;
    private static final Set<String> seen = Collections.synchronizedSet(new HashSet<>());

    private CustomizeDrawerPatch() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * Injection point. Called for every entry the drawer is about to show.
     */
    public static boolean isHidden(String title) {
        try {
            if (title == null || OWN_ENTRY.equals(title)) return false;
            remember(title);
            return hidden().contains(title);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void remember(String title) {
        if (!seen.add(title)) return;
        SharedPreferences preferences = Settings.preferences();
        if (preferences == null) return;
        Set<String> stored = new TreeSet<>(preferences.getStringSet(KEY_SEEN, Collections.emptySet()));
        if (stored.add(title)) {
            preferences.edit().putStringSet(KEY_SEEN, stored).apply();
        }
    }

    private static Set<String> hidden() {
        Set<String> current = hidden;
        if (current == null) {
            SharedPreferences preferences = Settings.preferences();
            current = preferences == null
                    ? Collections.emptySet()
                    : new HashSet<>(preferences.getStringSet(KEY_HIDDEN, Collections.emptySet()));
            hidden = current;
        }
        return current;
    }

    /**
     * @return The titles the drawer has shown so far, sorted.
     */
    public static Set<String> seenTitles() {
        SharedPreferences preferences = Settings.preferences();
        Set<String> titles = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (preferences != null) titles.addAll(preferences.getStringSet(KEY_SEEN, Collections.emptySet()));
        titles.addAll(seen);
        titles.remove(OWN_ENTRY);
        return titles;
    }

    public static Set<String> hiddenTitles() {
        return new HashSet<>(hidden());
    }

    public static void setHiddenTitles(Set<String> titles) {
        hidden = new HashSet<>(titles);
        SharedPreferences preferences = Settings.preferences();
        if (preferences != null) preferences.edit().putStringSet(KEY_HIDDEN, new HashSet<>(titles)).apply();
    }
}

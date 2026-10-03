/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.video;

import android.content.SharedPreferences;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import app.xpatches.extension.twitter.XLog;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Remembers the playback speed and replaces the speed levels of the video player.
 * <p>
 * The levels are an enum of X. Its static initializer is patched to rebuild the enum values
 * from {@link #customSpeedCount(Object[])}, {@link #customSpeedName(int)} and
 * {@link #customSpeedValue(int)}, and the places looking a level up by name go through
 * {@link #speedName(String)} and {@link #speedNamed(String)}, so names X stored before the
 * levels changed still resolve.
 */
@SuppressWarnings("unused")
public final class VideoSpeedPatch {
    private static final float[] ORIGINAL_SPEEDS = {0.5f, 1f, 1.25f, 1.5f, 2f, 2.5f, 3f};

    /**
     * The enum constants X ships, by name, and their speeds.
     */
    private static final Map<String, Object> originalsByName = new LinkedHashMap<>();
    private static final Map<String, Float> originalSpeeds = new LinkedHashMap<>();
    private static Field speedField;

    private static List<Float> customSpeeds = new ArrayList<>();
    private static List<String> customNames = new ArrayList<>();
    private static Object[] customValues;

    private VideoSpeedPatch() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * Injection point. Called with the stored "locked speed" flag of the video settings.
     */
    public static boolean isSpeedLocked(boolean original) {
        return original || Settings.REMEMBER_VIDEO_SPEED.get();
    }

    // region Speed levels

    /**
     * Injection point. Called from the static initializer of the speed enum with the values
     * X ships. Returns how many custom levels replace them, or 0 to keep them.
     */
    public static int customSpeedCount(Object[] originals) {
        try {
            originalsByName.clear();
            originalSpeeds.clear();
            speedField = findSpeedField(originals);
            if (speedField == null) {
                XLog.w("Could not find the speed value of " + originals.getClass().getComponentType());
                return 0;
            }
            for (Object original : originals) {
                String name = ((Enum<?>) original).name();
                originalsByName.put(name, original);
                originalSpeeds.put(name, speedField.getFloat(original));
            }

            List<Float> speeds = parseSpeeds(speedsText());
            if (speeds.isEmpty() || speeds.equals(speedsOf(originals))) return 0;

            customSpeeds = speeds;
            customNames = new ArrayList<>();
            for (float speed : speeds) customNames.add(nameFor(speed));
            XLog.i("Video speed levels: " + customNames);
            return speeds.size();
        } catch (Exception e) {
            XLog.e("Could not prepare the video speed levels", e);
            return 0;
        }
    }

    /**
     * Injection point.
     */
    public static int customSpeedCount() {
        return customSpeeds.size();
    }

    /**
     * Injection point.
     */
    public static String customSpeedName(int index) {
        return customNames.get(index);
    }

    /**
     * Injection point.
     */
    public static float customSpeedValue(int index) {
        return customSpeeds.get(index);
    }

    /**
     * Injection point. Called with the array the custom levels are collected in.
     */
    public static void setSpeeds(Object[] values) {
        customValues = values;
    }

    /**
     * Injection point.
     */
    public static Object[] speeds() {
        return customValues;
    }

    /**
     * Injection point. Called with the name a level is looked up by, such as the stored speed.
     *
     * @return A name that exists among the current levels.
     */
    public static String speedName(String name) {
        try {
            if (customValues == null || name == null || customNames.contains(name)) return name;
            Float speed = originalSpeeds.get(name);
            String replacement = speed == null ? null : nameOfSpeed(speed);
            if (replacement == null) replacement = nameOfSpeed(1f);
            XLog.i("Video speed " + name + " is not a level any more, using " + replacement);
            return replacement == null ? name : replacement;
        } catch (Exception e) {
            return name;
        }
    }

    /**
     * Injection point. Called where X uses one of its own levels directly, such as the default.
     *
     * @return The current level with the same speed, or the original level.
     */
    public static Object speedNamed(String name) {
        Object original = originalsByName.get(name);
        try {
            if (customValues == null) return original;
            Float speed = originalSpeeds.get(name);
            if (speed == null) return original;
            for (Object value : customValues) {
                if (value != null && speedField.getFloat(value) == speed) return value;
            }
            // The speed is no longer a level, use the normal speed.
            for (Object value : customValues) {
                if (value != null && speedField.getFloat(value) == 1f) return value;
            }
        } catch (Exception e) {
            XLog.e("Could not resolve the video speed " + name, e);
        }
        return original;
    }

    private static String nameOfSpeed(float speed) {
        for (int i = 0; i < customSpeeds.size(); i++) {
            if (customSpeeds.get(i) == speed) return customNames.get(i);
        }
        return null;
    }

    private static Field findSpeedField(Object[] originals) {
        if (originals.length == 0) return null;
        for (Field field : originals[0].getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.getType() != float.class) continue;
            field.setAccessible(true);
            return field;
        }
        return null;
    }

    private static List<Float> speedsOf(Object[] values) throws IllegalAccessException {
        List<Float> speeds = new ArrayList<>();
        for (Object value : values) speeds.add(speedField.getFloat(value));
        return speeds;
    }

    /**
     * @return The enum name of a speed, following the naming of X: X_5, X1, X1_25, X2.
     */
    private static String nameFor(float speed) {
        for (Map.Entry<String, Float> original : originalSpeeds.entrySet()) {
            if (original.getValue() == speed) return original.getKey();
        }
        String text = formatSpeed(speed).replace('.', '_');
        return "X" + text;
    }

    // endregion

    // region Settings

    /**
     * @return The speeds the user configured, one per line, or the levels of X.
     */
    public static String speedsText() {
        SharedPreferences preferences = Settings.preferences();
        String stored = preferences == null ? null : preferences.getString(Settings.KEY_VIDEO_SPEEDS, null);
        if (stored != null && !stored.trim().isEmpty()) return stored;

        StringBuilder text = new StringBuilder();
        for (float speed : ORIGINAL_SPEEDS) {
            if (text.length() > 0) text.append('\n');
            text.append(formatSpeed(speed));
        }
        return text.toString();
    }

    public static String speedsSummary() {
        List<Float> speeds = parseSpeeds(speedsText());
        if (speeds.isEmpty()) return "Tap to set the speeds of the video player";
        StringBuilder summary = new StringBuilder();
        for (float speed : speeds) {
            if (summary.length() > 0) summary.append(", ");
            summary.append(formatSpeed(speed)).append('×');
        }
        return summary.toString();
    }

    /**
     * @return The valid speeds in the text, sorted and without duplicates, always including 1×.
     */
    static List<Float> parseSpeeds(String text) {
        TreeSet<Float> speeds = new TreeSet<>();
        if (text != null) {
            for (String line : text.split("[\\n,;]")) {
                String trimmed = line.trim().replace(',', '.').replace("x", "").replace("×", "");
                if (trimmed.isEmpty()) continue;
                try {
                    float speed = Float.parseFloat(trimmed);
                    if (speed >= 0.1f && speed <= 10f) speeds.add(speed);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (speeds.isEmpty()) return new ArrayList<>();
        speeds.add(1f);
        return new ArrayList<>(speeds);
    }

    private static String formatSpeed(float speed) {
        String text = String.format(Locale.US, "%.2f", speed);
        text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        return text;
    }

    // endregion
}

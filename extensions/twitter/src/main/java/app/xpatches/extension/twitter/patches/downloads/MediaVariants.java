/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.downloads;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the video variants out of the media models of X. The models are obfuscated, so the
 * variants are found by shape: the list field whose elements carry a URL, a bit rate and a
 * content type.
 */
final class MediaVariants {
    private static final Pattern RESOLUTION = Pattern.compile("/(\\d{2,4})x(\\d{2,4})/");

    private MediaVariants() {
    }

    static final class Variant implements Comparable<Variant> {
        final String url;
        final int bitRate;
        final String contentType;
        final int width;
        final int height;

        Variant(String url, Integer bitRate, String contentType) {
            this.url = url;
            this.bitRate = bitRate == null ? 0 : bitRate;
            this.contentType = contentType == null ? "" : contentType;

            Matcher matcher = RESOLUTION.matcher(url);
            if (matcher.find()) {
                width = Integer.parseInt(matcher.group(1));
                height = Integer.parseInt(matcher.group(2));
            } else {
                width = 0;
                height = 0;
            }
        }

        boolean isStream() {
            return contentType.contains("mpegURL") || url.contains(".m3u8");
        }

        String label() {
            StringBuilder label = new StringBuilder();
            if (width > 0) {
                label.append(width).append(" × ").append(height);
            } else {
                label.append("Original");
            }
            if (bitRate > 0) {
                label.append("  ·  ").append(String.format(Locale.US, "%.1f Mbit/s", bitRate / 1_000_000f));
            }
            return label.toString();
        }

        org.json.JSONObject toJson() {
            org.json.JSONObject object = new org.json.JSONObject();
            try {
                object.put("url", url);
                if (bitRate > 0) object.put("bit_rate", bitRate);
                object.put("content_type", contentType);
            } catch (org.json.JSONException ignored) {
            }
            return object;
        }

        @Override
        public int compareTo(Variant other) {
            int byPixels = Integer.compare(other.width * other.height, width * height);
            return byPixels != 0 ? byPixels : Integer.compare(other.bitRate, bitRate);
        }
    }

    /**
     * @return The downloadable variants of the media, best first, or an empty list.
     */
    static List<Variant> of(Object media) {
        if (media == null) return Collections.emptyList();

        for (Class<?> type = media.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                Object value;
                try {
                    field.setAccessible(true);
                    value = field.get(media);
                } catch (Exception ignored) {
                    continue;
                }
                if (!(value instanceof Iterable)) continue;

                List<Variant> variants = fromIterable((Iterable<?>) value);
                if (!variants.isEmpty()) return variants;
            }
        }
        return Collections.emptyList();
    }

    /**
     * @return The downloadable variants in the "variants" array of a response, best first.
     */
    static List<Variant> fromJson(org.json.JSONArray array) {
        List<Variant> variants = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (int i = 0; i < array.length(); i++) {
            org.json.JSONObject object = array.optJSONObject(i);
            if (object == null) continue;
            String url = object.optString("url", null);
            if (url == null || !url.startsWith("http")) continue;

            Integer bitRate = object.has("bit_rate") && !object.isNull("bit_rate") ? object.optInt("bit_rate") : null;
            Variant variant = new Variant(url, bitRate, object.optString("content_type", ""));
            if (variant.isStream() || !seen.add(variant.url)) continue;
            variants.add(variant);
        }

        Collections.sort(variants);
        return variants;
    }

    /**
     * @return The downloadable variants in the list, best first, or an empty list if it is not a variant list.
     */
    static List<Variant> fromIterable(Iterable<?> elements) {
        List<Variant> variants = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Object element : elements) {
            Variant variant = fromElement(element);
            // Not a variant list at all.
            if (variant == null) return Collections.emptyList();
            if (variant.isStream() || !seen.add(variant.url)) continue;
            variants.add(variant);
        }

        Collections.sort(variants);
        return variants;
    }

    private static Variant fromElement(Object element) {
        if (element == null) return null;

        String url = null;
        Integer bitRate = null;
        String contentType = null;

        for (Field field : element.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            Object value;
            try {
                field.setAccessible(true);
                value = field.get(element);
            } catch (Exception ignored) {
                continue;
            }
            if (value instanceof String) {
                String string = (String) value;
                if (string.startsWith("http")) {
                    url = string;
                } else {
                    contentType = string;
                }
            } else if (value instanceof Integer) {
                bitRate = (Integer) value;
            }
        }

        return url == null ? null : new Variant(url, bitRate, contentType);
    }
}

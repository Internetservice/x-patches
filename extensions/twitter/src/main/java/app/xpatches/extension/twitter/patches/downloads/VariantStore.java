/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.downloads;

import android.content.Context;

import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import app.xpatches.extension.twitter.Utils;
import app.xpatches.extension.twitter.XLog;

/**
 * The qualities of the videos seen in the responses, by the URL of each quality. Kept on disk
 * as well, because X restores its timelines from its own cache after a restart, without the
 * responses passing by again.
 */
final class VariantStore {
    private static final String FILE_NAME = "xpatches-variants.json";
    private static final int MAX_VIDEOS = 400;

    private static final Object LOCK = new Object();
    /**
     * Every quality list, keyed by each of its URLs, least recently used first.
     */
    private static final LinkedHashMap<String, List<MediaVariants.Variant>> BY_URL = new LinkedHashMap<>(256, 0.75f, true);
    private static final ScheduledExecutorService WRITER = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> pendingWrite;
    private static boolean loaded;

    private VariantStore() {
    }

    static List<MediaVariants.Variant> get(String url) {
        synchronized (LOCK) {
            load();
            return BY_URL.get(url);
        }
    }

    static void put(List<MediaVariants.Variant> variants) {
        if (variants.size() < 2) return;
        synchronized (LOCK) {
            load();
            boolean changed = false;
            for (MediaVariants.Variant variant : variants) {
                // A list from the response is never replaced by the shorter one of the model.
                List<MediaVariants.Variant> known = BY_URL.get(variant.url);
                if (known == null || known.size() < variants.size()) {
                    BY_URL.put(variant.url, variants);
                    changed = true;
                }
            }
            if (!changed) return;

            trim();
            scheduleWrite();
        }
    }

    private static void trim() {
        if (videos().size() <= MAX_VIDEOS) return;
        Iterator<Map.Entry<String, List<MediaVariants.Variant>>> iterator = BY_URL.entrySet().iterator();
        List<MediaVariants.Variant> eldest = iterator.next().getValue();
        BY_URL.values().removeIf(list -> list == eldest);
    }

    private static Collection<List<MediaVariants.Variant>> videos() {
        List<List<MediaVariants.Variant>> videos = new ArrayList<>();
        for (List<MediaVariants.Variant> list : BY_URL.values()) {
            if (!videos.contains(list)) videos.add(list);
        }
        return videos;
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        try {
            File file = file();
            if (file == null || !file.exists()) return;
            JSONArray videos = new JSONArray(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            for (int i = 0; i < videos.length(); i++) {
                JSONArray video = videos.optJSONArray(i);
                if (video == null) continue;
                List<MediaVariants.Variant> variants = MediaVariants.fromJson(video);
                for (MediaVariants.Variant variant : variants) {
                    BY_URL.put(variant.url, variants);
                }
            }
            XLog.i("Loaded the qualities of " + videos.length() + " videos");
        } catch (Exception e) {
            XLog.e("Could not load the video qualities", e);
        }
    }

    private static void scheduleWrite() {
        if (pendingWrite != null) pendingWrite.cancel(false);
        pendingWrite = WRITER.schedule(VariantStore::write, 3, TimeUnit.SECONDS);
    }

    private static void write() {
        try {
            File file = file();
            if (file == null) return;

            JSONArray videos = new JSONArray();
            synchronized (LOCK) {
                for (List<MediaVariants.Variant> list : videos()) {
                    JSONArray video = new JSONArray();
                    for (MediaVariants.Variant variant : list) {
                        video.put(variant.toJson());
                    }
                    videos.put(video);
                }
            }

            File temporary = new File(file.getPath() + ".tmp");
            try (FileWriter writer = new FileWriter(temporary)) {
                writer.write(videos.toString());
            }
            if (!temporary.renameTo(file)) {
                XLog.w("Could not replace the video qualities file");
            }
        } catch (Exception e) {
            XLog.e("Could not save the video qualities", e);
        }
    }

    private static File file() {
        Context context = Utils.getContext();
        return context == null ? null : new File(context.getFilesDir(), FILE_NAME);
    }
}

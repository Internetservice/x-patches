/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.downloads;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.xpatches.extension.twitter.Utils;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Unlocks the download actions and offers the quality picker before a video is downloaded.
 * The chosen variant is downloaded by X itself, so it lands in the gallery like any other download.
 */
@SuppressWarnings("unused")
public final class UnlockDownloadsPatch {
    /**
     * The variants of the videos about to be downloaded, by the URL of each variant.
     */
    private static final Map<String, List<MediaVariants.Variant>> RECENT_VARIANTS =
            new LinkedHashMap<String, List<MediaVariants.Variant>>(32, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, List<MediaVariants.Variant>> eldest) {
                    return size() > 64;
                }
            };

    /**
     * Set while the chosen variant is handed back to the downloader, so it is not intercepted again.
     */
    private static final ThreadLocal<Boolean> CHOSEN = new ThreadLocal<>();

    private UnlockDownloadsPatch() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * Injection point. Called when the media models are built.
     *
     * @param original The flag the author of the post set.
     * @return If the download actions are offered on the media.
     */
    public static boolean isDownloadable(boolean original) {
        return original || Settings.UNLOCK_DOWNLOADS.get();
    }

    /**
     * Injection point. Called with the variants of a video right before one of them is downloaded.
     */
    public static void rememberVariants(Object variants) {
        try {
            if (!(variants instanceof Iterable)) return;
            List<MediaVariants.Variant> list = MediaVariants.fromIterable((Iterable<?>) variants);
            if (list.size() < 2) return;
            synchronized (RECENT_VARIANTS) {
                for (MediaVariants.Variant variant : list) {
                    RECENT_VARIANTS.put(variant.url, list);
                }
            }
        } catch (Exception e) {
            Log.e(Utils.LOG_TAG, "Failed to read the video variants", e);
        }
    }

    /**
     * Injection point. Called with the URL about to be downloaded by the media downloader.
     *
     * @param downloader The downloader instance, used to start the download of the chosen variant.
     * @return If the picker took over, so the downloader must not start.
     */
    public static boolean interceptDownload(Object downloader, String url, String fileName, Object listener, Object network) {
        try {
            if (Boolean.TRUE.equals(CHOSEN.get())) return false;
            if (!Settings.DOWNLOAD_QUALITY_PICKER.get() || url == null) return false;

            List<MediaVariants.Variant> variants;
            synchronized (RECENT_VARIANTS) {
                variants = RECENT_VARIANTS.get(url);
            }
            // Not a video with several qualities, for example a photo.
            if (variants == null) return false;

            Activity activity = Utils.getCurrentActivity();
            if (activity == null) {
                Log.w(Utils.LOG_TAG, "No activity to show the quality picker");
                return false;
            }

            Method download = findDownloadMethod(downloader, listener, network);
            if (download == null) {
                Log.w(Utils.LOG_TAG, "Could not find the download method of " + downloader.getClass().getName());
                return false;
            }

            Runnable show = () -> showPicker(activity, variants, variant -> {
                try {
                    CHOSEN.set(true);
                    download.invoke(downloader, variant.url, fileName, listener, network);
                } catch (Exception e) {
                    Log.e(Utils.LOG_TAG, "Failed to start the download", e);
                    Toast.makeText(activity, "Download failed", Toast.LENGTH_SHORT).show();
                } finally {
                    CHOSEN.set(false);
                }
            });
            if (Looper.myLooper() == Looper.getMainLooper()) {
                show.run();
            } else {
                activity.runOnUiThread(show);
            }
            return true;
        } catch (Exception e) {
            Log.e(Utils.LOG_TAG, "Failed to show the quality picker", e);
            return false;
        }
    }

    /**
     * @return The hooked download method of the downloader, found by its shape as its name is obfuscated.
     */
    private static Method findDownloadMethod(Object downloader, Object listener, Object network) {
        for (Method method : downloader.getClass().getDeclaredMethods()) {
            Class<?>[] types = method.getParameterTypes();
            if (method.getReturnType() != void.class || types.length != 4) continue;
            if (types[0] != String.class || types[1] != String.class) continue;
            if (listener != null && !types[2].isInstance(listener)) continue;
            if (network != null && !types[3].isInstance(network)) continue;
            method.setAccessible(true);
            return method;
        }
        return null;
    }

    private interface OnVariantChosen {
        void onChosen(MediaVariants.Variant variant);
    }

    private static void showPicker(Activity activity, List<MediaVariants.Variant> variants, OnVariantChosen onChosen) {
        boolean night = (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        int theme = night
                ? android.R.style.Theme_DeviceDefault_Dialog_Alert
                : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;

        CharSequence[] labels = new CharSequence[variants.size()];
        for (int i = 0; i < variants.size(); i++) {
            labels[i] = variants.get(i).label();
        }

        new AlertDialog.Builder(activity, theme)
                .setTitle("Download video")
                .setItems(labels, (dialog, which) -> onChosen.onChosen(variants.get(which)))
                .setNeutralButton("Copy link", (dialog, which) -> copyLink(activity, variants.get(0)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static void copyLink(Context context, MediaVariants.Variant variant) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Video link", variant.url));
        Toast.makeText(context, "Video link copied", Toast.LENGTH_SHORT).show();
    }
}

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
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;

import app.xpatches.extension.twitter.Utils;
import app.xpatches.extension.twitter.XLog;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Unlocks the download actions and offers the quality picker before a video is downloaded.
 * The chosen variant is downloaded by X itself, so it lands in the gallery like any other download.
 */
@SuppressWarnings("unused")
public final class UnlockDownloadsPatch {
    /**
     * Set while the chosen variant is handed back to the downloader, so it is not intercepted again.
     */
    private static final ThreadLocal<Boolean> CHOSEN = new ThreadLocal<>();

    /**
     * Until when the next in-app notification is held back, see {@link #suppressNotification()}.
     */
    private static volatile long suppressNotificationUntil;

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
     * Called by the response hooks with the "variants" array of a video. The responses carry every
     * quality, while the media models of X only keep the one they intend to download.
     */
    public static void rememberJsonVariants(org.json.JSONArray variants) {
        try {
            remember(MediaVariants.fromJson(variants));
        } catch (Exception e) {
            XLog.e("Failed to read the response variants", e);
        }
    }

    /**
     * Injection point. Called with the variants of a video right before one of them is downloaded.
     */
    public static void rememberVariants(Object variants) {
        try {
            if (!(variants instanceof Iterable)) return;
            List<MediaVariants.Variant> list = MediaVariants.fromIterable((Iterable<?>) variants);
            remember(list);

            // A download is about to start. X shows its "Download started" banner before the
            // downloader is reached, which the picker holds back until a quality is chosen.
            if (Settings.DOWNLOAD_QUALITY_PICKER.get() && !list.isEmpty() && knownVariants(list.get(0).url) != null) {
                suppressNotificationUntil = SystemClock.uptimeMillis() + 3000;
            }
        } catch (Exception e) {
            XLog.e("Failed to read the video variants", e);
        }
    }

    /**
     * Injection point. Called when X is about to show an in-app notification banner.
     *
     * @return If the banner must not be shown.
     */
    public static boolean suppressNotification() {
        if (SystemClock.uptimeMillis() > suppressNotificationUntil) return false;
        suppressNotificationUntil = 0;
        XLog.i("Holding back the download banner for the quality picker");
        return true;
    }

    private static List<MediaVariants.Variant> knownVariants(String url) {
        return VariantStore.get(url);
    }

    private static void remember(List<MediaVariants.Variant> list) {
        VariantStore.put(list);
    }

    /**
     * Injection point. Called with the URL about to be downloaded by the media downloader,
     * which watermarks the video before saving it.
     *
     * @return If the picker took over, so the downloader must not start.
     */
    public static boolean interceptDownload(Object downloader, String url, String mimeType, Object listener, Object network) {
        return intercept(downloader, url, new Object[]{mimeType, listener, network});
    }

    /**
     * Injection point. Called with the URL about to be handed to the Android download manager,
     * which every other download path ends in.
     *
     * @return If the picker took over, so the downloader must not start.
     */
    public static boolean interceptDownload(Object downloader, String url, String mimeType, Object headers,
                                            String title, String description, Object listener, boolean flag) {
        return intercept(downloader, url, new Object[]{mimeType, headers, title, description, listener, flag});
    }

    /**
     * @param downloader The downloader instance, used to start the download of the chosen variant.
     * @param arguments The arguments of the hooked download method after the URL.
     */
    private static boolean intercept(Object downloader, String url, Object[] arguments) {
        try {
            XLog.i("Download requested by " + downloader.getClass().getName() + " of " + url
                    + " with " + java.util.Arrays.toString(arguments) + " on " + Thread.currentThread().getName());
            if (Boolean.TRUE.equals(CHOSEN.get())) {
                XLog.i("Downloading the chosen variant");
                return false;
            }
            if (!Settings.DOWNLOAD_QUALITY_PICKER.get() || url == null) {
                XLog.i("Quality picker is off");
                return false;
            }

            List<MediaVariants.Variant> variants = knownVariants(url);
            // Not a video with several qualities, for example a photo or a processed file.
            if (variants == null) {
                XLog.i("No variants known for the URL, letting the download through");
                return false;
            }

            Activity activity = Utils.getCurrentActivity();
            if (activity == null) {
                XLog.w("No activity to show the quality picker");
                return false;
            }

            Method download = findDownloadMethod(downloader, arguments);
            if (download == null) {
                XLog.w("Could not find the download method of " + downloader.getClass().getName());
                return false;
            }
            XLog.i("Showing the quality picker with " + variants.size() + " variants");

            Runnable show = () -> showPicker(activity, variants, variant -> {
                try {
                    CHOSEN.set(true);
                    Object[] chosenArguments = new Object[arguments.length + 1];
                    chosenArguments[0] = variant.url;
                    System.arraycopy(arguments, 0, chosenArguments, 1, arguments.length);
                    download.invoke(downloader, chosenArguments);
                    Toast.makeText(activity, "Downloading " + variant.label(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    XLog.e("Failed to start the download", e);
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
            XLog.e("Failed to show the quality picker", e);
            return false;
        }
    }

    /**
     * @return The hooked download method of the downloader, found by its shape as its name is obfuscated.
     */
    private static Method findDownloadMethod(Object downloader, Object[] arguments) {
        for (Method method : downloader.getClass().getDeclaredMethods()) {
            Class<?>[] types = method.getParameterTypes();
            if (method.getReturnType() != void.class || types.length != arguments.length + 1) continue;
            if (types[0] != String.class) continue;

            boolean matches = true;
            for (int i = 0; i < arguments.length && matches; i++) {
                Object argument = arguments[i];
                Class<?> type = types[i + 1];
                if (argument instanceof Boolean) {
                    matches = type == boolean.class || type == Boolean.class;
                } else if (argument != null) {
                    matches = type.isInstance(argument);
                } else {
                    matches = !type.isPrimitive();
                }
            }
            if (!matches) continue;

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
                .setPositiveButton("Open with", (dialog, which) -> openWith(activity, variants.get(0)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Hands the best quality to another app, such as an external downloader.
     */
    private static void openWith(Context context, MediaVariants.Variant variant) {
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(android.net.Uri.parse(variant.url), "video/mp4");
            context.startActivity(android.content.Intent.createChooser(intent, "Open video with"));
        } catch (Exception e) {
            XLog.e("Could not open the video with another app", e);
            Toast.makeText(context, "No app can open the video", Toast.LENGTH_SHORT).show();
        }
    }

    private static void copyLink(Context context, MediaVariants.Variant variant) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Video link", variant.url));
        Toast.makeText(context, "Video link copied", Toast.LENGTH_SHORT).show();
    }
}

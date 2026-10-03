/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.downloads;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import app.xpatches.extension.twitter.Utils;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Unlocks the download actions and offers the quality picker before a video is downloaded.
 */
@SuppressWarnings("unused")
public final class UnlockDownloadsPatch {
    private static final String DOWNLOAD_FOLDER = "X";

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
     * Injection point. Called with the media model when a download is requested.
     *
     * @return If the picker took over the download, so X must not start its own.
     */
    public static boolean showQualityPicker(Object media) {
        try {
            if (!Settings.DOWNLOAD_QUALITY_PICKER.get()) return false;

            List<MediaVariants.Variant> variants = MediaVariants.of(media);
            if (variants.isEmpty()) {
                Log.i(Utils.LOG_TAG, "No video variants found on " + media.getClass().getName());
                return false;
            }

            Activity activity = Utils.getCurrentActivity();
            if (activity == null) {
                Log.w(Utils.LOG_TAG, "No activity to show the quality picker");
                return false;
            }

            if (Looper.myLooper() == Looper.getMainLooper()) {
                showPicker(activity, variants);
            } else {
                activity.runOnUiThread(() -> showPicker(activity, variants));
            }
            return true;
        } catch (Exception e) {
            Log.e(Utils.LOG_TAG, "Failed to show the quality picker", e);
            return false;
        }
    }

    private static void showPicker(Activity activity, List<MediaVariants.Variant> variants) {
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
                .setItems(labels, (dialog, which) -> download(activity, variants.get(which)))
                .setNeutralButton("Copy link", (dialog, which) -> copyLink(activity, variants.get(0)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static void download(Context context, MediaVariants.Variant variant) {
        try {
            String fileName = variant.fileName();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(variant.url))
                    .setTitle(fileName)
                    .setMimeType(variant.mimeType())
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, DOWNLOAD_FOLDER + "/" + fileName);

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
            Toast.makeText(context, "Downloading " + variant.label(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(Utils.LOG_TAG, "Failed to start the download", e);
            Toast.makeText(context, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void copyLink(Context context, MediaVariants.Variant variant) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Video link", variant.url));
        Toast.makeText(context, "Video link copied", Toast.LENGTH_SHORT).show();
    }
}

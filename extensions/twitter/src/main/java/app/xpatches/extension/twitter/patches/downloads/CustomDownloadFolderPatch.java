/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.downloads;

import android.content.SharedPreferences;

import app.xpatches.extension.twitter.settings.Settings;

/**
 * Redirects downloads to the folder chosen in the settings, given relative to the storage
 * root, such as "Download/X" or "Movies/X".
 */
@SuppressWarnings("unused")
public final class CustomDownloadFolderPatch {
    public static final String DEFAULT_FOLDER = "Download/X";

    private CustomDownloadFolderPatch() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * @return The chosen folder, cleaned up, or the default.
     */
    public static String folder() {
        SharedPreferences preferences = Settings.preferences();
        String folder = preferences == null ? null : preferences.getString(Settings.KEY_DOWNLOAD_FOLDER, null);
        if (folder == null) return DEFAULT_FOLDER;
        folder = folder.trim().replace('\\', '/').replaceAll("^/+|/+$", "");
        return folder.isEmpty() ? DEFAULT_FOLDER : folder;
    }

    /**
     * Injection point. Called with the public directory X saves to, "Download".
     */
    public static String downloadDirectory(String original) {
        String folder = folder();
        int slash = folder.indexOf('/');
        return slash < 0 ? folder : folder.substring(0, slash);
    }

    /**
     * Injection point. Called with the path inside the directory, "X/" followed by the file name.
     */
    public static String downloadSubPath(String original) {
        String fileName = original == null ? "" : original.substring(original.lastIndexOf('/') + 1);
        String folder = folder();
        int slash = folder.indexOf('/');
        String subFolder = slash < 0 ? "" : folder.substring(slash + 1);
        return subFolder.isEmpty() ? fileName : subFolder + "/" + fileName;
    }
}

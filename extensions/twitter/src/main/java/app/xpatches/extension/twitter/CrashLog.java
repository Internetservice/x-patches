/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Keeps the stack trace of the last crash of X, so it can be read from the settings
 * without a computer attached.
 */
public final class CrashLog {
    private static final String FILE_NAME = "xpatches-crash.txt";
    private static volatile boolean installed;

    private CrashLog() {
    }

    static synchronized void install(Context context) {
        if (installed) return;
        installed = true;

        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                write(context, thread, throwable);
            } catch (Throwable ignored) {
            }
            if (previous != null) previous.uncaughtException(thread, throwable);
        });
    }

    private static void write(Context context, Thread thread, Throwable throwable) throws Exception {
        StringWriter trace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(trace));

        String report = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())
                + "\nX Patches " + Utils.getPatchesVersion()
                + "\nAndroid " + android.os.Build.VERSION.RELEASE + ", " + android.os.Build.MODEL
                + "\nThread " + thread.getName() + "\n\n" + trace;

        try (FileWriter writer = new FileWriter(file(context))) {
            writer.write(report);
        }
    }

    /**
     * @return The last crash report, or null if none was recorded.
     */
    public static String read(Context context) {
        try {
            File file = file(context);
            if (!file.exists()) return null;
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.w(Utils.LOG_TAG, "Could not read the crash log", e);
            return null;
        }
    }

    public static void clear(Context context) {
        //noinspection ResultOfMethodCallIgnored
        file(context).delete();
    }

    private static File file(Context context) {
        return new File(context.getFilesDir(), FILE_NAME);
    }
}

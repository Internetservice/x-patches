/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;

/**
 * Logs to logcat and keeps the last lines in memory, so they can be read from the settings
 * without a computer attached.
 */
public final class XLog {
    private static final int CAPACITY = 300;
    private static final ArrayDeque<String> LINES = new ArrayDeque<>(CAPACITY);
    private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    private XLog() {
    }

    public static void i(String message) {
        Log.i(Utils.LOG_TAG, message);
        append("I", message);
    }

    public static void w(String message) {
        Log.w(Utils.LOG_TAG, message);
        append("W", message);
    }

    public static void e(String message, Throwable throwable) {
        Log.e(Utils.LOG_TAG, message, throwable);
        append("E", message + ": " + throwable);
    }

    private static void append(String level, String message) {
        synchronized (LINES) {
            if (LINES.size() >= CAPACITY) LINES.pollFirst();
            LINES.addLast(TIME.format(new Date()) + " " + level + " " + message);
        }
    }

    /**
     * @return The recorded lines, oldest first.
     */
    public static String dump() {
        synchronized (LINES) {
            return String.join("\n", LINES);
        }
    }

    public static void clear() {
        synchronized (LINES) {
            LINES.clear();
        }
    }
}

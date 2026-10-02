/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.utils.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class StreamUtils {
    private StreamUtils() {
    }

    /**
     * Reads an InputStream fully. Unlike InputStream#readAllBytes this is available below Android 13.
     *
     * @param inputStream The input stream to read.
     * @return The bytes of the stream.
     * @throws IOException If an I/O error occurs.
     */
    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toByteArray();
        }
    }

    /**
     * Reads an InputStream into a String.
     *
     * @param inputStream The input stream to read.
     * @return The string content of the stream.
     * @throws IOException If an I/O error occurs.
     */
    public static String toString(InputStream inputStream) throws IOException {
        return new String(readAllBytes(inputStream), StandardCharsets.UTF_8);
    }

    /**
     * Converts a String into an InputStream.
     *
     * @param string The string to convert.
     * @return An InputStream containing the string bytes.
     */
    public static InputStream fromString(String string) {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }
}

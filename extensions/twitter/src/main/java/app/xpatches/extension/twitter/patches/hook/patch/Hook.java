/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch;

public interface Hook<T> {
    /**
     * Hook implementation.
     *
     * @param data The data to hook.
     * @return The hooked data.
     */
    T hook(T data);
}

/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.media;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import app.xpatches.extension.twitter.Utils;
import app.xpatches.extension.twitter.XLog;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Closes the media viewer and the video tab on a right swipe, by sending them the event their
 * own close button sends. The viewers are known from the events passing through their handlers.
 */
@SuppressWarnings("unused")
public final class SwipeToCloseMediaPatch {
    private static WeakReference<Object> viewer = new WeakReference<>(null);
    private static Object closeEvent;
    private static int currentIndex;
    private static boolean closeOnAnyPage;

    private SwipeToCloseMediaPatch() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    // Class names of the viewer events. Modified during patching.

    public static String mediaChangedEvent() {
        return "";
    }

    public static String mediaCloseEvent() {
        return "";
    }

    public static String pageChangedEvent() {
        return "";
    }

    public static String videoCloseEvent() {
        return "";
    }

    public static String immersiveEvent() {
        return "";
    }

    /**
     * Injection point. Called with every event the media viewer and the video tab handle.
     *
     * @return If the event is consumed, so the handler must ignore it.
     */
    public static boolean onViewerEvent(Object handler, Object event) {
        try {
            if (handler == null || event == null) return false;
            String eventClass = event.getClass().getName();

            if (eventClass.equals(immersiveEvent()) && Settings.HIDE_IMMERSIVE_PLAYER.get()) {
                return true;
            }

            if (eventClass.equals(mediaChangedEvent())) {
                Object media = firstFieldValue(event);
                viewer = new WeakReference<>(handler);
                closeEvent = singleton(mediaCloseEvent());
                closeOnAnyPage = false;
                currentIndex = indexOf(handler, media);
            } else if (eventClass.equals(pageChangedEvent())) {
                viewer = new WeakReference<>(handler);
                closeEvent = singleton(videoCloseEvent());
                closeOnAnyPage = true;
                Object page = firstFieldValue(event);
                currentIndex = page instanceof Integer ? (Integer) page : 0;
            } else if (eventClass.equals(mediaCloseEvent()) || eventClass.equals(videoCloseEvent())) {
                if (viewer.get() == handler) viewer = new WeakReference<>(null);
            }
        } catch (Exception e) {
            XLog.e("Could not track the media viewer", e);
        }
        return false;
    }

    /**
     * Called when the screen was swiped right.
     */
    public static void onSwipeRight() {
        try {
            if (!Settings.SWIPE_TO_CLOSE_MEDIA.get()) return;
            Object handler = viewer.get();
            if (handler == null || closeEvent == null) return;
            if (!closeOnAnyPage && currentIndex != 0) return;
            if (isDestroyed(handler)) {
                viewer = new WeakReference<>(null);
                return;
            }

            Method handle = handlerMethod(handler, closeEvent.getClass());
            if (handle == null) {
                XLog.w("Could not find the event handler of " + handler.getClass().getName());
                return;
            }
            Activity activity = Utils.getCurrentActivity();
            Runnable close = () -> {
                try {
                    handle.invoke(handler, closeEvent);
                    XLog.i("Closed the media on swipe");
                } catch (Exception e) {
                    XLog.e("Could not close the media", e);
                }
            };
            if (activity != null) activity.runOnUiThread(close); else close.run();
            viewer = new WeakReference<>(null);
        } catch (Exception e) {
            XLog.e("Could not close the media on swipe", e);
        }
    }

    private static Object firstFieldValue(Object object) throws IllegalAccessException {
        for (Field field : object.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            return field.get(object);
        }
        return null;
    }

    /**
     * @return The singleton instance of an event class without fields.
     */
    private static Object singleton(String className) throws Exception {
        Class<?> type = Class.forName(className, true, SwipeToCloseMediaPatch.class.getClassLoader());
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == type) {
                field.setAccessible(true);
                return field.get(null);
            }
        }
        throw new IllegalStateException("No instance of " + className);
    }

    /**
     * @return The position of the media in the list of the viewer state, found through the
     * state flows of the viewer, or 0 if unknown.
     */
    private static int indexOf(Object handler, Object media) {
        if (media == null) return 0;
        try {
            for (Field field : handler.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                Object value = field.get(handler);
                if (value == null) continue;
                Method getValue;
                try {
                    getValue = value.getClass().getMethod("getValue");
                } catch (NoSuchMethodException e) {
                    continue;
                }
                Object state = getValue.invoke(value);
                if (state == null) continue;
                for (Field stateField : state.getClass().getDeclaredFields()) {
                    if (Modifier.isStatic(stateField.getModifiers()) || !List.class.isAssignableFrom(stateField.getType())) continue;
                    stateField.setAccessible(true);
                    List<?> list = (List<?>) stateField.get(state);
                    if (list == null) continue;
                    int index = list.indexOf(media);
                    if (index >= 0) return index;
                }
            }
        } catch (Exception e) {
            XLog.e("Could not find the media position", e);
        }
        return 0;
    }

    /**
     * @return If the lifecycle of the component reports it destroyed.
     */
    private static boolean isDestroyed(Object handler) {
        try {
            Method getLifecycle = handler.getClass().getMethod("getLifecycle");
            Object lifecycle = getLifecycle.invoke(handler);
            if (lifecycle == null) return true;
            for (Method method : lifecycle.getClass().getMethods()) {
                if (method.getParameterCount() == 0 && Enum.class.isAssignableFrom(method.getReturnType())) {
                    Object state = method.invoke(lifecycle);
                    return state != null && "DESTROYED".equals(((Enum<?>) state).name());
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static Method handlerMethod(Object handler, Class<?> eventType) {
        for (Method method : handler.getClass().getMethods()) {
            if (method.getReturnType() == void.class && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(eventType)
                    && method.getParameterTypes()[0] != Object.class) {
                return method;
            }
        }
        return null;
    }
}

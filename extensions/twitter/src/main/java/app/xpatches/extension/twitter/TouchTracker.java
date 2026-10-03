/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import app.xpatches.extension.twitter.patches.media.SwipeToCloseMediaPatch;

/**
 * Remembers where the screen was last touched, by wrapping the window callback of every
 * activity. Used to tell on which half of a video a hold gesture started.
 */
public final class TouchTracker {
    private static volatile float lastDownX = -1;
    private static volatile int lastWidth;
    private static float downY;
    private static long downTime;
    private static int lastHeight;

    private TouchTracker() {
    }

    /**
     * @return The horizontal position of the last touch as a fraction of the window width,
     * or -1 if nothing was touched yet.
     */
    public static float lastDownFraction() {
        return lastWidth <= 0 || lastDownX < 0 ? -1 : lastDownX / lastWidth;
    }

    /**
     * A quick, mostly horizontal drag to the right that did not start on the bottom edge,
     * where seek bars live.
     */
    private static void detectSwipeRight(MotionEvent up) {
        if (lastWidth <= 0 || lastHeight <= 0) return;
        float dx = up.getX() - lastDownX;
        float dy = Math.abs(up.getY() - downY);
        long duration = up.getEventTime() - downTime;
        if (dx > lastWidth * 0.2f && dy < dx * 0.5f && duration < 600 && downY < lastHeight * 0.85f) {
            if (SwipeToCloseMediaPatch.isPatchIncluded()) SwipeToCloseMediaPatch.onSwipeRight();
        }
    }

    static void install(Activity activity) {
        try {
            Window window = activity.getWindow();
            Window.Callback callback = window.getCallback();
            if (callback == null || Proxy.isProxyClass(callback.getClass())
                    && Proxy.getInvocationHandler(callback) instanceof Handler) {
                return;
            }
            Window.Callback proxy = (Window.Callback) Proxy.newProxyInstance(
                    Window.Callback.class.getClassLoader(),
                    new Class<?>[]{Window.Callback.class},
                    new Handler(callback, window));
            window.setCallback(proxy);
        } catch (Exception e) {
            XLog.e("Could not track touches of " + activity, e);
        }
    }

    private static final class Handler implements InvocationHandler {
        private final Window.Callback original;
        private final Window window;

        Handler(Window.Callback original, Window window) {
            this.original = original;
            this.window = window;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("dispatchTouchEvent".equals(method.getName()) && args != null && args.length == 1
                    && args[0] instanceof MotionEvent) {
                MotionEvent event = (MotionEvent) args[0];
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    View decor = window.getDecorView();
                    lastWidth = decor.getWidth();
                    lastHeight = decor.getHeight();
                    lastDownX = event.getX();
                    downY = event.getY();
                    downTime = event.getEventTime();
                } else if (action == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
                    detectSwipeRight(event);
                }
            }
            try {
                return method.invoke(original, args);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}

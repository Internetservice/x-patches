/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.xpatches.extension.twitter.CrashLog;
import app.xpatches.extension.twitter.patches.drawer.CustomizeDrawerPatch;
import app.xpatches.extension.twitter.patches.video.VideoSpeedPatch;
import app.xpatches.extension.twitter.Utils;
import app.xpatches.extension.twitter.XLog;

/**
 * The settings screen, built in code so it can be hosted by the activity behind the
 * launcher shortcut and by the dialog the navigation drawer opens over X.
 */
public final class SettingsView {
    private static final String SOURCE_URL = "https://github.com/Internetservice/x-patches";

    private final Context context;
    private final boolean night;
    private final int textPrimary;
    private final int textSecondary;
    private final LinearLayout root;

    /**
     * @param context A themed context, see {@link #themedContext(Context)}.
     * @param onClose Called when the close button is pressed.
     */
    public SettingsView(Context context, Runnable onClose) {
        this.context = context;
        night = isNight(context);
        textPrimary = resolveColor(android.R.attr.textColorPrimary, night ? Color.WHITE : Color.BLACK);
        textSecondary = resolveColor(android.R.attr.textColorSecondary, night ? 0xFFB0B0B0 : 0xFF666666);
        int background = resolveColor(android.R.attr.colorBackground, night ? Color.BLACK : Color.WHITE);

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);

        Toolbar toolbar = new Toolbar(context);
        toolbar.setTitle("X Patches");
        toolbar.setTitleTextColor(textPrimary);
        toolbar.setBackgroundColor(background);
        toolbar.setNavigationIcon(new BackArrowDrawable(textPrimary, dp(2)));
        toolbar.setNavigationContentDescription("Back");
        toolbar.setNavigationOnClickListener(v -> onClose.run());
        root.addView(toolbar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, 0, 0, dp(16));
        populate(list);

        ScrollView scroll = new ScrollView(context);
        scroll.setClipToPadding(false);
        scroll.addView(list, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // The window is edge to edge: keep the header below the status bar
        // and let the list scroll under the navigation bar.
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    0);
            scroll.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
            return insets;
        });
    }

    public View getView() {
        return root;
    }

    public boolean isNight() {
        return night;
    }

    /**
     * @return A context carrying the system day or night theme, which the views resolve their colors from.
     */
    public static Context themedContext(Context base) {
        return new android.view.ContextThemeWrapper(base, theme(isNight(base)));
    }

    /**
     * @return The full screen theme matching the system day or night mode.
     */
    public static int theme(boolean night) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            return android.R.style.Theme_DeviceDefault_DayNight;
        }
        return night ? android.R.style.Theme_DeviceDefault : android.R.style.Theme_DeviceDefault_Light;
    }

    public static boolean isNight(Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    private void populate(LinearLayout list) {
        SharedPreferences preferences = context.getSharedPreferences(Settings.PREFERENCES_NAME, Context.MODE_PRIVATE);
        Map<String, LinearLayout> categories = new LinkedHashMap<>();

        for (Settings.Toggle toggle : Settings.all()) {
            if (!toggle.isAvailable()) continue;

            LinearLayout category = categories.get(toggle.category);
            if (category == null) {
                category = addCategory(list, toggle.category);
                categories.put(toggle.category, category);
            }
            addSwitch(category, toggle.title, toggle.summary,
                    preferences.getBoolean(toggle.key, toggle.defaultValue),
                    checked -> preferences.edit().putBoolean(toggle.key, checked).apply());
        }

        if (VideoSpeedPatch.isPatchIncluded()) {
            LinearLayout category = categories.get(Settings.CATEGORY_VIDEO);
            if (category == null) category = addCategory(list, Settings.CATEGORY_VIDEO);
            TextView[] summary = new TextView[1];
            summary[0] = addRow(category, "Video speed levels", VideoSpeedPatch.speedsSummary(), null,
                    v -> editSpeeds(preferences, summary[0]))[1];
        }

        if (CustomizeDrawerPatch.isPatchIncluded()) {
            LinearLayout category = categories.get(Settings.CATEGORY_APP);
            if (category == null) category = addCategory(list, Settings.CATEGORY_APP);
            TextView[] summary = new TextView[1];
            summary[0] = addRow(category, "Side bar items", drawerSummary(), null, v -> editDrawerItems(summary[0]))[1];
        }

        if (Settings.isSharingLinkPatchIncluded()) {
            LinearLayout category = addCategory(list, Settings.CATEGORY_SHARING);
            TextView[] summary = new TextView[1];
            summary[0] = addRow(category, "Sharing domain", Settings.shareDomain(), null, v -> editDomain(preferences, summary[0]))[1];
            addSwitch(category, "Include username in links", "Otherwise links use x.com/i/status/…",
                    Settings.shareUsername(),
                    checked -> preferences.edit().putBoolean(Settings.KEY_SHARE_USERNAME, checked).apply());
        }

        LinearLayout about = addCategory(list, "About");
        addRow(about, "Restart X", "Changes apply after a restart", null, v -> restart());
        addRow(about, "X Patches", "Version " + Utils.getPatchesVersion(), null, null);
        addRow(about, "Source", SOURCE_URL.replace("https://", ""), null, v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_URL));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ignored) {
            }
        });

        String crash = CrashLog.read(context);
        if (crash != null) {
            addRow(about, "Last crash", crash.substring(0, Math.min(crash.length(), 80)).replace('\n', ' ') + "…",
                    null, v -> showText("Last crash", crash, () -> CrashLog.clear(context)));
        }
        addRow(about, "Log", "What the patches did since X was started", null,
                v -> showText("Log", XLog.dump().isEmpty() ? "Nothing logged yet." : XLog.dump(), XLog::clear));
    }

    private void showText(String title, String content, Runnable onClear) {
        TextView text = new TextView(context);
        text.setText(content);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        text.setTypeface(android.graphics.Typeface.MONOSPACE);
        text.setTextIsSelectable(true);
        text.setPadding(dp(20), dp(8), dp(20), dp(8));
        ScrollView scroll = new ScrollView(context);
        scroll.addView(text);

        new AlertDialog.Builder(context, night
                ? android.R.style.Theme_DeviceDefault_Dialog_Alert
                : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                .setTitle(title)
                .setView(scroll)
                .setPositiveButton("Copy", (dialog, which) -> {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("X Patches " + title, content));
                    android.widget.Toast.makeText(context, "Copied", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Clear", (dialog, which) -> onClear.run())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void restart() {
        try {
            Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (launch == null || launch.getComponent() == null) return;
            context.startActivity(Intent.makeRestartActivityTask(launch.getComponent()));
            Runtime.getRuntime().exit(0);
        } catch (Exception e) {
            android.util.Log.e(Utils.LOG_TAG, "Could not restart X", e);
        }
    }

    private LinearLayout addCategory(LinearLayout list, String title) {
        TextView header = new TextView(context);
        header.setText(title);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        header.setTextColor(textSecondary);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(dp(16), dp(20), dp(16), dp(4));
        list.addView(header);

        LinearLayout category = new LinearLayout(context);
        category.setOrientation(LinearLayout.VERTICAL);
        list.addView(category, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return category;
    }

    private interface OnChecked {
        void onChecked(boolean checked);
    }

    private void addSwitch(LinearLayout category, String title, String summary, boolean checked, OnChecked listener) {
        Switch toggle = new Switch(context);
        toggle.setChecked(checked);
        toggle.setClickable(false);
        toggle.setFocusable(false);
        toggle.setOnCheckedChangeListener((button, isChecked) -> listener.onChecked(isChecked));
        addRow(category, title, summary, toggle, v -> toggle.toggle());
    }

    /**
     * @return The title and summary views of the row.
     */
    private TextView[] addRow(LinearLayout category, String title, String summary, View end, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        row.setMinimumHeight(dp(56));
        if (onClick != null) {
            row.setOnClickListener(onClick);
            TypedValue ripple = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, ripple, true);
            row.setBackgroundResource(ripple.resourceId);
        }

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleView.setTextColor(textPrimary);
        texts.addView(titleView);

        TextView summaryView = new TextView(context);
        summaryView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        summaryView.setTextColor(textSecondary);
        summaryView.setPadding(0, dp(2), 0, 0);
        if (summary == null) {
            summaryView.setVisibility(View.GONE);
        } else {
            summaryView.setText(summary);
        }
        texts.addView(summaryView);

        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (end != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(dp(16));
            row.addView(end, params);
        }

        category.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new TextView[]{titleView, summaryView};
    }

    private String drawerSummary() {
        int hidden = CustomizeDrawerPatch.hiddenTitles().size();
        return hidden == 0 ? "Hide entries of the side menu" : hidden + " hidden";
    }

    private void editDrawerItems(TextView summary) {
        List<String> titles = new ArrayList<>(CustomizeDrawerPatch.seenTitles());
        if (titles.isEmpty()) {
            android.widget.Toast.makeText(context, "Open the side menu once, so its entries are known", android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        Set<String> hidden = CustomizeDrawerPatch.hiddenTitles();
        boolean[] checked = new boolean[titles.size()];
        for (int i = 0; i < titles.size(); i++) checked[i] = hidden.contains(titles.get(i));

        new AlertDialog.Builder(context, night
                ? android.R.style.Theme_DeviceDefault_Dialog_Alert
                : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                .setTitle("Hide side bar items")
                .setMultiChoiceItems(titles.toArray(new CharSequence[0]), checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Set<String> selected = new java.util.HashSet<>();
                    for (int i = 0; i < titles.size(); i++) if (checked[i]) selected.add(titles.get(i));
                    CustomizeDrawerPatch.setHiddenTitles(selected);
                    summary.setText(drawerSummary());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void editSpeeds(SharedPreferences preferences, TextView summary) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setSingleLine(false);
        input.setMinLines(6);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setText(VideoSpeedPatch.speedsText());

        FrameLayout container = new FrameLayout(context);
        container.setPadding(dp(20), dp(8), dp(20), 0);
        container.addView(input, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(context, night
                ? android.R.style.Theme_DeviceDefault_Dialog_Alert
                : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                .setTitle("Video speed levels")
                .setMessage("One speed per line, for example 0.25, 0.5, 0.75, 1, 1.5, 2, 2.5, 3. Applied after restarting X.")
                .setView(container)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    preferences.edit().putString(Settings.KEY_VIDEO_SPEEDS, input.getText().toString()).apply();
                    summary.setText(VideoSpeedPatch.speedsSummary());
                })
                .setNeutralButton("Reset", (dialog, which) -> {
                    preferences.edit().remove(Settings.KEY_VIDEO_SPEEDS).apply();
                    summary.setText(VideoSpeedPatch.speedsSummary());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void editDomain(SharedPreferences preferences, TextView summary) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(Settings.shareDomain());
        input.setSelection(input.getText().length());

        FrameLayout container = new FrameLayout(context);
        container.setPadding(dp(20), dp(8), dp(20), 0);
        container.addView(input, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(context, night
                ? android.R.style.Theme_DeviceDefault_Dialog_Alert
                : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                .setTitle("Domain used in shared links")
                .setView(container)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    preferences.edit().putString(Settings.KEY_SHARE_DOMAIN, text).apply();
                    summary.setText(Settings.shareDomain());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private int resolveColor(int attribute, int fallback) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(attribute, value, true)) {
            if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                return value.data;
            }
            try {
                return context.getColor(value.resourceId);
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }

    private int dp(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * A simple back arrow, as the framework has no public one.
     */
    private static final class BackArrowDrawable extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        BackArrowDrawable(int color, int strokeWidth) {
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }

        @Override
        public void draw(Canvas canvas) {
            android.graphics.Rect bounds = getBounds();
            float size = Math.min(bounds.width(), bounds.height());
            float cx = bounds.exactCenterX();
            float cy = bounds.exactCenterY();
            float half = size * 0.3f;

            Path path = new Path();
            path.moveTo(cx + half, cy);
            path.lineTo(cx - half, cy);
            path.moveTo(cx, cy - half);
            path.lineTo(cx - half, cy);
            path.lineTo(cx, cy + half);
            canvas.drawPath(path, paint);
        }

        @Override
        public int getIntrinsicWidth() {
            return 72;
        }

        @Override
        public int getIntrinsicHeight() {
            return 72;
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}

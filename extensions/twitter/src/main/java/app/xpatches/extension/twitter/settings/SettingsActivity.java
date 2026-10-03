/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toolbar;

import java.util.LinkedHashMap;
import java.util.Map;

import app.xpatches.extension.twitter.Utils;

/**
 * The settings screen of the patches. Registered in the manifest by the Settings patch and
 * opened from the navigation drawer, the launcher shortcut or the xpatches://settings link.
 */
@SuppressWarnings("deprecation")
public final class SettingsActivity extends Activity {
    private static final int CONTENT_ID = View.generateViewId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The toolbar below replaces the framework title bar.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Utils.setContext(getApplicationContext());

        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        // The window is edge to edge, so the header and the list are laid out below the
        // status bar by hand instead of relying on the framework action bar.
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(resolveColor(android.R.attr.colorBackground, night ? Color.BLACK : Color.WHITE));

        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle("X Patches");
        toolbar.setNavigationIcon(getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
        toolbar.setNavigationContentDescription("Close");
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setBackgroundColor(resolveColor(android.R.attr.colorPrimary, night ? Color.BLACK : Color.WHITE));
        root.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics())));

        FrameLayout content = new FrameLayout(this);
        content.setId(CONTENT_ID);
        root.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    0);
            return insets;
        });

        setContentView(root);

        if (!night) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(decor.getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(CONTENT_ID, new SettingsFragment())
                    .commit();
        }
    }

    private int resolveColor(int attribute, int fallback) {
        TypedValue value = new TypedValue();
        if (getTheme().resolveAttribute(attribute, value, true)) {
            if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                return value.data;
            }
            try {
                return getColor(value.resourceId);
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }

    public static final class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(Settings.PREFERENCES_NAME);

            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
            Map<String, PreferenceCategory> categories = new LinkedHashMap<>();

            for (Settings.Toggle toggle : Settings.all()) {
                if (!toggle.isAvailable()) continue;

                PreferenceCategory category = categories.get(toggle.category);
                if (category == null) {
                    category = new PreferenceCategory(getActivity());
                    category.setTitle(toggle.category);
                    screen.addPreference(category);
                    categories.put(toggle.category, category);
                }

                SwitchPreference preference = new SwitchPreference(getActivity());
                preference.setKey(toggle.key);
                preference.setTitle(toggle.title);
                if (toggle.summary != null) preference.setSummary(toggle.summary);
                preference.setDefaultValue(toggle.defaultValue);
                category.addPreference(preference);
            }

            if (Settings.isSharingLinkPatchIncluded()) {
                PreferenceCategory category = new PreferenceCategory(getActivity());
                category.setTitle(Settings.CATEGORY_SHARING);
                screen.addPreference(category);

                EditTextPreference domain = new EditTextPreference(getActivity());
                domain.setKey(Settings.KEY_SHARE_DOMAIN);
                domain.setTitle("Sharing domain");
                domain.setDialogTitle("Domain used in shared links");
                domain.setSummary(Settings.shareDomain());
                domain.setDefaultValue(Settings.shareDomain());
                domain.setOnPreferenceChangeListener((preference, value) -> {
                    String text = value == null ? "" : value.toString().trim();
                    preference.setSummary(text.isEmpty() ? Settings.shareDomain() : text);
                    return true;
                });
                category.addPreference(domain);

                SwitchPreference username = new SwitchPreference(getActivity());
                username.setKey(Settings.KEY_SHARE_USERNAME);
                username.setTitle("Include username in links");
                username.setSummary("Otherwise links use x.com/i/status/…");
                username.setDefaultValue(Settings.shareUsername());
                category.addPreference(username);
            }

            PreferenceCategory about = new PreferenceCategory(getActivity());
            about.setTitle("About");
            screen.addPreference(about);

            Preference version = new Preference(getActivity());
            version.setTitle("X Patches");
            version.setSummary("Version " + Utils.getPatchesVersion() + ". Changes apply after restarting X.");
            version.setSelectable(false);
            about.addPreference(version);

            Preference source = new Preference(getActivity());
            source.setTitle("Source");
            source.setSummary("github.com/Internetservice/x-patches");
            source.setOnPreferenceClickListener(preference -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Internetservice/x-patches")));
                } catch (Exception ignored) {
                }
                return true;
            });
            about.addPreference(source);

            setPreferenceScreen(screen);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // Let the list scroll under the navigation bar instead of being cut off by it.
            View view = getView();
            ListView list = view == null ? null : view.findViewById(android.R.id.list);
            if (list != null) {
                list.setClipToPadding(false);
                list.setOnApplyWindowInsetsListener((v, insets) -> {
                    v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.getSystemWindowInsetBottom());
                    return insets;
                });
            }
        }
    }
}

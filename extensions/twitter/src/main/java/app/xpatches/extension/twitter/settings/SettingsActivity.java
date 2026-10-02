/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.settings;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Map;

import app.xpatches.extension.twitter.Utils;

/**
 * The settings screen of the patches. Registered in the manifest by the Settings patch and
 * opened from the launcher shortcut or the xpatches://settings link.
 */
@SuppressWarnings("deprecation")
public final class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setContext(this);
        setTitle("X Patches");
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);

        // The DeviceDefault theme keeps light status bar icons on the light theme.
        boolean night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        if (!night) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(decor.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
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
                preference.setDefaultValue(true);
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
                    startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://github.com/Internetservice/x-patches")));
                } catch (Exception ignored) {
                }
                return true;
            });
            about.addPreference(source);

            setPreferenceScreen(screen);
        }
    }
}

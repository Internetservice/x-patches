/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import app.xpatches.extension.twitter.Utils;

/**
 * The settings screen behind the launcher shortcut and the xpatches://settings link.
 * Registered in the manifest by the Settings patch. The navigation drawer opens the same
 * screen as a dialog over X instead, see {@link SettingsDialog}.
 */
public final class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Utils.setContext(getApplicationContext());

        SettingsView settings = new SettingsView(this, this::finish);
        setContentView(settings.getView());
        SettingsDialog.styleSystemBars(getWindow(), settings.isNight());
    }
}

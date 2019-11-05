package com.xuvjso.nfmovies.Fragment;


import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.xuvjso.nfmovies.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragmentCompat{

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference about = getPreferenceScreen().findPreference("about");
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                //AlertDialog dialog = builder.setMessage("Made by @xuvjso\n\n奈菲影视 nfmovies.com\n\n团长资源 b.apkgm.top\n\n低端影视 ddrk.me").create();
                AlertDialog dialog = builder.setMessage(R.string.about_string).create();
                dialog.show();
                return true;
            }
        });

    }

}

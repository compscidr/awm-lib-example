package io.rightmesh.awm_lib_example;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class Preferences extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}

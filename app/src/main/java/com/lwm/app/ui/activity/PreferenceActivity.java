package com.lwm.app.ui.activity;

import android.os.Bundle;

import com.lwm.app.R;

import net.saik0.android.unifiedpreference.UnifiedPreferenceActivity;
import net.saik0.android.unifiedpreference.UnifiedPreferenceFragment;

public class PreferenceActivity extends UnifiedPreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setHeaderRes(R.xml.pref_headers);
        super.onCreate(savedInstanceState);
    }

    public static class AccessPointSettingsFragment extends UnifiedPreferenceFragment {}
    public static class AppSettingsFragment extends UnifiedPreferenceFragment {}
}

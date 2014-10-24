/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.katkiss;
import org.meerkats.katkiss.KKC;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.katkiss.Utils;

import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;

import android.provider.Settings;
import android.content.ContentResolver;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.os.Bundle;
import android.os.UserHandle;
import android.app.Dialog;
import android.app.Activity;

import java.util.ArrayList;

public class DockSettingsFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "DockSettingsFragment";
    private static final String KEY_TOUCHPAD = "touchpad";


    private ListPreference _touchpadModeList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.kk_dock_settings);

        _touchpadModeList = (ListPreference) findPreference(KEY_TOUCHPAD);

        refreshState();

        _touchpadModeList.setOnPreferenceChangeListener(this);
    }


    private void refreshState() {
        int valInt;
        int touchpadMode = getTouchpadModeSetting(1);
        _touchpadModeList.setValue(String.valueOf(touchpadMode));
        _touchpadModeList.setSummary("Touchpad mode set to " + _touchpadModeList.getEntries()[touchpadMode]);
    }
    
    @Override
    public void onResume() {
        super.onResume();

        refreshState();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        return null;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
	if(key == null) return true;

        if (key.equals(KEY_TOUCHPAD)) 
        {
            putTouchpadModeSetting(Integer.parseInt((String)objValue));
        }
        return true;
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
/*        if (preference == mFontSizePref) {
            if (Utils.hasMultipleUsers(getActivity())) {
                showDialog(DLG_GLOBAL_CHANGE_WARNING);
                return true;
            } else {
                mFontSizePref.click();
            }
        }
*/
        return false;
    }

    private int getTouchpadModeSetting(int defaultValue) {
        int result = defaultValue;
        try { result = Settings.System.getInt(getContentResolver(), KKC.S.DEVICE_SETTINGS_TOUCHPAD_MODE); } 
        catch (Exception e) { }
        return result;
    }

    private void putTouchpadModeSetting(int newMode)
    {
        android.provider.Settings.System.putInt(getContentResolver(), KKC.S.DEVICE_SETTINGS_TOUCHPAD_MODE, newMode);
        _touchpadModeList.setValue(String.valueOf(newMode));
        _touchpadModeList.setSummary("Touchpad mode set to " + _touchpadModeList.getEntries()[newMode]);
    }
}

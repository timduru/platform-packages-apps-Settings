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

package com.android.settings.eos;

import org.teameos.jellybean.settings.EOSConstants;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.IWindowManager;

public class SystemSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String EOS_DEVICE_SETTINGS = "eos_device_settings";
    private static final String EOS_PERFORMANCE_SETTINGS = "eos_performance_settings";

    private boolean hasDeviceSettings;
    private CheckBoxPreference mVolumeKeysSwitch;
    private ListPreference mWifiChannelsPreference;
    private ListPreference mDefaultVolumeStreamPreference;

    private Context mContext;
    private ContentResolver mContentResolver;
    private IWindowManager wm;
    private boolean mHasNavBar = false;
    private boolean mHasSystemBar = false;
    private boolean mHasOnlyStatBar = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IWindowManager wm = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        try {
            mHasNavBar = wm.hasNavigationBar();
            mHasSystemBar = wm.hasSystemNavBar();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // use this to define crespo, just for now
        if (!mHasNavBar && !mHasSystemBar) mHasOnlyStatBar = true;

        mContext = getActivity();
        mContentResolver = mContext.getContentResolver();

        addPreferencesFromResource(R.xml.eos_system_settings);

        hasDeviceSettings = this.getResources().getBoolean(
                R.bool.config_hasDeviceSettings);
        PreferenceScreen root = getPreferenceScreen();

        if (!hasDeviceSettings) {
            Preference ps = (Preference) findPreference(EOS_DEVICE_SETTINGS);
            if (ps != null) root.removePreference(ps);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mVolumeKeysSwitch.equals(preference)) {
            Settings.System.putInt(mContext.getContentResolver(),
                    EOSConstants.SYSTEM_VOLUME_KEYS_SWITCH_ON_ROTATION,
                    ((Boolean) newValue).booleanValue() ? 1 : 0);
            return true;
        } else if (mWifiChannelsPreference.equals(preference)) {
            final String newCountryCode = (String) newValue;
            String currentCountryCode = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Global.WIFI_COUNTRY_CODE);
            if (newCountryCode.equals(currentCountryCode)) {
                return false;
            } else {
                handleWifiState(newCountryCode);
                return true;
            }
        } else if (mDefaultVolumeStreamPreference.equals(preference)) {
            Settings.System.putString(mContext.getContentResolver(),
                    EOSConstants.SYSTEM_DEFAULT_VOLUME_STREAM,
                    (String) newValue);
            return true;
        }
        return false;
    }

    private void handleWifiState(String countryCode) {
        Settings.Secure.putString(mContext.getContentResolver(), Settings.Global.WIFI_COUNTRY_CODE,
                countryCode);
        mWifiChannelsPreference.setValue(String.valueOf(countryCode));
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()) {
            int delay = 5000;
            final ProgressDialog dialog = ProgressDialog.show(mContext,
                    getString(R.string.eos_wifi_regulatory_domain_changed),
                    getString(R.string.eos_wifi_restarting), true);
            dialog.show();
            wifi.setWifiEnabled(false);
            wifi.setWifiEnabled(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            }, delay);
        }
    }
}

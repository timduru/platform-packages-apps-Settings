/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.android.settings.R;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.util.AttributeSet;

import android.app.ProgressDialog;
import android.provider.Settings;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.preference.ListPreference;
import android.os.Handler;




public class WifiRegulatoryListPreference extends ListPreference implements Preference.OnPreferenceChangeListener
{
    final static String TAG = "WifiRegulatoryListPreference";
    protected Context mContext;
    protected ContentResolver mResolver;
    protected Resources mRes;

    public WifiRegulatoryListPreference(Context context)
    { super(context); init(); }

    public WifiRegulatoryListPreference(Context context, AttributeSet attrs)
    { super(context, attrs); init(); }

    private void init()
    {
       mContext = getContext();  
       mResolver = mContext.getContentResolver();
       mRes = mContext.getResources();
       setOnPreferenceChangeListener(this);
    }

    protected void onSetInitialValue (boolean restoreValue, Object defaultValue)
    {
        setValue(Settings.Secure.getString(mResolver, Settings.Global.WIFI_COUNTRY_CODE));
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) 
    {
        final String newCountryCode = (String) newValue;
        String currentCountryCode = Settings.Secure.getString(mResolver,
        Settings.Global.WIFI_COUNTRY_CODE);

        if (newCountryCode.equals(currentCountryCode)) return false;
        else handleWifiState(newCountryCode);
        return true;
    }

    private void handleWifiState(String countryCode) 
    {
        Settings.Secure.putString(mResolver, Settings.Global.WIFI_COUNTRY_CODE, countryCode);
        setValue(String.valueOf(countryCode));
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        if (wifi.isWifiEnabled()) 
            restartWifi(wifi);
    }

    private void restartWifi(WifiManager wifi)
    {
        int delay = 5000;
        final ProgressDialog dialog = ProgressDialog.show(mContext,
                mRes.getString(R.string.kk_wifi_regulatory_domain_changed),
                mRes.getString(R.string.kk_wifi_restarting), true);
        dialog.show();
        wifi.setWifiEnabled(false);
        wifi.setWifiEnabled(true);
        new Handler().postDelayed(
            new Runnable() 
            {
                @Override
                public void run() { dialog.dismiss(); }
            }, delay);
    }


}


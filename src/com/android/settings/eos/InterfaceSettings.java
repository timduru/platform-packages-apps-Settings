
package com.android.settings.eos;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.teameos.jellybean.settings.EOSConstants;

import com.android.internal.telephony.RILConstants;
import com.android.settings.R;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.IWindowManager;

import com.android.settings.SettingsPreferenceFragment;

public class InterfaceSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private Context mContext;

    private CheckBoxPreference mRecentsKillallButtonPreference;
    private CheckBoxPreference mRecentsMemDisplayPreference;
    private CheckBoxPreference mLowProfileNavBar;
    private CheckBoxPreference mEosTogglesEnabled;
    private CheckBoxPreference mHideIndicator;
    private ColorPreference mIndicatorColor;
    private Preference mIndicatorDefaultColor;
    private EosMultiSelectListPreference mEosQuickSettingsView;
    private boolean mEosSettingsEnabled = false;
    private boolean mHasNavBar = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mContext = getActivity();
        IWindowManager mWindowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        try {
            mHasNavBar = mWindowManager.hasNavigationBar();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        addPreferencesFromResource(R.xml.eos_interface_settings);

        mEosQuickSettingsView = (EosMultiSelectListPreference) findPreference("eos_interface_eos_quick_enabled");
        mEosTogglesEnabled = (CheckBoxPreference) findPreference("eos_interface_settings_eos_settings_enabled");
        mRecentsKillallButtonPreference = (CheckBoxPreference) findPreference("eos_interface_recents_killall_button");
        mRecentsMemDisplayPreference = (CheckBoxPreference) findPreference("eos_interface_recents_mem_display");
        mLowProfileNavBar = (CheckBoxPreference) findPreference("eos_interface_navbar_low_profile");
        mHideIndicator = (CheckBoxPreference)findPreference("eos_interface_settings_indicator_visibility");
        mIndicatorColor = (ColorPreference) findPreference("eos_interface_settings_indicator_color");
        mIndicatorDefaultColor = (Preference) findPreference("eos_interface_settings_indicator_color_default");

        if(!mHasNavBar) {
            PreferenceScreen ps = this.getPreferenceScreen();
            PreferenceCategory pc = (PreferenceCategory) ps.findPreference("eos_interface_navbar");
            if (pc != null) ps.removePreference(pc);
        }

        // will be null on tablets and grouper in tablet mode
        if(mLowProfileNavBar != null && mHasNavBar) {
            mLowProfileNavBar.setChecked(Settings.System.getInt(getContentResolver(),
                    EOSConstants.SYSTEMUI_BAR_SIZE_MODE, 0) == 1);
            mLowProfileNavBar.setOnPreferenceChangeListener(this);
        }

        mEosSettingsEnabled = Settings.System.getInt(getContentResolver(),
                EOSConstants.SYSTEMUI_SETTINGS_ENABLED,
                EOSConstants.SYSTEMUI_SETTINGS_ENABLED_DEF) == 1;
        if (mEosTogglesEnabled != null) {
            mEosTogglesEnabled.setChecked(mEosSettingsEnabled);
            mEosTogglesEnabled.notifyDependencyChange(mEosSettingsEnabled);
            mEosTogglesEnabled.setOnPreferenceChangeListener(this);
        }

        if (mEosQuickSettingsView != null) {
            mEosQuickSettingsView.setOnPreferenceChangeListener(this);
            mEosQuickSettingsView.setEntries(getResources().getStringArray(
                    R.array.eos_quick_enabled_names));
            mEosQuickSettingsView.setEntryValues(getResources().getStringArray(
                    R.array.eos_quick_enabled_preferences));
            mEosQuickSettingsView.setReturnFullList(true);
            populateEosSettingsList();
        }

        if (mHideIndicator != null) {
            mHideIndicator.setChecked(Settings.System.getInt(getContentResolver(),
                    EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_HIDDEN,
                    EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_HIDDEN_DEF) == 1);
            mHideIndicator.setOnPreferenceChangeListener(this);
            mHideIndicator.setEnabled(mEosSettingsEnabled);
        }

        if (mIndicatorColor != null) {
            mIndicatorColor.setProviderTarget(EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_COLOR);
            mIndicatorColor.setEnabled(mEosSettingsEnabled);
        }

        if (mIndicatorDefaultColor != null) {
            mIndicatorDefaultColor
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            // TODO Auto-generated method stub
                            Settings.System.putInt(mContext.getContentResolver(),
                                    EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_COLOR,
                                    EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_COLOR_DEF);
                            return true;
                        }
                    });
            mIndicatorDefaultColor.setEnabled(mEosSettingsEnabled);
        }

	    if (mRecentsKillallButtonPreference != null) {
            mRecentsKillallButtonPreference.setOnPreferenceChangeListener(this);
        }
	    if (mRecentsMemDisplayPreference != null) {
            mRecentsMemDisplayPreference.setOnPreferenceChangeListener(this);
        }
    }

    private void populateEosSettingsList() {
        LinkedHashSet<String> selectedValues = new LinkedHashSet<String>();
        String enabledControls = Settings.System.getString(getContentResolver(),
                EOSConstants.SYSTEMUI_SETTINGS_ENABLED_CONTROLS);
        if (enabledControls != null) {
            String[] controls = enabledControls.split("\\|");
            selectedValues.addAll(Arrays.asList(controls));
        } else {
            selectedValues.addAll(Arrays.asList(EOSConstants.SYSTEMUI_SETTINGS_DEFAULTS));
        }
        mEosQuickSettingsView.setValues(selectedValues);

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!manager.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
            mEosQuickSettingsView
                    .removeValueEntry(EOSConstants.SYSTEMUI_SETTINGS_MOBILEDATA);
            mEosQuickSettingsView
                    .removeValueEntry(EOSConstants.SYSTEMUI_SETTINGS_WIFITETHER);
            mEosQuickSettingsView.removeValueEntry(EOSConstants.SYSTEMUI_SETTINGS_USBTETHER);
        }
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if(!(tm.getCurrentPhoneType() == TelephonyManager.PHONE_TYPE_CDMA)
                || !(tm.getNetworkType() == RILConstants.NETWORK_MODE_LTE_CDMA_EVDO)) {
            mEosQuickSettingsView.removeValueEntry(EOSConstants.SYSTEMUI_SETTINGS_LTE);
        }
        if(!hasTorch()) {
            mEosQuickSettingsView.removeValueEntry(EOSConstants.SYSTEMUI_SETTINGS_TORCH);
        }
        mEosQuickSettingsView.setEnabled(mEosSettingsEnabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mEosQuickSettingsView)) {
            Map<String, Boolean> values = (Map<String, Boolean>) newValue;
            StringBuilder newPreferenceValue = new StringBuilder();
            for (Entry entry : values.entrySet()) {
                newPreferenceValue.append(entry.getKey());
                newPreferenceValue.append("|");
            }
            Settings.System.putString(getContentResolver(),
                    EOSConstants.SYSTEMUI_SETTINGS_ENABLED_CONTROLS,
                    newPreferenceValue.toString());
            return true;
        } else if (preference.equals(mRecentsKillallButtonPreference)) {
            Settings.System.putInt(mContext.getContentResolver(),
                    EOSConstants.SYSTEMUI_RECENTS_KILLALL_BUTTON,
                    ((Boolean) newValue).booleanValue() ? 1 : 0);
          return true;
        } else if (preference.equals(mRecentsMemDisplayPreference)) {
            Settings.System.putInt(mContext.getContentResolver(),
                    EOSConstants.SYSTEMUI_RECENTS_MEM_DISPLAY,
                    ((Boolean) newValue).booleanValue() ? 1 : 0);
          return true;
        } else if (preference.equals(mEosTogglesEnabled)) {
            mEosSettingsEnabled = ((Boolean) newValue).booleanValue();
            int val = mEosSettingsEnabled ? 1 : 0;
            Settings.System.putInt(getContentResolver(),
                    EOSConstants.SYSTEMUI_SETTINGS_ENABLED, val);
            mEosTogglesEnabled.notifyDependencyChange(mEosSettingsEnabled ? false : true);
            mEosQuickSettingsView.setEnabled(mEosSettingsEnabled);
            mHideIndicator.setEnabled(mEosSettingsEnabled);
            mIndicatorColor.setEnabled(mEosSettingsEnabled);
            mIndicatorDefaultColor.setEnabled(mEosSettingsEnabled);
            return true;
        } else if (preference.equals(mLowProfileNavBar)) {
            Settings.System.putInt(mContext.getContentResolver(),
                    EOSConstants.SYSTEMUI_BAR_SIZE_MODE,
                    ((Boolean) newValue).booleanValue() ? 1 : 0);
          return true;
        } else if (preference.equals(mHideIndicator)) {
            Settings.System.putInt(mContext.getContentResolver(),
                    EOSConstants.SYSTEMUI_SETTINGS_INDICATOR_HIDDEN,
                    ((Boolean) newValue).booleanValue() ? 1 : 0);
            return true;
        }
        return false;
    }

    private boolean hasTorch() {
        Camera mCamera = null;
        Parameters parameters;
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            parameters = mCamera.getParameters();
            List<String> flashModes = parameters.getSupportedFlashModes();
            if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) return true;
        } catch (RuntimeException e) {
            Log.i("EosInterfaceSettings", "Unable to acquire camera or failed to check if device is Torch capable");
        }
        finally {
            if(mCamera != null){
                mCamera.release();
                mCamera = null;
            }
        }
        return false;
    }
}

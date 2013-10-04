package com.android.settings.katkiss;


import android.app.*;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import 	android.app.AlertDialog.Builder;
import com.android.settings.R;

public class HDMIOutChooserFragment extends DialogFragment implements android.content.DialogInterface.OnClickListener
{
    private static String HDMI_MODE = "hdmi_mode";
    private ContentResolver mContentRes = null; 
    private String[] modes = {"center", "crop", "scale"};

    public HDMIOutChooserFragment() { }

    private void switchHDMIMode(int i)
    {
        //SystemProperties.set("nvidia.hwc.rotation", "HC");
        SystemProperties.set("nvidia.hwc.rotation", "ICS");
        SystemProperties.set("nvidia.hwc.mirror_mode", modes[i]);
    }

    public void onCancel(DialogInterface dialoginterface)
    {
        getActivity().finish();
    }

    public void onClick(DialogInterface dialoginterface, int i)
    {
        android.provider.Settings.System.putInt(mContentRes, HDMI_MODE, i);
        switchHDMIMode(i);
        getActivity().finish();
    }

    public Dialog onCreateDialog(Bundle bundle)
    {
        mContentRes = getActivity().getContentResolver();
        int currentMode = android.provider.Settings.System.getInt(mContentRes, HDMI_MODE, 2);
        Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setSingleChoiceItems(R.array.kk_hdmi_modes, currentMode, this);
        builder.setTitle(R.string.kk_hdmi_modes_title);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }
}


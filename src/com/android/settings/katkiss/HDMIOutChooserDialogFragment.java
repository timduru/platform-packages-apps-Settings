package com.android.settings.katkiss;


import android.app.*;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.app.AlertDialog.Builder;
import com.android.settings.R;
import org.meerkats.katkiss.KatUtils;
import org.meerkats.katkiss.KKC;

public class HDMIOutChooserDialogFragment extends DialogFragment implements android.content.DialogInterface.OnClickListener
{
    private ContentResolver mContentRes = null; 

    public HDMIOutChooserDialogFragment() { }

    public void onCancel(DialogInterface dialoginterface)
    {
        getActivity().finish();
    }

    public void onClick(DialogInterface dialoginterface, int i)
    {
        if(i<0) return;
        android.provider.Settings.System.putInt(mContentRes, KKC.S.HDMI_MODE, i);
        KatUtils.switchHDMIMode(i);
        getActivity().finish();
    }

    public Dialog onCreateDialog(Bundle bundle)
    {
        mContentRes = getActivity().getContentResolver();
        int currentMode = android.provider.Settings.System.getInt(mContentRes, KKC.S.HDMI_MODE, 2);
        Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setSingleChoiceItems(R.array.kk_hdmi_modes, currentMode, this);
        builder.setTitle(R.string.kk_hdmi_modes_title);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }
}


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
import android.content.Intent;
import android.os.UserHandle;



public class WallpaperModes extends DialogFragment implements android.content.DialogInterface.OnClickListener
{
    private ContentResolver mContentRes = null; 

    public WallpaperModes() { }

    public void onCancel(DialogInterface dialoginterface)
    {
        getActivity().finish();
    }

    public void onClick(DialogInterface dialoginterface, int i)
    {
        if(i<0) return;
        int prevMode = android.provider.Settings.System.getInt(mContentRes, KKC.S.SYSTEMUI_WALLPAPER_MODE, KKC.S.WALLPAPER_MODE_DISABLE_SYSTEM);

        android.provider.Settings.System.putInt(mContentRes, KKC.S.SYSTEMUI_WALLPAPER_MODE, i);

	if(i == KKC.S.WALLPAPER_MODE_DISABLE_ALL || prevMode == KKC.S.WALLPAPER_MODE_DISABLE_ALL)
          KatUtils.sendIntentToWindowManager(getActivity(), KKC.I.UI_CHANGED, KKC.I.CMD_REBOOT, false);
	else if(i == KKC.S.WALLPAPER_MODE_DISABLE_SYSTEM || prevMode == KKC.S.WALLPAPER_MODE_DISABLE_SYSTEM)
          KatUtils.sendIntentToWindowManager(getActivity(), KKC.I.UI_CHANGED,null, true);

        getActivity().finish();
    }

    public Dialog onCreateDialog(Bundle bundle)
    {
        mContentRes = getActivity().getContentResolver();
        int currentMode = android.provider.Settings.System.getInt(mContentRes, KKC.S.SYSTEMUI_WALLPAPER_MODE, KKC.S.WALLPAPER_MODE_DISABLE_SYSTEM);
        Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setSingleChoiceItems(R.array.kk_wallpaper_modes, currentMode, this);
        builder.setTitle(R.string.kk_ui_wallpaper_mode_title);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }
}


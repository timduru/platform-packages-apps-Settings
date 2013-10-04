package com.android.settings.katkiss;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.*;
import android.os.Bundle;

public class HDMIReceiver extends Activity
{
    private BroadcastReceiver mReceiver;

    public HDMIReceiver()
    {
        mReceiver = new BroadcastReceiver()
        {

            public void onReceive(Context context, Intent intent)
            {
                if(intent.getAction().equals("android.intent.action.HDMI_PLUGGED") && !intent.getBooleanExtra("state", false) && !isFinishing())
                    finish();
            }
        };
    }

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
/*        StatusBarManager statusbarmanager = (StatusBarManager)getSystemService("statusbar");
        if(statusbarmanager != null)
            statusbarmanager.collapse();*/
        new HDMIOutChooserDialogFragment().show(getFragmentManager(), "hdmiselector");
        IntentFilter intentfilter = new IntentFilter("android.intent.action.HDMI_PLUGGED");
        registerReceiver(mReceiver, intentfilter);
    }

    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}



package com.android.settings.katkiss;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.android.settings.R;
import com.android.settings.katkiss.DockSettingsFragment;
import android.os.SystemProperties;

public class KatReceiver extends BroadcastReceiver 
{
    private Context mContext;

	@Override
    public void onReceive(Context context, Intent intent) 
    {
    	mContext = context;
 	if (Intent.ACTION_DOCK_EVENT.equals(intent.getAction())) refreshDock(intent);
	else new OverClockTask().execute();
    }
    
    
    private void refreshDockSystemProperty(String key, String def) 
    { 
        String current = SystemProperties.get(key, def);
	if("0".equals(current)) return;

        SystemProperties.set(key, "");
        SystemProperties.set(key, current); 
    }

    private void refreshDock(Intent intent)
    {
        Integer mode = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, Intent.EXTRA_DOCK_STATE_UNDOCKED);
        boolean docked = mode != Intent.EXTRA_DOCK_STATE_UNDOCKED;

        if(docked) refreshDockSystemProperty(DockSettingsFragment.KEY_DOCK_POWERMODE, "0"); 
    }

    private class OverClockTask extends AsyncTask<Void, Void, Void>
    {
        boolean thtt = false;

		@Override
		protected Void doInBackground(Void... params) 
		{
	        try 
	        {
	            Process p = Runtime.getRuntime().exec("getprop kk.overclocking.failed");
	            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	            String input;
	            while ((input = reader.readLine()) != null) {
	                if (input.contains("1"))
	                    thtt = true;
	            }
	        } catch (IOException e) {return null;}

	        if (thtt) 
	        {
	            Utils.deletePrefFlag(mContext, Utils.CLOCKS_ON_BOOT_PREF);
	            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	            long when = System.currentTimeMillis();

	            Resources resources = mContext.getResources();
	            Notification notification = new Notification(android.R.drawable.ic_dialog_alert,
	                    resources.getString(R.string.kk_performance_not_applied), when);
	            Intent intent1 = new Intent()
	                    .setClassName("com.android.settings",
	                            "com.android.settings.MAIN")
//	                    .putExtra(Utils.INCOMING_FRAG_KEY, Utils.PERFORMANCE_FRAG_TAG)
	                    .addCategory(Intent.CATEGORY_DEFAULT)
	                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent1, 0);
	            notification.flags = Notification.FLAG_AUTO_CANCEL;
	            notification.setLatestEventInfo(mContext,
	                    resources.getString(R.string.kk_performance_notification_title),
	                    resources.getString(R.string.kk_performance_notification_text), contentIntent);
	            mNotificationManager.notify(0, notification);
	        } else {
	            if (Utils.prefFlagExists(mContext, Utils.CLOCKS_ON_BOOT_PREF)) {
	                String val = Utils.readPrefValue(mContext, Utils.MIN_PREF);
	                Utils.writeKernelValue(Utils.CPU_BASEPATH + "0/" + Utils.CPU_MIN_SCALE, val);
	                Utils.writeKernelValue(Utils.CPU_BASEPATH + "1/" + Utils.CPU_MIN_SCALE, val);
	                val = Utils.readPrefValue(mContext, Utils.MAX_PREF);
	                Utils.writeKernelValue(Utils.CPU_BASEPATH + "0/" + Utils.CPU_MAX_SCALE, val);
	                Utils.writeKernelValue(Utils.CPU_BASEPATH + "1/" + Utils.CPU_MAX_SCALE, val);
	                val = Utils.readPrefValue(mContext, Utils.GOV_PREF);
	                Utils.writeKernelValue(Utils.CPU_BASEPATH + "0/" + Utils.CPU_GOV, val);
	                Utils.writeKernelValue(Utils.CPU_BASEPATH + "1/" + Utils.CPU_GOV, val);
	            }
	            if (Utils.prefFlagExists(mContext, Utils.IOSCHED_PREF)) {
	                String val = Utils.readPrefValue(mContext, Utils.IOSCHED_PREF);
	                Utils.writeKernelValue(Utils.IO_SCHED, val);
	            }
	            if (Utils.hasKernelFeature(Utils.S2W_PATH)) {
	                boolean enabled = Utils.prefFlagExists(mContext, Utils.S2W_PREF);
	                Utils.writeKernelValue(Utils.S2W_PATH, enabled ? "1" : "0");
	            }
	        }
			return null;
		}
    	
    }
}

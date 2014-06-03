package com.android.settings.katkiss;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.settings.R;
import org.meerkats.katkiss.KKC;
import android.app.Activity;


public class ActionPicker 
{

    protected Context mContext;
    protected ContentResolver mResolver;
    private PackageManager mPm;
    private PackageAdapter mAdapter;
    private ArrayList<ResolveInfo> mPackageList;
    private ListView mListView;
    protected Resources mRes;

    public ActionPicker(Context c) 
    {
        mContext = c;
        mRes = mContext.getResources();
        mResolver = mContext.getContentResolver();
        mPm = mContext.getPackageManager();
        View dialog = View.inflate(mContext, R.layout.kk_activity_dialog, null);
        mListView = (ListView) dialog.findViewById(R.id.dialog_list);
        mPackageList = new ArrayList<ResolveInfo>();
        mAdapter = new PackageAdapter(mContext, mPackageList, mPm);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public interface ICallBackResult
    {
      public void pickedAction(String choice);
    }


    public static String getGenericActionLabel(Resources res, String actionID)
    {
        final CharSequence[] item_entries = res.getStringArray(R.array.kk_action_dialog_entries);
        final CharSequence[] item_values = res.getStringArray(R.array.kk_action_dialog_values);
        
        int foundID = -1;
        for(int i=0;i<item_values.length; i++)
        {
        	String id = (String) item_values[i];
        	if(id.equals(actionID)) { foundID = i; break; } 
        }
        if (foundID != -1) return (String)item_entries[foundID];
        else return actionID;
    }
    
    protected void showActionPickerDialog(final ICallBackResult callback) 
    {
        final CharSequence[] item_entries = mRes.getStringArray(R.array.kk_action_dialog_entries);
        final CharSequence[] item_values = mRes.getStringArray(R.array.kk_action_dialog_values);
        mPackageList.clear();
        new PackageLoader().execute(mPackageList);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle( mRes.getString(R.string.kk_action_dialog_title))
                .setNegativeButton(mRes.getString(com.android.internal.R.string.cancel),
                    new Dialog.OnClickListener() 
                    {
                      @Override
                      public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
                    })
                .setItems(item_entries, 
                    new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) 
                      {
                        String choice = (String) item_values[which];
                        // be sure to keep applications menu as the first item
                        if (which == 0)
                            showActivityPickerDialog(callback);
                        else if(which == 1) // Sendkey
                            showSendKeyPickerDialog(callback);
                        else if(which == 2) // Sendkeycode
                            showSendKeyCodePicker(callback);
                        else // Custom Action
                            callback.pickedAction(choice);
                      }
                }).create().show();
    }

    private void showActivityPickerDialog(final ICallBackResult callback) 
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setAdapter(mAdapter, 
          new Dialog.OnClickListener() 
          {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ResolveInfo app = (ResolveInfo) mListView.getAdapter().getItem(which);
                String name = app.activityInfo.loadLabel(mPm).toString();
                String component = new ComponentName(app.activityInfo.packageName, app.activityInfo.name).flattenToString();
                Drawable d = app.activityInfo.loadIcon(mPm);
                callback.pickedAction("app:" + component);
            }
          })
              .setTitle( mRes.getString(R.string.kk_activity_dialog_title))
              .setNegativeButton(mRes.getString(com.android.internal.R.string.cancel),
                        new Dialog.OnClickListener() 
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
                        })
                .create().show();
    }

    private class PackageLoader extends AsyncTask<ArrayList<ResolveInfo>, ArrayList<ResolveInfo>, ArrayList<ResolveInfo>> 
    {

        @Override
        protected ArrayList<ResolveInfo> doInBackground(ArrayList<ResolveInfo>... list) {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            for (ResolveInfo info : mPm.queryIntentActivities(intent, 0)) {
                list[0].add(info);
                publishProgress(list[0]);
                // sort the app packages by simple name
                Collections.sort(list[0], new Comparator<ResolveInfo>() {
                    @Override
                    public int compare(ResolveInfo lhs, ResolveInfo rhs) {
                        return lhs.activityInfo.loadLabel(mPm).toString()
                                .compareToIgnoreCase(rhs.activityInfo.loadLabel(mPm).toString());
                    }
                });
                publishProgress(list[0]);

            }
            return list[0];
        }

        @Override
        protected void onProgressUpdate(ArrayList<ResolveInfo>... list) 
        { super.onProgressUpdate(list); mAdapter.notifyDataSetChanged(); }

        @Override
        protected void onPostExecute(ArrayList<ResolveInfo> list) 
        { super.onPostExecute(list); }
    }

    private class KeyPickerCallBack implements ChooserDialogFragment.ICallBackResult
    {
        ICallBackResult mFinalActionCallBack;
        public KeyPickerCallBack(final ICallBackResult finalActionCallback) {mFinalActionCallBack = finalActionCallback;}

        @Override
        public void choice(String choice) { if(choice != null) mFinalActionCallBack.pickedAction(KKC.A.SENDKEY_BASE + choice); }
    }

    private void showSendKeyPickerDialog(final ICallBackResult callback)
    {
        String[] labelsFunctionKeys = mContext.getResources().getStringArray(R.array.kk_keyoverride_function_entries);
        String[] valuesFunctionKeys = mContext.getResources().getStringArray(R.array.kk_keyoverride_function_values); // String KEY
	for(int i=0; i<valuesFunctionKeys.length; i++)
		valuesFunctionKeys[i] = "" + android.view.KeyEvent.keyCodeFromString(valuesFunctionKeys[i]);

        String[] labelsSpecialKeys = mContext.getResources().getStringArray(R.array.kk_keyoverride_special_entries);
        String[] valuesSpecialKeys = mContext.getResources().getStringArray(R.array.kk_keyoverride_special_values);

// concatenate both key lists
	int totalSize = labelsFunctionKeys.length + labelsSpecialKeys.length;
	String[] labels = new String[totalSize];
	String[] values = new String[totalSize];

        for(int i=0; i<labelsFunctionKeys.length; i++)
	{ labels[i] = labelsFunctionKeys[i]; values[i] = valuesFunctionKeys[i]; }

        for(int i=0; i<labelsSpecialKeys.length; i++)
        { labels[labelsFunctionKeys.length + i] = labelsSpecialKeys[i]; values[labelsFunctionKeys.length + i] = valuesSpecialKeys[i]; }

// Display Dialog
        ChooserDialogFragment keyChooserDialog = new ChooserDialogFragment(R.string.kk_sendkey_title, new KeyPickerCallBack(callback), labels, values);
        keyChooserDialog.show((Activity) mContext);
    }

    private void showSendKeyCodePicker(final ICallBackResult callback)
    {
        UserPromptDialog picker = new UserPromptDialog((Activity) mContext,  R.string.kk_keycode_title, R.string.kk_keycode_msg)
           {
             @Override
              public boolean onChoiceValidate(String choice)
              {
                Integer val = null;
		try { val = Integer.parseInt(choice); }
		catch(Exception e) {}

		if(val == null || val <=0 || val > 230)  { Toast.makeText(mContext, "Not a Valid Keycode (0 < KeyCode < 230) ", Toast.LENGTH_LONG).show();return false; }

		callback.pickedAction(KKC.A.SENDKEY_BASE + choice); 
               	return true; // true = close dialog
              }
           };
        picker.show();
    }



}

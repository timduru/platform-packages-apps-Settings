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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.settings.R;

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
                        else
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
}

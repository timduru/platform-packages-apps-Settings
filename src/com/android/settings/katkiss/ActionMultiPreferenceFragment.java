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
import android.preference.PreferenceFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.settings.R;

public abstract class ActionMultiPreferenceFragment extends PreferenceFragment 
{
    private ArrayList<ActionPreference> mActionPreferenceList;
    private ActionPicker mActionPicker;

    class PrefClickListener implements View.OnClickListener 
    {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            String tag = (String) v.getTag();
            if (tag != null) {
                for (ActionPreference ap : mActionPreferenceList) {
                    if (tag.equals(ap.getTargetUri())) {
                        mActionPicker.showActionPickerDialog(ap);
                        break;
                    }
                }
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        mActionPicker = new ActionPicker((Context) getActivity());
    }

    protected void addActionPreference(ActionPreference pref) 
    {
	if(pref == null) return;
        pref.setListener(new PrefClickListener());

        if(mActionPreferenceList == null) mActionPreferenceList = new ArrayList<ActionPreference>();
        mActionPreferenceList.add(pref);
    }
}

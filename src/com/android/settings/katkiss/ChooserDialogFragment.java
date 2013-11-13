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
import java.util.List;
import android.app.Activity;



public class ChooserDialogFragment extends DialogFragment implements android.content.DialogInterface.OnClickListener
{
    private ContentResolver mContentRes = null; 
    private String[] _arrayValues, _arrayLabels; 
    private ICallBackResult _callback;
    private int _titleId;

    public ChooserDialogFragment(int titleId, final ICallBackResult callback, String[] arrayLabels, String[] arrayValues) 
    { init (titleId, callback, arrayLabels, arrayValues); }

    public ChooserDialogFragment(int titleId, final ICallBackResult callback, String[] arrayLabels) 
    { init (titleId, callback, arrayLabels,  null); }

    private void init(int titleId, final ICallBackResult callback, String[] arrayLabels, String[] arrayValues) 
    { 
	_titleId = titleId;
	_callback = callback;
	_arrayLabels = arrayLabels;
	_arrayValues = arrayValues;
    }

    public interface ICallBackResult
    {
      public void choice(String choice);
    }

    public void onCancel(DialogInterface dialoginterface) { getActivity().finish(); }

    public void onClick(DialogInterface dialoginterface, int i)
    {
        if(i<0) return;
	if(_arrayValues != null)
		_callback.choice(_arrayValues[i]);
	else
		_callback.choice("" + i);
	dismiss();
    }

    public Dialog onCreateDialog(Bundle bundle)
    {
        mContentRes = getActivity().getContentResolver();
        Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setSingleChoiceItems(_arrayLabels, 0, this);
        builder.setTitle(_titleId);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    public void show(Activity activity)
    {
	show(activity.getFragmentManager(), "Send Key");
    } 
}


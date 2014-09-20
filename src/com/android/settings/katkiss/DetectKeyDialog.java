package com.android.settings.katkiss;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.app.AlertDialog;
import android.view.KeyEvent;
 
public abstract class DetectKeyDialog extends AlertDialog.Builder implements OnClickListener 
{
  private final TextView _txt;

  public DetectKeyDialog(Context context, int title, int msg) 
  {
	super(context);
	setMessage(msg);
	setTitle(title);
 
	_txt = new TextView(context);
	setView(_txt);
 
//	setPositiveButton(com.android.internal.R.string.ok, this);
	setNegativeButton(com.android.internal.R.string.cancel, this);
	DialogKeyListener dkl = new DialogKeyListener();        
	setOnKeyListener(dkl);
 }

  private class DialogKeyListener implements android.content.DialogInterface.OnKeyListener
  {
	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) 
	{
		_txt.setText("" + keyCode);
	       	if(onChoiceValidate("" + keyCode))
                	dialog.dismiss();
	        return true;
	}
  }
 
  public void onClick(DialogInterface dialog, int which) 
  {
  	if (which != DialogInterface.BUTTON_POSITIVE) { onCancelClicked(dialog); return; }

   	if(onChoiceValidate(_txt.getText().toString()))
		dialog.dismiss();
  }
 
  public void onCancelClicked(DialogInterface dialog) { dialog.dismiss(); }

  abstract public boolean onChoiceValidate(String choice);
}

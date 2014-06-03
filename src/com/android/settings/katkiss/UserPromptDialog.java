package com.android.settings.katkiss;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
import android.app.AlertDialog;
 
public abstract class UserPromptDialog extends AlertDialog.Builder implements OnClickListener 
{
  private final EditText _txt;

  public UserPromptDialog(Context context, int title, int msg) 
  {
	super(context);
	setMessage(msg);
	setTitle(title);
 
	_txt = new EditText(context);
	setView(_txt);
 
	setPositiveButton(com.android.internal.R.string.ok, this);
	setNegativeButton(com.android.internal.R.string.cancel, this);
 }
 
  @Override
  public void onClick(DialogInterface dialog, int which) 
  {
  	if (which != DialogInterface.BUTTON_POSITIVE) { onCancelClicked(dialog); return; }

   	if(onChoiceValidate(_txt.getText().toString()))
		dialog.dismiss();
  }
 
  public void onCancelClicked(DialogInterface dialog) { dialog.dismiss(); }

  abstract public boolean onChoiceValidate(String choice);
}

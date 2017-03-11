package com.android.settings.katkiss;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;


public class ActionPickerActivity extends Activity  implements ActionPicker.ICallBackResult
{
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_detail);
        new ActionPicker(this).showActionPickerDialog(this);
    }

    @Override
    public void onBackPressed() { setResult(Activity.RESULT_CANCELED); super.onBackPressed(); finish();}

    @Override
    public void pickedAction(String choice)
    {
            final Intent data = new Intent();
            data.putExtra("action",choice);
            setResult(Activity.RESULT_OK, data);
            finish();
    }
}

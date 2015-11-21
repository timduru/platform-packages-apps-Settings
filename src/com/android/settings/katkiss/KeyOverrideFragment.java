package com.android.settings.katkiss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ToggleButton;

import android.content.Context;
import android.content.res.Resources;

import org.meerkats.katkiss.KatUtils;
import org.meerkats.katkiss.KeyActions;
import com.android.settings.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class KeyOverrideFragment extends ListFragment implements ActionPicker.ICallBackResult
{
	private ArrayList<KeyActions> _keyActionsList;
	private String _currentKeyCode = null;
	private Integer _currentFlags = 0;
   	private ActionPicker _actionPicker;
   	private Context _context;
   	private OnClickListener _addKeyOverrideClickListener = null;
   	private OnClickListener _detectKeyOverrideClickListener = null;
   	private KeyActionsArrayAdapter listArrayAdapter;
   	private ToggleButton shiftL, shiftR, ctrlL, ctrlR, altL, altR;
	
	public KeyOverrideFragment()
	{
		_addKeyOverrideClickListener = new OnClickListener() 
										{           
											  @Override
											  public void onClick(View v) 
											  {   OnClickAddNewKeyOverride(v); }    
										};
		
                _detectKeyOverrideClickListener = new OnClickListener()
                                                                                {
                                                                                          @Override
                                                                                          public void onClick(View v)
                                                                                          {  showDetectKeyPicker() ; }
                                                                                };

	}
	
	public class KeyActionsArrayAdapter extends ArrayAdapter<KeyActions> implements OnClickListener
	{
		  private final List<KeyActions> _keyList;
		  private final Activity _context;

		  class ViewHolder 
		  {
		    protected TextView keyLabel;
		    protected TextView keyActions;
		    protected ImageButton delete;
		  }

		  public KeyActionsArrayAdapter(Activity context, List<KeyActions> list) 
		  {
		    super(context, R.layout.kk_keyoverride_list, list);
		    _context = context;
		    _keyList = list;
		  }

		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) 
		  {
		    View view = convertView;

		    if (view == null) 
		    {
		      LayoutInflater inflator = _context.getLayoutInflater();
		      view = inflator.inflate(R.layout.kk_keyoverride_list, null);
		      final ViewHolder viewHolder = new ViewHolder();
		      viewHolder.keyLabel = (TextView) view.findViewById(R.id.keylabel);
		      viewHolder.keyActions = (TextView) view.findViewById(R.id.keyactions);
		      viewHolder.delete = (ImageButton) view.findViewById(R.id.del);
		      view.setTag(viewHolder);
		    }
		    
		    ViewHolder holder = (ViewHolder) view.getTag();
		    KeyActions keyActions = _keyList.get(position);
		    holder.keyLabel.setText("" + getKeyString(keyActions.getID()));
		    holder.keyActions.setText(keyActions.toString());
		    holder.delete.setTag(keyActions);
		    holder.delete.setOnClickListener(this);		    
		    return view;
		  }

		@Override
		public void onClick(View v) 
		{
			KeyActions keyActions = (KeyActions) v.getTag();
			if(keyActions != null)
			{
				keyActions.deleteFromSettings();
				_keyList.remove(keyActions);
			}
			notifyDataSetChanged();
		}
	} // class KeyActionsArrayAdapter
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View v = inflater.inflate(R.layout.kk_keyoverride, null);    
		
		ImageButton addBtn = (ImageButton) v.findViewById(R.id.functionkeys_add);
		addBtn.setOnClickListener(_addKeyOverrideClickListener);

		addBtn = (ImageButton) v.findViewById(R.id.detectkey_add);
		addBtn.setOnClickListener(_addKeyOverrideClickListener);

		Button detectBtn = (Button) v.findViewById(R.id.detectkey_btn);
		detectBtn.setOnClickListener(_detectKeyOverrideClickListener);

		shiftL = (ToggleButton) v.findViewById(R.id.shiftL);
		shiftR = (ToggleButton) v.findViewById(R.id.shiftR);
		ctrlL = (ToggleButton) v.findViewById(R.id.ctrlL);
		ctrlR = (ToggleButton) v.findViewById(R.id.ctrlR);
		altL = (ToggleButton) v.findViewById(R.id.altL);
		altR = (ToggleButton) v.findViewById(R.id.altR);

		return v;       
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		_context = (Context) getActivity();
		_actionPicker = new ActionPicker(_context);
		super.onActivityCreated(savedInstanceState);
		refreshKeyActionsList();
		listArrayAdapter = new KeyActionsArrayAdapter(getActivity(),  _keyActionsList);
		setListAdapter(listArrayAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
	
	
	}

	private void refreshKeyActionsList()
	{
		if(_keyActionsList == null) _keyActionsList = new ArrayList<KeyActions>();
		_keyActionsList.clear();

		ArrayList<KeyActions> keyActionsList = KatUtils.getKeyOverrideList(_context);
		if(keyActionsList != null) 
			_keyActionsList.addAll(keyActionsList);
	}

 	public void OnClickAddNewKeyOverride(View view) 
	{
		_currentKeyCode = null;
		switch(view.getId())
		{
			case R.id.functionkeys_add:
				_currentKeyCode = getSelectedKeyCode(R.id.functionkeys_spinner, R.array.kk_keyoverride_function_values, true);
			break;
			case R.id.detectkey_add:
		                TextView txt  = (TextView) getView().findViewById(R.id.detected_keycode);
				_currentKeyCode = txt.getText().toString();
			break;
		}

		refreshCurrentFlags(_currentKeyCode);

		if(_currentKeyCode != null && ! _currentKeyCode.equals(""))
			_actionPicker.showActionPickerDialog(this);
 	}

	private void refreshCurrentFlags(String keyCode)
	{ 
		_currentFlags = 0;
		if(shiftL.isChecked() || "59".equals(keyCode)) _currentFlags |= KeyEvent.META_SHIFT_LEFT_ON | KeyEvent.META_SHIFT_ON;
		if(shiftR.isChecked() || "60".equals(keyCode)) _currentFlags |= KeyEvent.META_SHIFT_RIGHT_ON | KeyEvent.META_SHIFT_ON;
		if(ctrlL.isChecked() || "113".equals(keyCode)) _currentFlags |= KeyEvent.META_CTRL_LEFT_ON | KeyEvent.META_CTRL_ON;
		if(ctrlR.isChecked() || "114".equals(keyCode)) _currentFlags |= KeyEvent.META_CTRL_RIGHT_ON | KeyEvent.META_CTRL_ON;
		if(altL.isChecked() || "57".equals(keyCode)) _currentFlags |= KeyEvent.META_ALT_LEFT_ON | KeyEvent.META_ALT_ON;
		if(altR.isChecked() || "58".equals(keyCode)) _currentFlags |= KeyEvent.META_ALT_RIGHT_ON | KeyEvent.META_ALT_ON;
	}

	private  String getSelectedKeyCode(int spinnerViewId, CharSequence[] item_values, boolean convertFromString)
	{
		String keyCode = "";
		Spinner spinner = (Spinner) getView().findViewById(spinnerViewId);
		String selectionValue = (String) item_values[spinner.getSelectedItemPosition()];

		if(convertFromString)
			return "" + android.view.KeyEvent.keyCodeFromString(selectionValue);
		else 
			return selectionValue;
		
	}

        private  String getSelectedKeyCode(int spinnerViewId, int valuesArrayId, boolean convertFromString)
        {
		return getSelectedKeyCode(spinnerViewId, _context.getResources().getStringArray(valuesArrayId), convertFromString);
        }

        private  String getSelectedKeyCode(int spinnerViewId, List<String> values, boolean convertFromString)
        {
                String keyCode = "";
                Spinner spinner = (Spinner) getView().findViewById(spinnerViewId);
                String selectionValue = (String) values.get(spinner.getSelectedItemPosition());

                if(convertFromString)
                        return "" + android.view.KeyEvent.keyCodeFromString(selectionValue);
                else
                        return selectionValue;

        }

        private  String getKeyString(int code)
	{
		String keycodeString = KeyEvent.keyCodeToString(code);
		if(keycodeString.length() > "KEYCODE_".length()) keycodeString = keycodeString.substring("KEYCODE_".length());
		return keycodeString;
	}


	@Override
	public void pickedAction(String choice)
	{
		KeyActions keyAction = new KeyActions(_context, Integer.parseInt(_currentKeyCode) );
		keyAction.initFromSettings();

		if(choice.equals("none")) keyAction.delFlag(_currentFlags);
		else keyAction.addFlagAction(_currentFlags, choice);
		keyAction.writeToSettings();

		refreshKeyActionsList();
		listArrayAdapter.notifyDataSetChanged();
	}


    private void showDetectKeyPicker()
    {
        DetectKeyDialog picker = new DetectKeyDialog((Activity) _context,  R.string.kk_detectkey_title, R.string.kk_keycode_msg)
           {
             @Override
              public boolean onChoiceValidate(String choice)
              {
                //if(val == null || val <=0 || val > 230)  { Toast.makeText(mContext, "Not a Valid Keycode (0 < KeyCode < 230) ", Toast.LENGTH_LONG).show();return false; }

                //callback.pickedAction(KKC.A.SENDKEY_BASE + choice);
                ((TextView) getView().findViewById(R.id.detected_keycode)) .setText(choice);
                ((TextView) getView().findViewById(R.id.detected_keylabel)) .setText(getKeyString(Integer.parseInt(choice)));
//                _currentKeyCode = choice;
                return true; // true = close dialog
              }
           };
        picker.show();
    }


}

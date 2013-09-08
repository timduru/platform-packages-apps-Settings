package com.android.settings.katkiss;

import android.os.Bundle;
import com.android.settings.R;


public class SoftKeyActions extends ActionFragment {

    public static SoftKeyActions newInstance(Bundle args) {
        SoftKeyActions frag = new SoftKeyActions();
        if (args != null) {
            args.putString(Utils.FRAG_TITLE_KEY, "Softkey Actions");
        }
        frag.setArguments(args);
        return frag;
    }

    public static SoftKeyActions newInstance() {
        SoftKeyActions frag = new SoftKeyActions();
        Bundle args = new Bundle();
        args.putString(Utils.FRAG_TITLE_KEY, "Softkey Actions");
        frag.setArguments(args);
        return frag;
    }

    public SoftKeyActions(Bundle args) {
        newInstance(args);
    }

    public SoftKeyActions() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.kk_softkey_settings);

        addActionPreference((ActionPreference) findPreference("kk_ui_back_longpress"));
        addActionPreference((ActionPreference) findPreference("kk_ui_recent_longpress"));
        addActionPreference((ActionPreference) findPreference("kk_ui_menu_longpress"));
    }
}

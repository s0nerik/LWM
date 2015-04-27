package app;

import android.content.Context;
import android.preference.PreferenceManager;

import com.tale.prettysharedpreferences.IntegerEditor;
import com.tale.prettysharedpreferences.PrettySharedPreferences;

public class PrefManager extends PrettySharedPreferences<PrefManager> {

    public PrefManager(Context context) {
        super(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public IntegerEditor<PrefManager> drawerSelection() {
        return getIntegerEditor("drawer_selection");
    }

}
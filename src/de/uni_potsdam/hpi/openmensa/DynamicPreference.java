package de.uni_potsdam.hpi.openmensa;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class DynamicPreference extends ListPreference {


    public DynamicPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateDialogView() {
        ListView view = new ListView(getContext());
        view.setAdapter(adapter());
        setEntries(entries());
        setEntryValues(entryValues());
        //setValueIndex(initializeIndex());
        return view;
    }

    private ListAdapter adapter() {
        return new ArrayAdapter(getContext(), android.R.layout.select_dialog_singlechoice);
    }

    private CharSequence[] entries() {
    	CharSequence[] entries = { "Griebnitzsee", "Golm", "Neues Palais" };
        return entries;
    }

    private CharSequence[] entryValues() {
    	CharSequence[] entryValues = { "1", "2", "3" };
    	return entryValues;
    }
}

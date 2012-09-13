package de.uni_potsdam.hpi.openmensa;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.Canteens;

/**
 * Provides simple methods to access shared settings.
 * 
 * @author dominik
 *
 */
public class SettingsProvider {

	public static final String KEY_SOURCE_URL = "pref_source_url";
	public static final String KEY_CANTEENS = "pref_canteens";
	public static final String KEY_ACTIVE_CANTEENS = "pref_active_canteens";
	
	private static Gson gson = new Gson();
	
    
    private static SharedPreferences getSharedPrefs(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public static String getSourceUrl(Context context) {
    	String url = getSharedPrefs(context).getString(KEY_SOURCE_URL, context.getResources().getString(R.string.source_url_default));
    	return url;
    }
    
    public static Canteens getCanteens(Context context) {
    	// Throws ClassCastException if there is a preference with this name that is not a Set.
    	Canteens canteens = new Canteens();
    	String json = getSharedPrefs(context).getString(KEY_CANTEENS, "{}");
    	canteens = gson.fromJson(json, Canteens.class);
		return canteens;
    }
    
    public static void setCanteens(Context context, Canteens canteens) {
    	String json = gson.toJson(canteens);
    	SharedPreferences.Editor editor = getSharedPrefs(context).edit();
    	editor.putString(SettingsProvider.KEY_CANTEENS, json);
    	editor.commit();
    }

    /**
     * sets the active canteens in the canteens object
     * @param context
     */
	public static void refreshActiveCanteens(Context context) {
		Set<String> activeCanteensKeys = getSharedPrefs(context).getStringSet(KEY_ACTIVE_CANTEENS, new HashSet<String>());
		Canteens canteens = getCanteens(context);
		for (Canteen canteen : canteens.values()) {
			canteen.active = activeCanteensKeys.contains(canteen.key);
		}
		setCanteens(context, canteens);
	}
}

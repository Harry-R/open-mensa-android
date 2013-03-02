package de.uni_potsdam.hpi.openmensa.api;

import java.util.Calendar;
import java.util.HashMap;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import de.uni_potsdam.hpi.openmensa.MainActivity;
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsProvider;
import de.uni_potsdam.hpi.openmensa.helpers.SpinnerItem;

/**
 * 
 * @author dominik
 *
 */
public class Canteen implements SpinnerItem {	
	@SerializedName("id")
	public String key = null;
	
	@SerializedName("name")
	public String name = "Dummy";
	
	@SerializedName("address")
	public String address;
	
	@SerializedName("coordinates")
	public Float[] coordinates;
	
	//==================
	// app only, not api

	/**
	 * date -> meals
	 */
	@SerializedName("_days")
	public HashMap<String, Day> days;
	
	/**
	 * save when we last fetched
	 */
	@SerializedName("_updates")
	public HashMap<String, Long> updates;
	
	public Canteen(String key, String name) {
		this.name = name;
		this.key = key;
	}

	@Override
	public String toString() {
		return name;
	}

	public void updateDays(Days newDays) {
		if (days == null)
			days = new HashMap<String, Day>();
		for (Day day : newDays) {
			days.put(day.date, day);
		}
	}
	
	public Day getDay(String date) {
		if (days == null) {
			return null;
		}
		return days.get(date);
	}
	
	public void justUpdated(String date) {
		if (updates == null)
			updates = new HashMap<String, Long>();
		Calendar now = Calendar.getInstance();
		updates.put(date, now.getTimeInMillis());
	}

	/**
	 * normally, you should fetch whenever no day is present,
	 * but some open mensa feeds are not complete. So let's see whether
	 * we have already fetched the information for a certain day lately.
	 * 
	 * @param string
	 * @return
	 */
	public boolean isOutOfDate(String date) {
		if (updates == null)
			return true;
		
		Calendar now = Calendar.getInstance();
		Long lastUpdate = updates.get(date);
		
		if (lastUpdate == null)
			return true;
		
		// 1 hour
		int maxDiff = 1000*60*60;
		if (now.getTimeInMillis() - lastUpdate > maxDiff) {
			return true;
		}
		return false;
	}

	@Override
	public boolean execute(MainActivity mainActivity, int itemPosition) {
		Canteen c = SettingsProvider.getStorage(mainActivity).getFavouriteCanteens().get(itemPosition);
		Log.d(MainActivity.TAG, String.format("Chose canteen %s", c.key));
		mainActivity.changeCanteenTo(c);
		return true;
	}
}

package de.uni_potsdam.hpi.openmensa;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.openmensa.api.Canteen;
import de.uni_potsdam.hpi.openmensa.api.Meal;

public class MainActivity extends FragmentActivity implements
		OnSharedPreferenceChangeListener, OnNavigationListener,
		OnFinishedFetchingCanteensListener, OnFinishedFetchingMealsListener {

	public static final String TAG = "Canteendroid";
	public static final Boolean LOGV = true;
	public static final String PREFS_NAME = "CanteendroidPrefs";

	private int mYear;
	private int mMonth;
	private int mDay;

	private static Storage storage = new Storage();
	private SpinnerAdapter spinnerAdapter;
	
	static Context context;
	
	Gson gson = new Gson();

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections
		// of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		reload();
		
		refreshActiveCanteens();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		storage.flush(this);
	}

	/**
	 * Refreshes the canteens in the action bar
	 * 
	 * TODO: should wait for completion of refreshAvailableCanteens()
	 */
	private void refreshActiveCanteens() {
		Log.d(TAG, "Refreshing active canteen list");
		
		ArrayList<Canteen> activeCanteens = storage.getActiveCanteens();
		
		if (activeCanteens.size() == 0) {
			new AlertDialog.Builder(this)
			.setTitle("No active canteens.")
			.setMessage("You have not yet set any active canteens. Please select at least one.")
			.setNeutralButton("Ok",
			new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent settings = new Intent(MainActivity.context, SettingsActivity.class);
				startActivity(settings);
			}
			}).show();
		}
		
		Log.d(TAG, String.format("%s active canteens", activeCanteens.size()));
		
		ActionBar actionBar = getActionBar();
		spinnerAdapter = new ArrayAdapter<Canteen>(this, android.R.layout.simple_spinner_dropdown_item, activeCanteens);
		actionBar.setListNavigationCallbacks(spinnerAdapter, this);
		
		Canteen curr = storage.getCurrentCanteen();
		if(curr != null) {
			Log.d(TAG, curr.toString());
			int displayedCanteenPosition = activeCanteens.indexOf(curr);
			actionBar.setSelectedNavigationItem(displayedCanteenPosition);
		}
	}

	/**
	 * Refreshes the available canteens list
	 */
	private void refreshAvailableCanteens() {
		// load available canteens from settings and afterwards refetch the list from server
		// TODO: should be avoided by caching..., 
		// TODO: but still needs to refresh the view and set the available canteens
		
		String baseUrl = SettingsProvider.getSourceUrl(this);
		String url = baseUrl + "canteens";
		
		RetrieveFeedTask task = new RetrieveCanteenFeedTask(this, this);
		task.execute(new String[] { url });
	}
	
	@Override
	public void onCanteenFetchFinished(RetrieveCanteenFeedTask task) {
		storage.saveCanteens(this, task.getCanteens());
		
		refreshActiveCanteens();
	}
	
	public void fetchMealFeed() {
		//RetrieveFeedTask task = new RetrieveMealFeedTask(this.getActivity(), this);
		//task.execute(new String[] { "source_url" });
	}
	
	@Override
	public void onMealFetchFinished(RetrieveMealFeedTask task) {
		//listItems.addAll(task.getMealList());
		//adapter.notifyDataSetChanged();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * Change the current canteen
	 */
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		Canteen c = storage.getActiveCanteens().get(itemPosition);
		Log.d(TAG, String.format("Chose canteen %s", c.key));
		storage.setCurrentCanteen(c);
		// TODO: need to refresh the view
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent settings = new Intent(this, SettingsActivity.class);

		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_settings:
				startActivity(settings);
				return true;
			case R.id.reload:
				reload();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsProvider.KEY_ACTIVE_CANTEENS)) {
			// when changed in settings -> also change in canteens object
			SettingsProvider.refreshActiveCanteens(this);
			refreshActiveCanteens();
		}
		if (key.equals(SettingsProvider.KEY_SOURCE_URL)) {
			reload();
		}
	}

	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * 
	 * @param context
	 * @return True if device has Internet
	 * 
	 *  Code from: http://www.androidsnippets.org/snippets/131/
	 */
	public static boolean isOnline(Context context) {

		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}
		if (info.isRoaming()) {
			// here is the roaming option you can change it if you want to
			// disable Internet while roaming, just return false
			return false;
		}
		return true;
	}

	/**
	 * Refreshes the meals hash by fetching the data from the API and then displays the latest data.
	 * 
	 * TODO: make it work
	 */
	private void reload() {
		storage.refreshStorage(this);
		
		if (!isOnline(MainActivity.this)) {
			new AlertDialog.Builder(MainActivity.this).setNegativeButton("Okay",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).setTitle("Not Connected").setMessage("You are not connected to the Internet.");
		} else {
			// fetch menu feed and maybe canteens
			
			if (storage.isOutOfDate()) {
				Log.d(TAG, "Fetch canteens because storage is out of date");
				// async
				refreshAvailableCanteens();
			}
		}

//		int index = mViewPager.getCurrentItem();
//		mSectionsPagerAdapter.getItem(index);
//		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/**
		 * Creates an Item
		 */
		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new DaySectionFragment();
			Bundle args = new Bundle();

			args.putString(DaySectionFragment.ARG_DATE, "2012-09-17");
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return getString(R.string.title_section1).toUpperCase();
				case 1:
					return getString(R.string.title_section2).toUpperCase();
				case 2:
					return getString(R.string.title_section3).toUpperCase();
			}
			return null;
		}
	}

	/**
	 * A fragment representing a section of the app, that displays the Meals for
	 * one Day.
	 */
	public static class DaySectionFragment extends ListFragment{
		public DaySectionFragment() {
		}

		public static final String ARG_DATE = "date";
		private ArrayList<Meal> listItems;
		private String date;
		MealAdapter adapter;
		private Canteen canteen;

		/**
		 * When creating, retrieve this instance's number from its arguments.
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle args = getArguments();

			canteen = MainActivity.storage.getCurrentCanteen();
			date = args.getString(ARG_DATE);
		}

		/**
		 * The Fragment's UI is just a simple text view showing its instance
		 * number.
		 */
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			View listView = inflater.inflate(R.layout.fragment_pager_list,
					container, false);
			// TextView titleView = (TextView)
			// listView.findViewById(R.id.title);
			// titleView.setText(mensaName);
			return listView;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			listItems = new ArrayList<Meal>();

			adapter = new MealAdapter(getActivity(), R.layout.list_item, listItems);

			// Assign adapter to ListView
			setListAdapter(adapter);
			
			ArrayList<Meal> meals = canteen.getMeals(date);
			listItems.addAll(meals);
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			Log.i("FragmentList", "Item clicked: " + id);
		}
	}
}

/*
 * Copyright (c) 2012 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.shared.ForecastItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MountainPassItemActivity extends SherlockFragmentActivity {
	
	DateFormat parseDateFormat = new SimpleDateFormat("yyyy,M,d,H,m"); //e.g. [2010, 11, 2, 8, 22, 32, 883, 0, 0]
	DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	
	private ArrayList<CameraItem> cameraItems;
	private ArrayList<ForecastItem> forecastItems;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    Bundle b = getIntent().getExtras();
	    String mountainPassName = b.getString("MountainPassName");
	    
        getSupportActionBar().setTitle(mountainPassName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

	    cameraItems = (ArrayList<CameraItem>)getIntent().getSerializableExtra("Cameras");
	    forecastItems = (ArrayList<ForecastItem>)getIntent().getSerializableExtra("Forecasts");        
        
        ActionBar.Tab reportTab = getSupportActionBar().newTab();
        reportTab.setText("Report");
        reportTab.setTabListener(new TabListener<MountainPassItemReportFragment>(this, "Report", MountainPassItemReportFragment.class, b));
        getSupportActionBar().addTab(reportTab);	    
	    
	    if (!cameraItems.isEmpty()) {
	        ActionBar.Tab camerasTab = getSupportActionBar().newTab();
	        camerasTab.setText("Cameras");
	        camerasTab.setTabListener(new TabListener<MountainPassItemCameraFragment>(this, "Cameras", MountainPassItemCameraFragment.class, b));
	        getSupportActionBar().addTab(camerasTab); 
	    }
        
	    if (!forecastItems.isEmpty()) {
	        ActionBar.Tab forecastTab = getSupportActionBar().newTab();
	        forecastTab.setText("Forecast");
	        forecastTab.setTabListener(new TabListener<MountainPassItemForecastFragment>(this, "Forecast", MountainPassItemForecastFragment.class, b));
	        getSupportActionBar().addTab(forecastTab); 	    	
	    }
        
        if (savedInstanceState != null) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.star, menu);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	    	return true;
	    case R.id.menu_star:
			Toast.makeText(this, "Starred", Toast.LENGTH_SHORT).show();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*Save the selected tab in order to restore in screen rotation*/
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }
    
    public class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final SherlockFragmentActivity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;

        /** Constructor used each time a new tab is created.
          * @param activity  The host Activity, used to instantiate the fragment
          * @param tag  The identifier tag for the fragment
          * @param clz  The fragment's Class, used to instantiate the fragment
          * @param args The fragment's passed arguments
          */
        public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;
            
            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();


            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                ft.detach(mFragment);
            }
        }       

        /* The following are each of the ActionBar.TabListener callbacks */

        public void onTabSelected(Tab tab, FragmentTransaction ft) {

                ft = mActivity.getSupportFragmentManager().beginTransaction();

            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
                ft.commit();
            } else {
                ft.attach(mFragment);
                ft.commit();
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

            ft = mActivity.getSupportFragmentManager().beginTransaction();

            if (mFragment != null) {
                ft.detach(mFragment);
                ft.commitAllowingStateLoss();
            }   
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Usually do nothing.
        }
    }	
}
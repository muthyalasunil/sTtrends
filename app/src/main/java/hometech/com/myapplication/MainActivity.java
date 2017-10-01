package hometech.com.myapplication;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private List<String[]> mScripData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort) {
            mSectionsPagerAdapter.onOptionsItemSelected(item);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter implements FragmentChangeListener {

        private ScripDetailFragment scripDetailFragment;
        private ScripListFragment scripListFragment;
        private ScripChartFragment scripChartFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            scripListFragment = new ScripListFragment();
            scripListFragment.setFragmentChangeListener(SectionsPagerAdapter.this);
            scripDetailFragment = new ScripDetailFragment();
            scripDetailFragment.setFragmentChangeListener(SectionsPagerAdapter.this);

            scripChartFragment = new ScripChartFragment();
            scripChartFragment.setFragmentChangeListener(SectionsPagerAdapter.this);

         }

        public void onScripDataUpdate(List<String[]> data){
            scripDetailFragment.setScripData(data);
            MainActivity.this.mScripData = data;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if ( position == 1) {
                return scripDetailFragment;
            }else if ( position == 0) {
                return scripListFragment;
            }else
                return scripChartFragment;//PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Watchlist";
                case 1:
                    return "Scrip Details";
                case 2:
                    return "Scrip Chart";
            }
            return null;
        }

        @Override
        public void onPageChangeRequest(int pageId, Map info) {
             if (pageId == 1){
                String scrip = (String)info.get("scrip");
                if (scrip!=null){
                    SharedPreferences settings = MainActivity.this.getSharedPreferences(Utils.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Utils.CURR_SCRIP, scrip);
                    editor.apply();
                }
            }else if ( pageId == 2 ){
                 String scrip = (String)info.get("scrip");
                 if (scrip!=null) {
                     scripChartFragment.updateSeriesData(scrip);
                 }
             }
            MainActivity.this.mViewPager.setCurrentItem(pageId);

        }

        public void onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_sort) {
                if ( MainActivity.this.mViewPager.getCurrentItem() == 0 ){
                    scripListFragment.sort();
                }
            }

        }
    }
}

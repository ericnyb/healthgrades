package com.ericbandiero.ratsandmice.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ericbandiero.librarymain.UtilsShared;
import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.AppUtility;
import com.ericbandiero.ratsandmice.DataProvider;
import com.ericbandiero.ratsandmice.HeaderSorter;
import com.ericbandiero.ratsandmice.HealthDataMainReceiver;
import com.ericbandiero.ratsandmice.LocationGetter;
import com.ericbandiero.ratsandmice.R;
import com.ericbandiero.ratsandmice.Reports;
import com.ericbandiero.ratsandmice.adapters.DataViewerAdapter;
import com.ericbandiero.ratsandmice.interfaces.IListenToItemSelected;
import com.ericbandiero.ratsandmice.interfaces.ISetUpData;
import com.ericbandiero.ratsandmice.parent.ParentActivity;

import java.util.ArrayList;
import java.util.List;

import healthdeptdata.HealthDataRestaurants;
import healthdeptdata.Inspections;
import healthdeptdata.MatchType;


public class SearchActivity extends ParentActivity implements ISetUpData, AdapterView.OnItemSelectedListener,IListenToItemSelected {


    private SearchView searchView;

    private Intent serviceToRun;

    private IntentFilter filter;

    public static MatchType matchtype;

    private ListView listViewData;

    //UI
    TextView textViewRestaurantName;
    TextView textViewRestaurantPhone;
    TextView textViewRestaurantAddress;

    RadioGroup radioGroupSearchMatch;
    CheckBox checkboxRespectFilter;

    private List<Inspections> listSearchData;

    private Spinner spinnerSearchField;

    private DataViewerAdapter dataViewerAdapter;

    private HeaderSorter headerSorter=new HeaderSorter();

    private Spinner spinnerFilters;

	private Drawable actionBarBackground;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        setTitle("Search");

		//Get color of action bar

        ActionBar supportActionBar = getSupportActionBar();
      //  supportActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00cfaa")));
      //  supportActionBar.setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
      //  supportActionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        textViewHeader=(TextView)findViewById(R.id.textheader);
        textViewLabels=(TextView)findViewById(R.id.textmainlabels);


        TextView textViewSpinnerSearchFieldHeader= (TextView) findViewById(R.id.textViewMainReportTitle);
        textViewSpinnerSearchFieldHeader.setText("Search by:");
        textViewSpinnerSearchFieldHeader.setTypeface(Typeface.create("monospace", Typeface.NORMAL));


       TextView textViewSpinnerGeographyFilterHeader= (TextView) findViewById(R.id.spinnerheadertest);
       textViewSpinnerGeographyFilterHeader.setText("Filter by:");
       textViewSpinnerGeographyFilterHeader.setTypeface(Typeface.create("monospace", Typeface.NORMAL));

//        tvSearch = (TextView) findViewById(R.id.textSearch);
//        tvSearch.setText("");

        radioGroupSearchMatch = (RadioGroup) findViewById(R.id.radio_search_match_type);
        //checkboxRespectFilter=(CheckBox)findViewById(R.id.checkbox_filter);
        //checkboxRespectFilter.setVisibility(View.GONE);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        int radio_selected_last = settings.getInt(AppConstant.RADIO_MATCH_SELECTED, R.id.radio_starts);

        //First time in this may not have anything -we will use starts with option
        if (radio_selected_last!=R.id.radio_starts & radio_selected_last!=R.id.radio_contains & radio_selected_last!=R.id.radio_exact){
            radio_selected_last=(R.id.radio_starts);
        }
        radioGroupSearchMatch.check(radio_selected_last);

        RadioButton rd=(RadioButton)radioGroupSearchMatch.getChildAt(0);

        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Button:"+rd.getPaddingLeft());

        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Padding:"+radio_selected_last);


//        checkboxRespectFilter.setChecked(settings.getBoolean(RESPECT_FILTER,false));
//        checkboxRespectFilter.setText(getString(R.string.checkBoxFilterRespect)+DataProvider.getHealthDataFilter().getFilterName().toString());
//        checkboxRespectFilter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                focusOnSearchBarAndOpenKeyboard();
//            }
//        });

        switch(radio_selected_last){
            case R.id.radio_starts:
                matchtype = MatchType.MATCH_NAME_STARTS_WITH;
                break;
            case R.id.radio_contains:
                matchtype = MatchType.MATCH_NAME_CONTAINS;
                break;
            case R.id.radio_exact:
                matchtype = MatchType.MATCH_NAME_EXACT;
                break;
        }

//        if (radio_selected_last == R.id.radio_starts) {
//            matchtype = MatchType.MATCH_NAME_STARTS_WITH;
//        } else {
//            matchtype = MatchType.MATCH_NAME_CONTAINS;
//        }

        spinnerSearchField = (Spinner) findViewById(R.id.restaurant_search_spinner);
        // Create an ArrayAdapter using the string array and a default spinnerSearchField layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.search_fields, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinnerSearchField
        spinnerSearchField.setAdapter(adapter);

        //See if we ever had anything selected in previous use.
        String previousFieldSelected=settings.getString(AppConstant.SEARCH_FIELD_SELECTED, "N/A");

        if (previousFieldSelected.equals("N/A")){
            spinnerSearchField.setSelection(0);
        }
        else{
            spinnerSearchField.setSelection(adapter.getPosition(previousFieldSelected));
        }
        spinnerSearchField.setOnItemSelectedListener(this);


         //New spinnerSearchField for filter by geography
        spinnerFilters = (Spinner) findViewById(R.id.spinner_reports_filter);
        AppUtility.setUpFilterGeographySpinner(this, spinnerFilters);


    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "We are in resume!");
        //=====This tells what broadcasts we want to receive.
        filter = new IntentFilter(AppConstant.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new HealthDataMainReceiver(this);
        this.registerReceiver(receiver, filter);
        handleIntent(getIntent());
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Post resume...");
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","On start");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Post create...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(AppConstant.RADIO_MATCH_SELECTED, radioGroupSearchMatch.getCheckedRadioButtonId());
       // editor.putBoolean(RESPECT_FILTER, checkboxRespectFilter.isChecked());
        editor.putString(AppConstant.SEARCH_FIELD_SELECTED, spinnerSearchField.getSelectedItem().toString());
        // Commit the edits!
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    //    /**
//     * This hook is called when the user signals the desire to start a search.
//     * <p/>
//     * <p>You can use this function as a simple way to launch the search UI, in response to a
//     * menu item, search button, or other widgets within your activity. Unless overidden,
//     * calling this function is the same as calling
//     * {@link #startSearch startSearch(null, false, null, false)}, which launches
//     * search for the current activity as specified in its manifest, see {@link android.app.SearchManager}.
//     * <p/>
//     * <p>You can override this function to force global search, e.g. in response to a dedicated
//     * search key, or to block search entirely (by simply returning false).
//     * <p/>
//     * <p>Note: when running in a {@link android.content.res.Configuration#UI_MODE_TYPE_TELEVISION}, the default
//     * implementation changes to simply return false and you must supply your own custom
//     * implementation if you want to support search.</p>
//     *
//     * @return Returns {@code true} if search launched, and {@code false} if the activity does
//     * not respond to search.  The default implementation always returns {@code true}, except
//     * when in {@link android.content.res.Configuration#UI_MODE_TYPE_TELEVISION} mode where it returns false.
//     * @see android.app.SearchManager
//     */
//    @Override
//    public boolean onSearchRequested() {
//        if (AppConfig.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "In search!");
//        return super.onSearchRequested();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "Creating menu");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);



        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		//MenuItem item = menu.findItem(R.id.search);

	//	if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Menu name:"+item.getIcon().getClass().getName());
		searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setVisibility(View.VISIBLE);
        searchView.setIconified(false);
       // searchView.setBackgroundColor(ContextCompat.getColor(this,R.color.White));
		//searchView.setIconifiedByDefault(false);

//		ImageView searchViewIcon = (ImageView)searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
//		ViewGroup linearLayoutSearchView =(ViewGroup) searchViewIcon.getParent();
//		linearLayoutSearchView.removeView(searchViewIcon);


	if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">",	searchView.toString());

        //searchView.setIconifiedByDefault(true);
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Searchxx");
        //Change the X to black (was white - so hidden on background)
        ImageView searchCloseIcon = (ImageView)searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);


        if(searchCloseIcon!=null) {
        //  searchCloseIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        }


   /*    searchView.setOnCloseListener(new SearchView.OnCloseListener() {
           @Override
           public boolean onClose() {

			   if (searchView.isIconified()) {
				   if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Iconified");
				   searchView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.Green));
			   }
			   else{
				   if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Not Iconified");
				   searchView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.LightSkyBlue));

				  // searchView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), actionBarBackground.));
			   }

               return false;
           }
       });
*/
        if (DataProvider.getListSearchDataSet().size()>0){
            setUpData();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.search_help) {
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Help!");
            AppUtility.helpActivityStart(this,item);
            return true;
        }

        //We want to set this to true so we don't get sent back here if default
        if ( id == android.R.id.home){
            MainActivity.userCameBackFromSearch =true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }



    //The user clicked on a result - we get data for it.
    private void getCamis(Inspections inspections) {

        //We want to pass in the previous report - will be used in analytics
        Intent intent = new Intent(this, InspectionActivity.class);
        intent.putExtra("CAMIS", inspections.getCamis());
        intent.putExtra("DBA", inspections.getDba());

        startActivity(intent);

        //Need to pass this in as bundle - just a cipher
        Reports r = new Reports();
        r.setReportName(Reports.GET_BY_CAMIS);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, r);

        Reports.setLastReportRun(r);

        Intent serviceToRun2;
        serviceToRun2 = new Intent(this, DataProvider.class);
       // serviceToRun2.putExtra("DataToGet", "CAMIS");
        serviceToRun2.putExtra("CAMIS", inspections.getCamis());
        //Maybe get rid of this and just use the data to get
        serviceToRun2.putExtras(bundle);

        startService(serviceToRun2);

    }

    @Override
    public void setUpData() {
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Setting up data...");
        if (progressDialog!=null) {
            progressDialog.dismiss();
        }

        this.searchView.clearFocus();

        ListView listViewHeader = (ListView) findViewById(R.id.listViewMainHeader);

        listViewData = (ListView) findViewById(R.id.listViewMainData);

        listSearchData =  new ArrayList<>(DataProvider.getListSearchDataSet());

        textViewHeader.setText("Records that matched: " + listSearchData.size());

        if (!DataProvider.errorMessage.isEmpty()) {
            if (DataProvider.errorMessage.toLowerCase().equals("out of memory")) {
                textViewHeader.setText("No results - we had an error: " + DataProvider.errorMessage + " - try a different filter or search that returns less results.");
            } else {
                textViewHeader.setText("No results - we had an error: " + DataProvider.errorMessage);
            }
        }
        dataViewerAdapter = new DataViewerAdapter(this, R.layout.view_row_items_dataview_new, listSearchData);

        DataViewerAdapter dataViewerAdapterColumns=new DataViewerAdapter(this,R.layout.view_row_items_dataview_columns);

        listViewHeader.setAdapter(dataViewerAdapterColumns);

        listViewData.setAdapter(dataViewerAdapter);
    }

    /*
       We are using this because the onClick gets blocked by descendants with click events (phone)
        */
    public void onClickItemLayout(View view) {
        AppUtility.getCamis(this,view,listViewData);
    }

    public final void onRadioButtonClicked(View view) {

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "view in on clicked:" + view.getClass().getName());

        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_starts:
                if (checked)
                    matchtype = MatchType.MATCH_NAME_STARTS_WITH;
                break;
            case R.id.radio_contains:
                if (checked)
                    matchtype = MatchType.MATCH_NAME_CONTAINS;
                break;
            case R.id.radio_exact:
                if (checked)
                    matchtype = MatchType.MATCH_NAME_EXACT;
                break;
        }
        focusOnSearchBarAndOpenKeyboard();
    }

    private void focusOnSearchBarAndOpenKeyboard() {
		if (searchView != null) {
			searchView.requestFocus();
			searchView.setIconified(false);
		}
	}

    private void handleIntent(Intent intent) {

       // if (AppConfig.DEBUG) Log.i(this.getClass().getSimpleName() + ">", spinnerSearchField.getSelectedItem().toString());

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            //This was being kept so non violation reports were still marking these...
            HealthDataRestaurants.setViolationCodesArray(null);

            String query = intent.getStringExtra(SearchManager.QUERY);

            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "We had a search!");
            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "Query:"+query);
            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "Query length:"+query.toString().trim().length());

            String searchForItem= spinnerSearchField.getSelectedItem().toString().toLowerCase();

            if (matchtype.equals(MatchType.MATCH_NAME_CONTAINS)
                    &(searchForItem.toLowerCase().equals("name")|(searchForItem.toLowerCase().equals("street name")))
                    & query.toString().trim().length()<2)
            {
                UtilsShared.toastIt(AppConstant.getApplicationContextMain(), "You need at least two characters for this search when using contains...", Toast.LENGTH_LONG);
                return;
            }

            if (spinnerFilters.getSelectedItem()!=null){
                if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Spinner selected in Search Activity:"+spinnerFilters.getSelectedItem().toString());
                if (spinnerFilters.getSelectedItem().toString().contains(AppConstant.CURRENT_ZIP_CODE_FILTER_NAME.toString())) {
                    try {
                        if(!LocationGetter.getLocation(this)){
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(this.getClass().getSimpleName()+">","Location getter tossed error:"+e.getMessage().toString());
                    }
                }
            }

            Reports r = new Reports();
            Bundle bundle = new Bundle();
            serviceToRun = new Intent(this, DataProvider.class);

            //Unless a sepcial situation, we always use filter
            serviceToRun.putExtra(DataProvider.STRICT_SEARCH_MUST_USE_FILTER,true);


            switch (searchForItem){
                case "name":
                    //Need to pass this in as bundle - just a cipher
                    //Used in DataProvider case statement to know what data to get.
                    r.setReportName(Reports.DBA_SEARCH);
                    bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, r);
                    break;
                case "building #":
                    //Need to pass this in as bundle - just a cipher
                    //Used in DataProvider case statement to know what data to get.
                    r.setReportName(Reports.FIELD_SEARCH);
                    bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, r);
                    serviceToRun.putExtra("search_query",query);
                    serviceToRun.putExtra("field","building");
                    break;
                case "street name":
                    //Need to pass this in as bundle - just a cipher
                    //Used in DataProvider case statement to know what data to get.
                    r.setReportName(Reports.FIELD_SEARCH);
                    bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, r);
                    serviceToRun.putExtra("search_query",query);
                    serviceToRun.putExtra("field","street");
                    break;
                case "phone #":
                    //Need to pass this in as bundle - just a cipher
                    //Used in DataProvider case statement to know what data to get.
                    r.setReportName(Reports.FIELD_SEARCH);
                    bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, r);
                    serviceToRun.putExtra("search_query",query);
                    serviceToRun.putExtra("field","phone");
                    break;
                case "cuisine":
                    //Need to pass this in as bundle - just a cipher
                    //Used in DataProvider case statement to know what data to get.
                    r.setReportName(Reports.FIELD_SEARCH);
                    bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, r);
                   // serviceToRun.putExtra("cuisine",query);
                    serviceToRun.putExtra("search_query",query);
                    serviceToRun.putExtra("field","cuisine_description");
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Query:"+query);
                    break;
                case "violation":
                    //Need to pass this in as bundle - just a cipher
                    //Used in DataProvider case statement to know what data to get.
                    r.setReportName(Reports.FIELD_SEARCH);
                    bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, r);
                    serviceToRun.putExtra("search_query",query);
                    serviceToRun.putExtra("field","violation_description");
                    break;
                case "zip code":
                    //Need to pass this in as bundle - just a cipher
                    //Used in DataProvider case statement to know what data to get.
                    //Can't have any other filter if searching by zip code - doesn't make sense.
                    r.setReportName(Reports.FIELD_SEARCH);
                    bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, r);
                    serviceToRun.putExtra("search_query",query);
                    serviceToRun.putExtra("field","zipcode");
                    serviceToRun.putExtra(DataProvider.STRICT_SEARCH_MUST_USE_FILTER,false);
                    break;
            }


            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Last known zipcode:"+AppUtility.getLastKnownZipCode());

            Reports.setLastReportRun(r);
            //TODO default in above case


            //todo - is this used?
            serviceToRun.putExtra("DataToGet", "CAMIS");

            serviceToRun.putExtra(DataProvider.SEARCH_STRING_PASSED_IN, query);
            //Maybe get rid of this and just use the data to get
            serviceToRun.putExtras(bundle);

            this.startService(serviceToRun);

            progressDialog = ProgressDialog.show(this, null,
                    getResources().getString(R.string.progess_dialog), true);

            progressDialog.show();


            //searchView.setIconified(true);
            // searchView.setIconified(true);
            //use the query to search your data somehow
        } else {
            if (AppConstant.DEBUG)
                Log.i(this.getClass().getSimpleName() + ">", "Intent was not search!");
        }

    }

    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p/>
     * Impelmenters can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","view:"+parent.getId());

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Item selected:"+parent.getItemAtPosition(position).toString());
        ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.app_color_spinner_filter_selected_item));
        ((TextView) parent.getChildAt(0)).setTextSize(14);
        radioGroupSearchMatch.setVisibility(!parent.getItemAtPosition(position).toString().toLowerCase().equals("cuisine")?View.VISIBLE:View.INVISIBLE);
        focusOnSearchBarAndOpenKeyboard();
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void itemSelectedListenCustom(AdapterView adapterView) {
        focusOnSearchBarAndOpenKeyboard();
    }

    public void headerClicked(View view) {
        headerSorter.sortHeader(view, listSearchData, dataViewerAdapter);
    }

    public void onClickPhoneTextView(View view){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean userWantsToDial = sharedPref.getBoolean(getResources().getString(R.string.pref_key_phone_dial), false);

        if (!userWantsToDial){
            return;
        }
        String phoneNumber=((TextView) view).getText().toString();
//        View parent = (View)view.getParent().getParent();
//        TextView txtView=null;
//        if (parent != null) {
//            txtView = (TextView)parent.findViewById(R.id.txt_dba);
//            System.out.println(txtView.getText());
//        }

        if (phoneNumber!=null && phoneNumber.trim().length()>4){
            String uri = "tel:" +phoneNumber ;
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        }
    }
}

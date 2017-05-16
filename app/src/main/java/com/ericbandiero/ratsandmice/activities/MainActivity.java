package com.ericbandiero.ratsandmice.activities;



//TODO phone number dial
//TODO Fix up filters - add and delete
//TODO  Add to analyzer

//TODO Gut feeling - look at havana cafe bronx - yellow is right - but they never got an A - score usually C.
//TODO stats meta data
//Boro by boro, ect
//Year by year camis
//Year by year food types
//Newest food types
//percent closed by boro - toughest?

//TODO App name change?
//TODO Help topics
//TODO check gut feeling genesis bar, szechuan garden
//TODO Data integrity
//Data missing by field
//TODO Filters are help in static list in health data restaurants...not so good?
/*
TODO When searching by building starts with isn't good - need to make it exact.
    i.e. if the building is 7 cornelia you can't just find 7,it brings 77, 709,72, etc.
      can't use a space - then nothing comes back.
*/

//TODO Need street search

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ericbandiero.librarymain.UtilsShared;
import com.ericbandiero.myframework.Utility;
import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.AppUtility;
import com.ericbandiero.ratsandmice.DataProvider;
import com.ericbandiero.ratsandmice.HealthDataMainReceiver;
import com.ericbandiero.ratsandmice.LocationGetter;
import com.ericbandiero.ratsandmice.PreferenceUtility;
import com.ericbandiero.ratsandmice.R;
import com.ericbandiero.ratsandmice.Reports;
import com.ericbandiero.ratsandmice.SettingsActivity;
import com.ericbandiero.ratsandmice.adapters.MainActivityExpandAdapter;
import com.ericbandiero.ratsandmice.interfaces.ISetUpData;
import com.ericbandiero.ratsandmice.parent.ParentActivity;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import healthdeptdata.HealthDataFilter;
import healthdeptdata.HealthDataRestaurants;

public class MainActivity extends ParentActivity implements ISetUpData,AdapterView.OnItemSelectedListener, Serializable {

    private static boolean alerted_user_to_install_maps=false;

    private final Bundle bundleHoldingParcelReport = new Bundle();

    //New ======================
    private ExpandableListView expListView;
    private final List<Reports> repHeader = new ArrayList<>();

    private final HashMap<Reports, List<Reports>> repChildren = new HashMap<>();
    //=========================

    //Used for starting application with Search activity.
    public static boolean userCameBackFromSearch = false;
    //Utility class

    //Last inspection date
    private TextView textViewLastInspection;

    //Main adapter
    private MainActivityExpandAdapter mainExpandAdapter;

    //private LocationListener locationListener;

    private Spinner spinnerFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fetchMessages=true;
        super.onCreate(savedInstanceState);

        if (savedInstanceState!=null){
          if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Saved bundle:"+savedInstanceState.toString());
        }

        //We ask here because location can be asked from search as well.
       askForLocationPermission();

        if(!alerted_user_to_install_maps){
            checkIfMapsInstalled();
        }

        System.out.println("Name:"+UtilsShared.getAppLabel(this));

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","In main create!");
        setContentView(R.layout.activity_main_expand);

        //getSupportActionBar().setTitle("Eatery Grades NYC");

        textViewLastInspection=(TextView)findViewById(R.id.textheader);

        if (AppConstant.applicationContextMain==null) {
            AppConstant.applicationContextMain = getApplicationContext();
        }

        //setContentView(R.layout.activity_main_expand);

        expListView = (ExpandableListView) findViewById(R.id.ExpandMainScreenReports);
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Date in YYYYMMDD format:"+Utility.dateTodayYYYYMMDD());

        //Reports.loadMainExpandData(repHeader, repChildren);

        //final MainActivityExpandAdapter mainExpandAdapter = new MainActivityExpandAdapter(this, repHeader, repChildren);
        mainExpandAdapter = new MainActivityExpandAdapter(this, repHeader, repChildren);

        expListView.setAdapter(mainExpandAdapter);

        //Get rid of group indicator for now
        expListView.setGroupIndicator(null);

        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int itemType = ExpandableListView.getPackedPositionType(id);
                final int childPosition;


                if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Position:"+position);
                if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Id:"+id);


                 long packedPosition = ((ExpandableListView) parent).getExpandableListPosition(position);

                if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Packed Position:"+packedPosition);

                final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

                if ( itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    childPosition = ExpandableListView.getPackedPositionChild(id);
                    //groupPosition = ExpandableListView.getPackedPositionGroup(id);

                    if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Child:"+childPosition);
                    if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Group:"+groupPosition);

                    final Reports r = (Reports) mainExpandAdapter.getChild(groupPosition, childPosition);

                    if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Report:"+r.toString());
                    //do your per-item callback here

                    ///This is to remove the child from view

                    //We only delete user reports
                    if (r.getReportId()== Reports.REPORTS_USER_REPORT_ID) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        PreferenceUtility.deleteUserViolationReports(r.getReportDesc());
                                        mainExpandAdapter.removeChildItem(groupPosition,childPosition);
//                                        finish();
                                        //startActivity(getIntent());
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setMessage("Delete saved report " + r.getReportName() + "?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();

                    }

                    return true; //true if we consumed the click, false if not

                } else
                    //groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    //do your per-group callback here
                    //true if we consumed the click, false if not
                    // null item; we don't consume the click
                    return itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP;
            }});

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (AppConstant.DEBUG)
                    Log.d(this.getClass().getSimpleName() + ">", "Group position:" + groupPosition);

                Reports r = (Reports) mainExpandAdapter.getGroup(groupPosition);


                if (r.getReportName().equals(Reports.FAVORITE_RESTAURANTS)&!PreferenceUtility.checkThatWeHaveFavorites()){
                    return false;
                }

                if (AppConstant.DEBUG)
                    Log.i(this.getClass().getSimpleName() + ">","Report name:"+r.getReportName());
                if (r.getReportId() > 0) {
                    runReports(r);
                }
                return false;
            }
        });

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (AppConstant.DEBUG)
                    Log.i(this.getClass().getSimpleName() + ">", "Child click:" + childPosition);
                Reports r = (Reports) mainExpandAdapter.getChild(groupPosition, childPosition);
                if (AppConstant.DEBUG)
                    Log.i(this.getClass().getSimpleName() + ">", r.getReportName());
                runReports(r);
                return false;
            }
        });

        //List<HealthDataFilter> allFilters;
        //allFilters = new HealthDataRestaurants().getAllFilters();

        spinnerFilters = (Spinner) findViewById(R.id.spinner_reports_filter);



        AppUtility.setSavedSortOrder();

     //   ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, allFilters);

       // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       // spinnerFilters.setAdapter(adapter);
       // spinnerFilters.setOnItemSelectedListener(this);
        //spinnerFilters.setSelection(AppUtility.getLastFilterInDataProvider(this));
        //AppUtility.setLastFilterInDataProvider(getApplicationContext());

        //Get last inspection date
        if (DataProvider.getListMainDataSet().isEmpty()) {
            getLastInspectionDate();
        }
        else{
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            String stringLastInspection = sharedPref.getString("last inspection date", "N/A");
            textViewLastInspection.setText(stringLastInspection);
        }

        //This does nothing - just for debugging.
        PreferenceUtility.showAllDefaultSavedPreferences(this);

    }

    private void checkIfMapsInstalled() {

        boolean googleMapsInstalled = UtilsShared.isGoogleMapsInstalled(getApplicationContext());

        AppUtility.isMapProviderInstalled=googleMapsInstalled;

        //We don't want to keep asking
        alerted_user_to_install_maps=true;

        if (!googleMapsInstalled){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This app uses maps - please install Google Maps");
            builder.setCancelable(true);
            builder.setNegativeButton("Ignore",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
               //TODO Keep as preference - for now toast
                    UtilsShared.toastIt(AppConstant.getApplicationContextMain(),"Maps function will be disabled in app.", Toast.LENGTH_LONG);
                }
            });
            builder.setPositiveButton("Install", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));

                    // Verify that the intent will resolve to an activity
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    else{
                        UtilsShared.toastIt(AppConstant.getApplicationContextMain(),"Cannot install Google Maps from this app - you need to install it manually.", Toast.LENGTH_LONG);
                    }

                   // startActivity(intent);
                  //Finish the activity so they can't circumvent the check
                 //   finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    //TODO Make public - always run reports from here - no place else like in search
    private void runReports(Reports reportToRun) {

/*
        if (1==1){
            ReportRunner reportRunner=new ReportRunner();
            reportRunner.runReport(reportToRun,MainActivity.this,spinnerFilters);
            return;
        }
*/

        if(!AppUtility.checkNetwork()){
            return;
        }

        Intent intent = new Intent();
        Context context = getApplicationContext();

        //We want to make sure location is on
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Selected spinner:"+(spinnerFilters.getSelectedItem()!=null?spinnerFilters.getSelectedItem():"Null"));

        //This was being kept so non violation reports were still marking these...
        HealthDataRestaurants.setViolationCodesArray(null);

        if (spinnerFilters.getSelectedItem()!=null){
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Spinner selected in Main Activity:"+spinnerFilters.getSelectedItem().toString());
            if (spinnerFilters.getSelectedItem().toString().contains(AppConstant.CURRENT_ZIP_CODE_FILTER_NAME)) {
                try {
                    if(!LocationGetter.getLocation(this)){
                        return;
                    }
                } catch (Exception e) {
                   // e.printStackTrace();
                    Log.e(this.getClass().getSimpleName()+">","Location getter tossed error:"+ e.getMessage());
                }
            }
        }

        //Some reports are just for internal use - we don't need to show them in an activity class.

            //We want to check this and make a switch on the run.
            //Violation reports
            if (reportToRun.getReportId() == Reports.REPORTS_VIOLATION_CODES_QUERY) {
                ViolationsActivity.aViolationCodes = reportToRun.getCodeArray();
            }

            if (AppConstant.DEBUG)
                Log.i(this.getClass().getSimpleName() + ">", "Report we are going to run:" + reportToRun.getReportName());

            bundleHoldingParcelReport.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, reportToRun);

            //Store this so other activities can use it
            Reports.setLastReportRun(reportToRun);

            Intent serviceToRun = new Intent(context, DataProvider.class);
            //Switch for which activity to run
            //TODO Clean this up - give them an ID so we know what activity they use
            switch (reportToRun.getReportName()) {
                //This is when user is looking up a violation to run report on.
                case Reports.VIOLATIONS_USER_CREATE:
                    intent.setClass(context, ViolationsActivity.class);
                    // intent.putExtra("LOOKUP")
                    break;
                case Reports.VIOLATIONS_USER_FLAG_SET:
                    intent.setClass(context, ViolationsActivity.class);
                    break;
                case Reports.META_CUISINES_BY_BORO:
                    intent.setClass(context, ViolationsActivity.class);
                    break;
                case Reports.META_CUISINES_BY_FILTER:
                    intent.setClass(context, ViolationsActivity.class);
                    break;
                case Reports.META_CHAINS:
                    intent.setClass(context, ViolationsActivity.class);
                    break;
                    //serviceToRun.putExtra()
                case Reports.META_RESTAURANTS_BY_BORO:
                    intent.setClass(context, ViolationsActivity.class);
                    break;
                case Reports.META_RESTAURANTS_BY_FILTER:
                    intent.setClass(context, ViolationsActivity.class);
                    break;
                case Reports.META_RESTAURANTS_BY_ZIP_CODE:
                    intent.setClass(context, ViolationsActivity.class);
                    break;
                default:
                    intent.setClass(context, DataViewerActivity.class);
            }

            //We pass along  the report as a bundle in almost all cases
            if (reportToRun.getReportId()!= Reports.REPORTS_NO_DATA_ACTIVITY_NEEDED) {
                intent.putExtras(bundleHoldingParcelReport);
                intentRunner(intent);
            }


        serviceToRun.putExtras(bundleHoldingParcelReport);
        context.startService(serviceToRun);
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
        //We do not have this set as single instance in manifest, so we come back here instead of create.
        //Would keep us from having to re-create fields.
        //On issue is getIntent always returns the same as first time.
        //We start this from filterActivity - no way to pass in an extra if single instance




        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "In resume!");
        DataViewerActivity.clearPreviousData();
        //Clear data from DataProvider
        DataProvider.getListDataSet().clear();
        DataProvider.getListSearchDataSet().clear();

        //TODO Come back to this one see above comments on single instance
        //Right now this will always be the case because activity always gets restarted
        if (spinnerFilters.getAdapter()==null) {
            AppUtility.setUpFilterGeographySpinner(this, spinnerFilters);
            //noinspection StatementWithEmptyBody
            if (spinnerFilters.getSelectedItem()!=null&&spinnerFilters.getSelectedItem().toString().contains(AppConstant.CURRENT_ZIP_CODE_FILTER_NAME)){
                //LocationGetter.getLocation(this);
            }
        }

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Spinner:"+spinnerFilters.getSelectedItem().toString());


        //AppConfig.deleteAllUserViolationReports();

        //Just shows us the zip filters - does nothing
        //PreferenceUtility.getAllZipFilters();

        //We look to see if search is default start activity
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean searchWasSetAsDefaultStartActivity = sharedPref.getBoolean(getResources().getString(R.string.pref_key_search_screen), false);

        //If search is default start activity we
        if (searchWasSetAsDefaultStartActivity) {
            //If we did this at startup and user cam back here we don't want to go back in loop.
            if (!userCameBackFromSearch) {
                goToSearchActivity();
            }
        }


        loadReportsData();

    }

    public void loadReportsData(){
        //TODO temp?
        repHeader.clear();
        repChildren.clear();



        Reports.loadMainExpandData(repHeader, repChildren);

        //Only allow this if user is using current filter!
        removeMenuItem();

    }

    private void removeMenuItem() {
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Current selected menu:"+spinnerFilters.getSelectedItem().toString());
        if (!(spinnerFilters.getSelectedItem().toString().contains(AppConstant.CURRENT_ZIP_CODE_FILTER_NAME))) {

            //TODO Finish this to remove if filter isn't one zip code
            List<Reports> reports = repChildren.get(new Reports(-5));
            int positionToRemove = -1;
            int c = 0;
            for (Reports report : reports) {
                if (report.getReportName().equals(Reports.ALL_PLACES_FOR_ONE_ZIP_CODE)) {
                    positionToRemove = c;
                }
                c++;
            }
            repChildren.get(new Reports(-5)).remove(positionToRemove);
        }
        mainExpandAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        String resourceEntryName = getResources().getResourceEntryName(item.getItemId());

        if (id == R.id.action_settings) {
            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "Search selected");
            //We have the manifest letting us do this - but it doesn't look good.
            //onSearchRequested();

            Intent intent = new Intent(this, SettingsActivity.class);

            startActivity(intent);

            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            if (!AppUtility.checkNetwork()){
                return true;
            }
            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "Search selected");
            //We have the manifest letting us do this - but it doesn't look good.
            //onSearchRequested();

            goToSearchActivity();

            return true;
        }

        if(id==R.id.Filter_Add){
            return userAddZipFilter();
        }

        if (id == R.id.Filter) {
            goToFilterActivity();
        }

        if (id == R.id.Violation_Flagger) {
            Reports r=new Reports(Reports.VIOLATIONS_USER_FLAG_SET,"",Reports.REPORT_ID_GENERIC,false);
            runReports(r);
        }

        if (resourceEntryName.contains("_help")){
            AppUtility.helpActivityStart(this,item);
            //Temp test
            //ReportFieldBuilder reportFieldBuilder=new ReportFieldBuilder("African","cuisine_description","African Cuisine",getApplicationContext());
            //reportFieldBuilder.runFieldReport();
        }

   /*
        if (id == R.id.search_help) {
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Help!");
            AppUtility.helpActivityStart(this,id);
            return true;
        }

        if (id==R.id.about_help){
          //  Intent intent = new Intent(this, HelpActivity.class);
           // intent.putExtra("menu",id);
          //  startActivity(intent);
            AppUtility.helpActivityStart(this,id);
        }

        if (id==R.id.help_help){
//            Intent intent = new Intent(this, HelpActivity.class);
//            intent.putExtra("menu",id);
//            startActivity(intent);
            AppUtility.helpActivityStart(this,id);
        }

        if (id==R.id.gut_help){
//            Intent intent = new Intent(this, HelpActivity.class);
//            intent.putExtra("menu",id);
//            startActivity(intent);
            AppUtility.helpActivityStart(this,id);
        }
*/
        return super.onOptionsItemSelected(item);
    }

    private boolean userAddZipFilter() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.fragment_save_data_dialog, null);

        //Set up
        final EditText editTextZipCodes= (EditText) promptsView.findViewById(R.id.user_report_name2);
        //TextView textViewZipCodes=(TextView) promptsView.findViewById(R.id.lbl_enter_zip_Code);

        TextView textViewName=(TextView) promptsView.findViewById(R.id.lbl_your_name);
        textViewName.setText("Enter new filter name:");


        //We don't need to see these
        //editTextNotUSed.setVisibility(View.GONE);
        // textViewZipCodes.setVisibility(View.GONE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(promptsView);

        // final EditText userInput= (EditText) builder.getContext().getResources().getLayout(R.id.user_report_name);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.user_report_name);


        //TODO Temp location test
        //editTextZipCodes.setHint(AppUtility.getLastKnownZipCode().toString());

        // Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", userInput.getText().toString());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(userInput.getWindowToken(), 0);
                onSaveDataDialog(userInput.getText().toString(),editTextZipCodes.getText().toString());

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(userInput.getWindowToken(), 0);
            }
        });


//            TextView title = new TextView(this);
//            title.setText("Save Report");
//           // title.setId(Integer.valueOf(100));
//            title.setPadding(10, 10, 10, 10);
//            title.setGravity(Gravity.CENTER); // this is required to bring it to center.
//            title.setTextSize(22);
//            title.setBackgroundColor(getResources().getColor(R.color.Yellow));
//            title.setTextColor(getResources().getColor(R.color.Black));
        builder.setCustomTitle(AppUtility.getTextDialogTitle(this, "Create New Zip Code Filter"));
        //builder.setTitle("Save Report");


        final AlertDialog dialog = builder.create();


        userInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","In listener");
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });


        dialog.show();
        userInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        return true;
    }

    private void goToSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    private void goToFilterActivity() {
        Intent intent = new Intent(this, FilterActivity.class);
        startActivity(intent);
    }

    private  void  intentRunner(Intent intent) {
        //Right now this is always the DataViewerActivity.class
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Intent:"+intent.getComponent().getShortClassName().replace(".",""));
        intent.putExtra("STARTED_FROM_MAIN_ACTIVITY", "TRUE");
        //  intent.putExtras(bundleHoldingParcelReport);
        // startActivity(intent);


        switch (Reports.getLastReportRun().getReportName()) {
            case Reports.VIOLATIONS_USER_CREATE:
                startActivityForResult(intent, ViolationsActivity.REQUEST_CODE);
                break;
            case Reports.VIOLATIONS_USER_FLAG_SET:
                startActivityForResult(intent, ViolationsActivity.REQUEST_CODE_VIOLATION_FLAG);
                break;
            default:
                startActivity(intent);
                break;
        }
    }

    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p/>
     * Implementors can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Spinner selection
        //This only changes the color of the selected item. Keeps drop downs the same.
      //  ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.app_color_spinner_filter_selected_item));
        ((TextView) parent.getChildAt(0)).setTextColor(ContextCompat.getColor(this,R.color.app_color_spinner_filter_selected_item));
        ((TextView) parent.getChildAt(0)).setTextSize(14);
        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Item selected!" + parent.getItemAtPosition(position
            ).toString());
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "Position:" + position);
        DataProvider.setHealthDataFilter((HealthDataFilter) parent.getItemAtPosition(position));
        PreferenceUtility.saveLastFilterName(this);
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

    /**
     * Called from violations screen for user selected violation.
     * @param requestCode Special code so we know what to look for
     * @param resultCode Did we have success
     * @param data Data sent back to use
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Result code:"+resultCode);

        if (resultCode!=RESULT_OK){
            return;
        }

        // Check which request we're responding to
        if (requestCode == ViolationsActivity.REQUEST_CODE) {
            DataProvider.getListDataSet().clear();
            if (AppConstant.DEBUG)
                Log.i(this.getClass().getSimpleName() + ">", "Violation chosen:" + data.getStringExtra(ViolationsActivity.REQUEST_MESSAGE));

            Reports report=new Reports();
            report.setReportName(Reports.USER_SELECTED_VIOLATIONS_SEARCH);

            //Look for first period in violation description
            int indexOfPeriod=ViolationsActivity.VIOLATION_CODE_DESC.indexOf(".");
            // See if there are at least 10 words - return -1 if not
            int ordinalIndexOf = StringUtils.ordinalIndexOf(ViolationsActivity.VIOLATION_CODE_DESC, " ",10);
            //We show either 50 characters or 10 words
            int endPoint=(indexOfPeriod>50?50:indexOfPeriod);
            report.setReportDesc(ViolationsActivity.VIOLATION_CODE_DESC.substring(0, ordinalIndexOf>-1?ordinalIndexOf:endPoint));
            runReports(report);
        }

        //User had selected a codes to always flag
        if (requestCode == ViolationsActivity.REQUEST_CODE_VIOLATION_FLAG) {
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","requestCode = " + requestCode);
            String [] codes=ViolationsActivity.aViolationCodes;
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","codes to save as flagged:"+Arrays.asList(codes).toString());
            PreferenceUtility.saveUserViolationsToMark(new HashSet<>(Arrays.asList(codes)));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","In main pause!");
        //We want to save our


    }



    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     * <p/>
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}.
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     * @see #onCreate
     * @see #onPostCreate
     * @see #onResume
     * @see #onSaveInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","In restoredInstanceState");
           super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void setUpData() {
        if (!DataProvider.getListMainDataSet().isEmpty()) {
            textViewLastInspection.setText("Last inspection date:" + AppConstant.DATE_FORMAT_HEADER.format(DataProvider.getListMainDataSet().get(0).getInspection_date()));
        }

        //We want to save the last date - we don't want to always run that data request - we re-use value
        SharedPreferences settings =  this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = settings.edit();
        edit.putString("last inspection date",textViewLastInspection.getText().toString());
        edit.apply();

        //We got message from broadcast
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Going to unregister receiver");
        unRegisterOurReceiver();

    }


    private void getLastInspectionDate(){
if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Getting last inspection date...");
//        if (!utilsShared.checkConnection(this.getBaseContext())){
//            return;
//        }

        //=====This tells what broadcasts we want to receive.
        IntentFilter filter = new IntentFilter(AppConstant.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        //receiver = new HealthDataReceiver();
        receiver = new HealthDataMainReceiver(this);
        this.registerReceiver(receiver, filter);
        //Get last inspection date
        Reports r=new Reports(Reports.LAST_INSPECTION_DATE,"", Reports.REPORTS_NO_DATA_ACTIVITY_NEEDED,true);
        runReports(r);
    }

/*

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
*/

    private void onSaveDataDialog(String userReportName, String zipCodes) {
        //throw new UnsupportedOperationException("Not yet ready!");
        if (!userReportName.isEmpty() && !zipCodes.isEmpty()) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppConstant.getApplicationContextMain());
            SharedPreferences.Editor editor = prefs.edit();
            Set<String>setZipCodes=new HashSet<>();

            String [] zipArray =zipCodes.split(",");

            Collections.addAll(setZipCodes, zipArray);

            editor.putStringSet(AppConstant.USER_PREF_ZIP_FILTER_PREFIX + userReportName.toUpperCase(), setZipCodes);
            editor.apply();
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","setZipCodes.toString() = " + setZipCodes.toString());
            AppUtility.setUpFilterGeographySpinner(this,spinnerFilters);
        }
        else{
            UtilsShared.toastIt(AppConstant.getApplicationContextMain(),"Both fields must be filled in!",Toast.LENGTH_SHORT);
        }
        //TODO Give an error message
    }
    private void askForLocationPermission(){
        //Check for permissions
        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("To use the 'Current Zip Code' filter you will need to allow location permission access.");
                builder.setTitle("Location Services");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, 0);
                    }
                });

                builder.show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 0);
            }
        }
    }
}

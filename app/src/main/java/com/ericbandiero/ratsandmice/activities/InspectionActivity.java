package com.ericbandiero.ratsandmice.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ericbandiero.librarymain.UtilsShared;
import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.AppUtility;
import com.ericbandiero.ratsandmice.DataProvider;
import com.ericbandiero.ratsandmice.GutFeeling;
import com.ericbandiero.ratsandmice.HealthDataMainReceiver;
import com.ericbandiero.ratsandmice.InspectionSorterPlus;
import com.ericbandiero.ratsandmice.MarkViolations;
import com.ericbandiero.ratsandmice.PreferenceUtility;
import com.ericbandiero.ratsandmice.R;
import com.ericbandiero.ratsandmice.Reports;
import com.ericbandiero.ratsandmice.adapters.InspectionExpandAdapter;
import com.ericbandiero.ratsandmice.interfaces.ISetUpData;
import com.ericbandiero.ratsandmice.parent.ParentActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import healthdeptdata.DataAnalyzerSingleCamis;
import healthdeptdata.HealthDataRestaurants;
import healthdeptdata.InspectionSorts;
import healthdeptdata.Inspections;


public class InspectionActivity extends ParentActivity implements ISetUpData,ActionMode.Callback {

    private IntentFilter filter;
    private TextView textViewHeader;

    private final List<Inspections> dataListLastInspection = new ArrayList<>();

    //UI
    private TextView textViewRestaurantName;
    private TextView textViewRestaurantPhone;
    private TextView textViewRestaurantAddress;
    private TextView textViewRestaurantCuisine;
    private TextView textGutFeeling;

    private final List<Inspections> dataList = new ArrayList<>();

    private final List<String> listOfInspectionClosures = new ArrayList<>();

    //New for expandable listView
    private InspectionExpandAdapter inspectionExpandAdapterData;
    private InspectionExpandAdapter inspectionExpandAdapterColumns;

    private ExpandableListView expListView;
    private ExpandableListView expListViewForColumns;

    //Data for expandable listView
    private List<Inspections> listDataHeader;
    private List<Inspections> listInspectionByDate = new ArrayList<>();
    private SortedMap<Inspections, List<Inspections>> sortedMapChildren;


    //Sent in as extras
    private String camis;
    private String dba;
    private boolean favorites;

    //Set up colors
    private  int colorOk;
    private  int colorNo;
    private  int colorMaybe;
    private int gutFeelingDefaultTextColor;
    private int gutFeelingNoTextColor;


    private int favorite_selected_color;
    //Will be what it is naturally
    private int favorite_default_color;

    private DataAnalyzerSingleCamis dataAnalyzerSingleCamis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (AppConstant.DEBUG)
                Log.i(this.getClass().getSimpleName() + ">", "Info on saved instance state:" + savedInstanceState.toString());
        }




        setContentView(R.layout.activity_inspection);

        textGutFeeling = (TextView) findViewById(R.id.textview_gut_feeling);

        //We will change this color based on our analytics
        textGutFeeling.setBackgroundColor(Color.WHITE);
        //String defaultGutFeelingText = textGutFeeling.getText().toString();
        gutFeelingDefaultTextColor= ContextCompat.getColor(this,R.color.app_color_gut_default_text_color);
        gutFeelingNoTextColor=ContextCompat.getColor(this,R.color.app_color_gut_no_text_color);


        favorite_selected_color=ContextCompat.getColor(this,R.color.app_color_favorite_is_true);
        favorite_default_color=ContextCompat.getColor(this,R.color.app_color_favorite_is_false);

      //  Reports reportLastRun = Reports.getLastReportRun();

//        if (AppConfig.DEBUG)
//            Log.i(this.getClass().getSimpleName() + ">", "Last report run information:" + Reports.getLastReportRun().toString());

        //We could do it this way - but below looks cleaner and only do lookup once
        //textViewRestaurantName=(TextView)findViewById(R.id.inc_inspection_restaurant_header).findViewById(R.id.textViewRestaurantName);
        View c = findViewById(R.id.inc_inspection_restaurant_header);
        textViewRestaurantName = (TextView) c.findViewById(R.id.textViewRestaurantName);
        textViewRestaurantPhone = (TextView) c.findViewById(R.id.textViewRestaurantPhone);
        textViewRestaurantAddress = (TextView) c.findViewById(R.id.textViewRestaurantAddress);
        textViewRestaurantCuisine = (TextView) c.findViewById(R.id.textViewRestaurantCuisine);

        //We don't want to use this.
/*
        c.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
                if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Long press!");
                    startActionMode(InspectionActivity.this);
                return true;
            }
        });
*/

        camis = getIntent().getStringExtra("CAMIS");
        dba = getIntent().getStringExtra("DBA");

        //We want to
        //favorites=getIntent().getBooleanExtra("FAVORITES", false);
        //favorites=true;

        favorites=PreferenceUtility.checkIfARestaurantIsFavorite(camis);




        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Camis:"+camis);
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","DBA:"+dba);
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Favorite?"+favorites);

        textViewHeader = (TextView) findViewById(R.id.inspection_header_text);
        textViewHeader.setText("Press inspection line for details.");

        colorOk = ContextCompat.getColor(this,R.color.app_color_gut_ok);
        colorNo = ContextCompat.getColor(this,R.color.app_color_gut_no);
        colorMaybe = ContextCompat.getColor(this,R.color.app_color_gut_maybe);


        //=====This tells what broadcasts we want to receive.
        filter = new IntentFilter(AppConstant.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new HealthDataMainReceiver(this);
        this.registerReceiver(receiver, filter);

        if (DataProvider.getListDetailDataSet().size() == 0) {

            progressDialog = ProgressDialog.show(this, null,
                    getResources().getString(R.string.progess_dialog), true);

            progressDialog.show();
        }
        //We have data already.
        else {
            setUpData();
        }

        //Hack - we want to run this ourselves for quick debug. See mainActivity as well.
        if (AppConstant.TEMP_HACK) {
            Intent serviceToRun = new Intent(this, DataProvider.class);
            serviceToRun.putExtra("DataToGet", "CAMIS");
            serviceToRun.putExtra("CAMIS", "50006110");
            this.startService(serviceToRun);
        }
    }

    /**
     * Prepare the Screen's standard options menu to be displayed.  This is
     * called right before the menu is shown, every time it is shown.  You can
     * use this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.
     * <p/>
     * <p>The default implementation updates the system menu items based on the
     * activity's state.  Deriving classes should always call through to the
     * base class implementation.
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","On prepare menu");
        if (favorites){
            MenuItem item = menu.findItem(R.id.save_favorite_restaurant);
            item.setTitle("Remove favorite");
            Drawable icon = item.getIcon();
            icon.mutate().setColorFilter( favorite_selected_color, PorterDuff.Mode.MULTIPLY );
         //   setMenuFavoriteColor(getResources().getColor(R.color.Pink));
        }
        else{
            MenuItem item = menu.findItem(R.id.save_favorite_restaurant);
            item.setTitle("Remove favorite");
            Drawable icon = item.getIcon();
            icon.mutate().setColorFilter(favorite_default_color, PorterDuff.Mode.MULTIPLY );
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /*
    private void setMenuFavoriteColor(int color){

//        Drawable icon = ;
//        icon.mutate().setColorFilter( getResources().getColor(R.color.Pink), PorterDuff.Mode.MULTIPLY );
//        menuFavorite.setMenuFavororiteColor(getResources().getColor(R.color.Pink));
    }
    */


    //This will be called by receiver to handle data to UI
    @Override
    public synchronized void setUpData() {

        if (InspectionActivity.progressDialog != null) {
            InspectionActivity.progressDialog.dismiss();
        }

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Data Provider listDetailDataSet size:" + DataProvider.getListDetailDataSet().size());


        //Clear - we want to start fresh
        dataList.clear();

        //This adds the data that data provider produced. It has ALL data for that restaurant
        //All data has the head (name, camis, etc) and details (violations, scores,grades)
        //Is a bit inefficient, but that is how it is for now
        dataList.addAll(DataProvider.getListDetailDataSet());


        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Data size:" + dataList.size());


        //Fill in our header textViews
        Inspections inspect = dataList.get(0);

        textViewRestaurantName.setText(inspect.getDba());
        textViewRestaurantPhone.setText(inspect.getPhone());
        AppUtility.inspectionBuildAddress(inspect, textViewRestaurantAddress, true);
        textViewRestaurantCuisine.setText("Cuisine:"+inspect.getCuisine_description());
        textViewRestaurantCuisine.setVisibility(View.VISIBLE);


        //Get data from analyzer - dataList will have ALL info for a restaurant.
        dataAnalyzerSingleCamis = new DataAnalyzerSingleCamis(dataList);

        //This gets inspections by date for listView
        listInspectionByDate.clear();
        listInspectionByDate = dataAnalyzerSingleCamis.getInspectionsByDate();

        //Fills in header columns
        //inspectionHeaderAdapter=new InspectionHeaderAdapter(this,R.layout.view_row_items_inspection_data,true);
        //inspectionHeaderAdapter=new InspectionHeaderAdapter(this,R.layout.view_row_items_inspection_data,true);
        //  inspectionHeaderAdapter=new InspectionExpandAdapter(this, listDataHeader, sortedMapChildren,true);
        // listViewInspectionHeader.setAdapter(inspectionHeaderAdapter);

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Inspections by date size:" + listInspectionByDate.size());

        //   List<Integer> distinctYearsFromInspectionDate = dataAnalyzerSingleCamis.getDistinctYearsFromInspectionDate();
        //    DataPoints scoreMetrics = dataAnalyzerSingleCamis.getScoreMetrics();
        //    String lastGrade = dataAnalyzerSingleCamis.lastGrade();
        //    Map<Integer, Double> averageScoreByYear = dataAnalyzerSingleCamis.getAverageScoreByYear();
        //   int numberOfYearsOfData = dataAnalyzerSingleCamis.getNumberOfYearsOfData();
        //    List<Inspections> noScoresAdminData = dataAnalyzerSingleCamis.getListNoScoresAdminData();
        //    int closedCount = dataAnalyzerSingleCamis.getCountOfClosures();
        Inspections lastInspectionData = dataAnalyzerSingleCamis.getLastInspection();


        dataListLastInspection.add(lastInspectionData);
        // if (AppConfig.DEBUG) Log.i(this.getClass().getSimpleName()+">","Here");
        // mainDataArrayAdapter.notifyDataSetChanged();

        //List<Inspections> listOfInspectionClosures1 = dataAnalyzerSingleCamis.getListOfInspectionClosures();

        for (Inspections inspections1 : dataList) {
            listOfInspectionClosures.add(inspections1.getViolation_description() != null ? inspections1.getViolation_description() : "N/A");
        }


        // preparing list data
        prepareListData();


        inspectionExpandAdapterData = new InspectionExpandAdapter(this, listDataHeader, sortedMapChildren, false);

        inspectionExpandAdapterColumns = new InspectionExpandAdapter(this, null, null, true);

//        String reportName="";
//
//        if(Reports.getLastReportRun()!=null){
//            reportName=Reports.getLastReportRun().getReportName();
//        }

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Last report run:"+ Reports.getLastReportRun().toString());

     /*
      //  if (AppConfig.DEBUG) Log.i(this.getClass().getSimpleName()+">","Health code array:"+HealthDataRestaurants.getViolationCodesArray()[0]);
        //Want to set up our markers to look for report violations found
        switch (reportName){
            case Reports.ROACHES:
                inspectionExpandAdapterData.setMarkViolations(new MarkViolations(HealthDataRestaurants.getViolationCodesArray()));
                break;
            case Reports.RATS_OR_MICE_IN_PAST_YEAR:
                inspectionExpandAdapterData.setMarkViolations(new MarkViolations(HealthDataRestaurants.getViolationCodesArray()));
                break;
            case Reports.NO_GRADE_SIGN_POSTED_IN_PAST_YEAR:
                inspectionExpandAdapterData.setMarkViolations(new MarkViolations(HealthDataRestaurants.getViolationCodesArray()));
                break;
            case Reports.USER_SELECTED_VIOLATIONS_SEARCH:
                inspectionExpandAdapterData.setMarkViolations(new MarkViolations(HealthDataRestaurants.getViolationCodesArray()));
                break;
            case Reports.HAND_ISSUES:
                inspectionExpandAdapterData.setMarkViolations(new MarkViolations(HealthDataRestaurants.getViolationCodesArray()));
                break;
            default:
                //TODO We should use violation codes not text?
                //If we are here it wasn't from violations report but we want user to see violations.

                //Temp we will get this for every report - get rid of case?
                inspectionExpandAdapterData.setMarkViolations(new MarkViolations(HealthDataRestaurants.getViolationCodesArray()));

                if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Last report id:"+Reports.getLastReportRun().getReportId());

                if (Reports.getLastReportRun().getReportId()== Reports.REPORTS_USER_REPORT_ID){
                    inspectionExpandAdapterData.setMarkViolations(new MarkViolations(HealthDataRestaurants.getViolationCodesArray()));
                }

                if (Reports.getLastReportRun().getReportId()== Reports.REPORTS_VIOLATION_CODES_QUERY){
                    inspectionExpandAdapterData.setMarkViolations(new MarkViolations(Reports.getLastReportRun().getCodeArray()));
                }
        }

*/


        //We will get this for every report
        inspectionExpandAdapterData.setMarkViolations(new MarkViolations(HealthDataRestaurants.getViolationCodesArray()));

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Last report id:"+Reports.getLastReportRun().getReportId());

        if (Reports.getLastReportRun().getReportId()== Reports.REPORTS_USER_REPORT_ID){
            inspectionExpandAdapterData.setMarkViolations(new MarkViolations(HealthDataRestaurants.getViolationCodesArray()));
        }

        if (Reports.getLastReportRun().getReportId()== Reports.REPORTS_VIOLATION_CODES_QUERY){
            inspectionExpandAdapterData.setMarkViolations(new MarkViolations(Reports.getLastReportRun().getCodeArray()));
        }


        expListViewForColumns = (ExpandableListView) findViewById(R.id.lvExpColumns);

        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "Parent:" + expListViewForColumns.getParent().toString());

        expListView = (ExpandableListView) findViewById(R.id.lvExp);


        //expListView.getContext().obtainStyledAttributes(attrs, com.android.internal.R.styleable.ExpandableListView, defStyle,0));
        // setting list adapter
        expListView.setAdapter(inspectionExpandAdapterData);
        expListViewForColumns.setAdapter(inspectionExpandAdapterColumns);

        analyzeDataForGutFeeling();
        findReportMetrics();


        // adapter_inspection_header.notifyDataSetChanged();
  //      StringBuilder stringBuilder = new StringBuilder();

//            stringBuilder.append("Name:" + dba);
//            stringBuilder.append(DataViewerActivity.NEWLINE);
//            stringBuilder.append("CAMIS:" + camis);
//            stringBuilder.append(DataViewerActivity.NEWLINE);
//            stringBuilder.append("Records:" + dataList.size());
//            stringBuilder.append(DataViewerActivity.NEWLINE);
//            stringBuilder.append("Last graded grade:"+lastGrade);
//            stringBuilder.append(DataViewerActivity.NEWLINE);
//            stringBuilder.append("Latest inspections grade:"+lastInspectionData.getGrade());
//            stringBuilder.append(DataViewerActivity.NEWLINE);
//            stringBuilder.append("Latest score:"+scoreMetrics.getLatestData());
//            stringBuilder.append(DataViewerActivity.NEWLINE);
//            stringBuilder.append("Last score check:"+lastInspectionData.getScore());
//            stringBuilder.append(DataViewerActivity.NEWLINE);

        //stringBuilder.append("Number of years inspected:" + numberOfYearsOfData);
        //stringBuilder.append(DataViewerActivity.NEWLINE);
        //stringBuilder.append("Distinct years:" + distinctYearsFromInspectionDate.toString());
        //stringBuilder.append(DataViewerActivity.NEWLINE);
        //stringBuilder.append(scoreMetrics.getDataPointeName());
        //stringBuilder.append(DataViewerActivity.NEWLINE);

        //stringBuilder.append("Past year (365 days):" + scoreMetrics.getYearData());
        //stringBuilder.append(DataViewerActivity.NEWLINE);
        //stringBuilder.append("Ever:" + scoreMetrics.getAllData());
        // stringBuilder.append(DataViewerActivity.NEWLINE);
        //stringBuilder.append("Number of times closed:" + closedCount);
        // stringBuilder.append(DataViewerActivity.NEWLINE);


 //       stringBuilder.append("No Score inspections - ADMIN");
  //      stringBuilder.append(DataViewerActivity.NEWLINE);

//        for (Inspections inspections1 : noScoresAdminData) {
//            stringBuilder.append(inspections1.getViolation_description());
//            stringBuilder.append(DataViewerActivity.NEWLINE);
//        }

    //    if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", stringBuilder.toString());

        //textViewHeader.setText(stringBuilder.toString());

    }


    private void findReportMetrics() {
        //Depending on report we want to highlight which inspection it was found in.
        for (Map.Entry<Inspections, List<Inspections>> inspectionsListEntry : sortedMapChildren.entrySet()) {
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", inspectionsListEntry.getKey().toString());
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", inspectionsListEntry.getValue().toString());
        }


        //Can we get a textView from the adapter?
        Object item = expListView.getAdapter().getItem(0);
        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Item is a:" + item.getClass().getName());

    }

    /**
     * We will use this to analyze data to give and overall gut feeling rating.
     */
    private void analyzeDataForGutFeeling() {
        //This all tricky because some of the inspections are graded and some are not.
        //We can go by last score and then look at last grade.
        //Example: An establishment could have a last ungraded score of 2, but the previous grade was C.


    //    GutFeeling gutFeeling=new GutFeeling(listInspectionByDate);
        //Pass in ALL the data down to violation level
        GutFeeling gutFeeling=new GutFeeling(dataList);

        int analysisResult = gutFeeling.analyze();

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Result of analysisResult:"+analysisResult);


        setBackGroundColorTextView(textGutFeeling,analysisResult);
        textGutFeeling.setText("Gut feeling: "+gutFeeling.getReasonForGutFeeling());

    }


    private void setBackGroundColorTextView(View v,int gutFeeling){

        ((TextView)v).setTextColor(gutFeelingDefaultTextColor);

        switch (gutFeeling){
            case GutFeeling.OK:
                v.setBackgroundColor(colorOk);
                break;
            case GutFeeling.MAYBE:
                v.setBackgroundColor(colorMaybe);
                break;
            case GutFeeling.NO:
                v.setBackgroundColor(colorNo);
                ((TextView)v).setTextColor(gutFeelingNoTextColor);
                break;
        }

    }

/*

    private int gradeColor(Character grade){
        switch (grade){
            case 'A':
                return colorOk;
            case 'B':
                return colorMaybe;
            case 'C':
                return colorNo;
        }
        return 1;
    }
*/


    /*
 * Preparing the list data
 */
    private void prepareListData() {

        listDataHeader = new ArrayList<>();


        //We use this because we need they key (header) to be unique - date will be
        sortedMapChildren = new TreeMap<>(InspectionSorts.SORT_BY_INSPECTION_DATE_RECENT_FIRST);

        //All the inspections by date
        listDataHeader.addAll(listInspectionByDate);


        //Add all inspection by date to this:


        //Inspections ins1 = new Inspections();

        //We go through the inspection by dates (header)
        for (Inspections inspections : listInspectionByDate) {
            //List to hold violations (children)
            List<Inspections> listViolations = new ArrayList<>();
            for (Inspections allData : dataList) {
                if (inspections.getInspection_date().equals(allData.getInspection_date())) {
                    listViolations.add(allData);
                }
            }
            Collections.sort(listViolations, InspectionSorterPlus.SORT_BY_CRITICAL_FLAG);
            sortedMapChildren.put(inspections, listViolations);
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "List of violations add:" + listViolations.toString());
        }

        for (Inspections inspections1 : sortedMapChildren.keySet()) {
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "Key:" + inspections1.getInspection_date());
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "Value list size:" + sortedMapChildren.get(inspections1).size());
            List<Inspections> listChildren = sortedMapChildren.get(inspections1);
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "Children in list:" + listChildren.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data_inspection, menu);
        //Menu menuFavorite = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch(id) {
             case R.id.gut_feeling_color_key:
                Dialog dialog = new Dialog(InspectionActivity.this);
                dialog.setContentView(R.layout.fragment_gut_help_dialog);
                dialog.setTitle(getString(R.string.gut_feeling_color_key));
                dialog.setCancelable(true);
                UtilsShared.centerDialogTitle(dialog);
                dialog.show();
                break;
            case R.id.context_map_it:
                if (!AppUtility.isMapProviderInstalled) {
                    UtilsShared.toastIt(AppConstant.getApplicationContextMain(),"Google maps is not installed - cannot show map!", Toast.LENGTH_LONG);
                    return true;
                }
                AppUtility.mapRestaurant(this, AppUtility.buildLocationNameFromInspection(dataList.get(0)));
                break;
            case R.id.save_favorite_restaurant:

               if (AppConstant.DEBUG)
                        Log.i(this.getClass().getSimpleName() + ">", item.getTitle().toString());
                if(favorites) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    if (favorites) {
                                        PreferenceUtility.deleteSharedPreferenceStrings(PreferenceUtility.FAVORITE_RESTAURANT, camis);

                                        favorites = false;
                                    }
//                                    else {
//                                        PreferenceUtility.saveSharedPreferenceStrings(PreferenceUtility.FAVORITE_RESTAURANT, camis, dba);
//                                        favorites = true;
//                                    }
                                    invalidateOptionsMenu();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };


                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener);

                    builder.setCustomTitle(AppUtility.getTextDialogTitle(this, (!favorites ? "Save as Favorite?" : "Remove Favorite?")));

                    builder.show();
                }
                else{
                    PreferenceUtility.saveSharedPreferenceStrings(PreferenceUtility.FAVORITE_RESTAURANT, camis, dba);
                    UtilsShared.toastIt(AppConstant.getApplicationContextMain(),"Saved "+dba+" as favorite.",Toast.LENGTH_LONG);
                    favorites = true;
                    invalidateOptionsMenu();
                }
                break;
            case R.id.context_phone_it:

                /*
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                Boolean userWantsToDial = sharedPref.getBoolean(getResources().getString(R.string.pref_key_phone_dial), false);

                if (!userWantsToDial){
                    break;
                }
*/

               String phoneNumber= (textViewRestaurantPhone.getText() != null) ? textViewRestaurantPhone.getText().toString() : "";
              //  String phoneNumber=("7768066");
//        View parent = (View)view.getParent().getParent();
//        TextView txtView=null;
//        if (parent != null) {
//            txtView = (TextView)parent.findViewById(R.id.txt_dba);
//            System.out.println(txtView.getText());
//        }



                if (phoneNumber.trim().length() > 4){
                    String uri = "tel:" +phoneNumber ;
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
        }


        if (getResources().getResourceEntryName(item.getItemId()).contains("_help")){
            AppUtility.helpActivityStart(this,item);
        }


        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_home) {
//            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Go back to home screen");
//            Intent intent=new Intent(this,MainActivity.class);
//            startActivity(intent);
//            return true;
//        }




     /*   if (id==R.id.save_favorite_restaurant) {

            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">",item.getTitle().toString());
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            if (favorites){
                                PreferenceUtility.deleteSharedPreferenceStrings(PreferenceUtility.FAVORITE_RESTAURANT, camis);
                            }
                            else {
                                PreferenceUtility.saveSharedPreferenceStrings(PreferenceUtility.FAVORITE_RESTAURANT, camis, dba);
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener);

            builder.setCustomTitle(AppUtility.getTextDialogTitle(this,(!favorites?"Save as Favorite?":"Remove Favorite?")));

            builder.show();


        }
*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">", "In Destroy");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "In saved instance state");

        outState.putString("Previous visit", "We were once here!");

        super.onSaveInstanceState(outState);
    }


    //These aren't in use - we aren't using context menu.

    /**
     * Called when action mode is first created. The menu supplied will be used to
     * generate action buttons for the action mode.
     *
     * @param mode ActionMode being created
     * @param menu Menu used to populate action buttons
     * @return true if the action mode should be created, false if entering this
     * mode should be aborted.
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_menu_inspection, menu);
        return true;

    }

    /**
     * Called to refresh an action mode's action menu whenever it is invalidated.
     *
     * @param mode ActionMode being prepared
     * @param menu Menu used to populate action buttons
     * @return true if the menu or action mode was updated, false otherwise.
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /**
     * Called to report a user click on an action button.
     *
     * @param mode The current ActionMode
     * @param item The item that was clicked
     * @return true if this callback handled the event, false if the standard MenuItem
     * invocation should continue.
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.context_map_it:
                if (!AppUtility.isMapProviderInstalled){
                    UtilsShared.toastIt(AppConstant.getApplicationContextMain(),"Google maps is not installed - cannot show map!", Toast.LENGTH_LONG);
                    return true;
                }
                AppUtility.mapRestaurant(this,AppUtility.buildLocationNameFromInspection(dataList.get(0)));
                mode.finish(); // Action picked, so close the CAB
                return true;
            case R.id.context_phone_it:
                //shareCurrentItem();
                mode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }
    }

    /**
     * Called when an action mode is about to be exited and destroyed.
     *
     * @param mode The current ActionMode being destroyed
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
    //Not using the context men so above isn't called
}

package com.ericbandiero.ratsandmice.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ericbandiero.librarymain.UtilsShared;
import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.AppUtility;
import com.ericbandiero.ratsandmice.DataProvider;
import com.ericbandiero.ratsandmice.HeaderSorter;
import com.ericbandiero.ratsandmice.HealthDataMainReceiver;
import com.ericbandiero.ratsandmice.PreferenceUtility;
import com.ericbandiero.ratsandmice.R;
import com.ericbandiero.ratsandmice.ReportFieldBuilder;
import com.ericbandiero.ratsandmice.Reports;
import com.ericbandiero.ratsandmice.adapters.DataViewerAdapter;
import com.ericbandiero.ratsandmice.dialogs.DialogSorterFragment;
import com.ericbandiero.ratsandmice.interfaces.ISetUpData;
import com.ericbandiero.ratsandmice.parent.ParentActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import healthdeptdata.Inspections;
import healthdeptdata.InspectionSorts;



public class DataViewerActivity extends ParentActivity implements ISetUpData,DialogSorterFragment.SortAlertListener{

    //On screen rotate we always come here - start from scratch.

    public static final String NEWLINE=System.getProperty("line.separator");

    private static List<Inspections> dataList=new ArrayList<>();
    private static final int LIMIT_DATA=-1;

    private ListView listViewHeader;
    private ListView listViewData;

    private DataViewerAdapter dataViewerAdapter;

    //May get rid of these
   // private Comparator<Inspections> sortOrder;
   // private boolean needToReverseSortOrder=false;

    //Class to sort
    HeaderSorter headerSorter=new HeaderSorter();

    private IntentFilter filter;

    //private Bundle bundlePassedInToReceiver=new Bundle();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","In create of Data viewer activity");
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Intent:"+getIntent().toString());

        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Intent started from main?:"+getIntent().getStringExtra("STARTED_FROM_MAIN_ACTIVITY"));



        setContentView(R.layout.activity_dataview);

        listViewHeader = (ListView) findViewById(R.id.listViewMainHeader);
        listViewData = (ListView) findViewById(R.id.listViewMainData);

        textViewHeader=(TextView)findViewById(R.id.textheader);
        textViewLabels=(TextView)findViewById(R.id.textmainlabels);
        //textViewLabels.setText("Current filter:"+ DataProvider.getHealthDataFilter().getFilterName());
        textViewLabels.setText("Press restaurant row to get inspection details.");
        textViewLabels.setVisibility(View.VISIBLE);
       // final List<Inspections> list_filtered=new ArrayList<>();
        textViewHeader.setText(AppConstant.GETTING_DATA);

        //createHeaderColumns(listViewHeader);
       //AppUtility.createHeaderColumns(this, listViewHeader);

        //dataViewerAdapter=new MainDataArrayAdapter2(DataViewerActivity.this, R.layout.viewitems_main,dataList);
        dataViewerAdapter =new DataViewerAdapter(DataViewerActivity.this, R.layout.view_row_items_dataview_new,dataList);

        DataViewerAdapter dataViewerAdapterColumns=new DataViewerAdapter(DataViewerActivity.this,R.layout.view_row_items_dataview_columns);

        //Set columns
        String lastReport=Reports.getLastReportRun().getReportName();
        switch (lastReport){
            //Both cases
            case Reports.CLOSED_MOST_TIMES_EVER:
            case Reports.CLOSED_MOST_TIMES_PAST_YEAR:
                dataViewerAdapterColumns.setColumnNames(new String[]{"Name","Date","Closed",""});
                break;
        }


        listViewHeader.setAdapter(dataViewerAdapterColumns);


        listViewData.setAdapter(dataViewerAdapter);


        //If we are coming back to this screen from detail we don't need this. We already have data.
        if (dataList.isEmpty()) {
            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Data list is empty");
            textViewHeader.setText(AppConstant.GETTING_DATA);
            progressDialog = ProgressDialog.show(this, null,
                    getResources().getString(R.string.progess_dialog), true);

            progressDialog.show();
        }
        else{
            //See if we came back here from the next screen - don't want to re-initialize
            textViewHeader.setText(PreferenceUtility.getDefaultSharedPreferenceStrings(AppConstant.SHARED_PREF_RECORDS_HEADER_TEXT, " "));
            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Data list is not empty:"+dataList.size());
        }
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","DataProvider datalistsize:"+ DataProvider.getListDataSet().size());

        //At this point if we are in screen rotate we want to refill local dataList.
        if (DataProvider.getListDataSet().size()>0) {
            dataList.clear();
            dataList.addAll(DataProvider.getListDataSet());
            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">", DataProvider.getListDataSet().get(0).toString());
            dataViewerAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }

        //=====This tells what broadcasts we want to receive.
        filter = new IntentFilter(AppConstant.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        //receiver = new HealthDataReceiver();
        receiver = new HealthDataMainReceiver(this);
        this.registerReceiver(receiver, filter);
        //Moved to onResume
      //  this.registerReceiver(receiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data_viewer, menu);
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Last report:"+ Reports.getLastReportRun().getReportName());

        MenuItem item = menu.findItem(R.id.save_report);
        if (Reports.getLastReportRun().getReportName().equals(Reports.USER_SELECTED_VIOLATIONS_SEARCH)|
                Reports.getLastReportRun().getReportName().equals(Reports.FIELD_SEARCH)){
            item.setVisible(true);
        }
        else{
            item.setVisible(false);
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }


        if (id == R.id.save_report) {

            // get prompts.xml view
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.fragment_save_data_dialog, null);

            //Set up
            EditText editTextNotUSed= (EditText) promptsView.findViewById(R.id.user_report_name2);

            TextView textViewZipCodes=(TextView) promptsView.findViewById(R.id.lbl_enter_zip_Code);

            //We don't need to see these
            editTextNotUSed.setVisibility(View.GONE);
            textViewZipCodes.setVisibility(View.GONE);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            //LayoutInflater inflater = this.getLayoutInflater();

            builder.setView(promptsView);

           // final EditText userInput= (EditText) builder.getContext().getResources().getLayout(R.id.user_report_name);
            final EditText userInput = (EditText) promptsView
                    .findViewById(R.id.user_report_name);


            // Add the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">",userInput.getText().toString());
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(userInput.getWindowToken(), 0);
                    onSaveDataDialog(userInput.getText().toString());
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
             builder.setCustomTitle(AppUtility.getTextDialogTitle(this, "Save Report"));
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


        if (id == R.id.sort) {
            DialogFragment sorterFragment=new DialogSorterFragment();
            sorterFragment.show(getFragmentManager(), "Sorter");
            return true;
        }


        if (id==R.id.dataview_color_key){
            Dialog dialog = new Dialog(DataViewerActivity.this);
            dialog.setContentView(R.layout.fragment_dataview_color_key_dialog);
            dialog.setTitle(getString(R.string.dataview_color_key));
            dialog.setCancelable(true);
            UtilsShared.centerDialogTitle(dialog);
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Data list size:"+dataList.size());
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Filter:"+filter);
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Last report:"+Reports.getLastReportRun().toString());
        //this.registerReceiver(receiver, filter);
        //textViewHeader.setText(textForHeaderTextView+" Records:"+dataList.size());
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","resuming...");
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Intent in resume:"+getIntent().toString());
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Has extra:"+getIntent().hasExtra("Hello"));

        if (Reports.getLastReportRun().getReportName().equals(Reports.FAVORITE_RESTAURANTS)){
            if (dataList.size()>0){
                //We want to check to see if this is still a favorite
                for (java.util.Iterator iterator = dataList.iterator(); iterator.hasNext(); ) {
                    Inspections inspection = (Inspections) iterator.next();
                    if (!PreferenceUtility.checkIfARestaurantIsFavorite(inspection.getCamis())){
                        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","No longer a favorite");
                        iterator.remove();
                    }
                }
            }
            dataViewerAdapter.notifyDataSetChanged();
            textViewHeader.setText(Reports.getLastReportRun().getReportDesc() + getString(R.string.dataset_ressults_name) + dataList.size());
        }
    }

    /**
     * Handle onNewIntent() to inform the fragment manager that the
     * state is not saved.  If you are handling new intents and may be
     * making changes to the fragment state, you want to be sure to call
     * through to the super-class here first.  Otherwise, if your state
     * is saved but the activity is not stopped, you could get an
     * onNewIntent() call which happens before onResume() and trying to
     * perform fragment operations at that point will throw IllegalStateException
     * because the fragment manager thinks the state is still saved.
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","On handle new intent!");
        super.onNewIntent(intent);
    }

    @Override
    public void setUpData() {
        if (progressDialog!=null) {
            progressDialog.dismiss();
        }


        textViewHeader.setText(AppConstant.GETTING_DATA);
        dataList.clear();
        dataList.addAll(DataProvider.getListDataSet());
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Data list size:"+dataList.size());

        dataViewerAdapter.notifyDataSetChanged();

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Last report:"+Reports.getLastReportRun().getReportName());

        if(Reports.getLastReportRun().getReportId()!= Reports.REPORTS_USER_REPORT_ID) {
            if (Reports.getLastReportRun().getReportName().equals(Reports.CLOSED_MOST_TIMES_EVER)||Reports.getLastReportRun().getReportName().equals(Reports.CLOSED_MOST_TIMES_PAST_YEAR)){
                textViewHeader.setText(Reports.getLastReportRun().getReportDesc());
            }
            else{
                textViewHeader.setText(Reports.getLastReportRun().getReportDesc() + getString(R.string.dataset_ressults_name) + dataList.size());
            }
        }
        else{
            textViewHeader.setText(Reports.getLastReportRun().getReportName() + getString(R.string.dataset_ressults_name) + dataList.size());
        }

        if (!DataProvider.errorMessage.isEmpty()){
            if (DataProvider.errorMessage.toLowerCase().equals("out of memory")){
                textViewHeader.setText("No results - we had an error: " + DataProvider.errorMessage+ " - try a different filter that returns less results.");
            }
            else{
                textViewHeader.setText("No results - we had an error: " + DataProvider.errorMessage);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","In destroy");
        //dataList.clear();
        }

    @Override
    public void onFinishSortDialog(String sortSelected) {
        //Collections.sort(dataList,Collections.reverseOrder(InspectionSorts.SORT_BY_SCORE));
        //System.out.println(sortSelected);
        //They user picks a sort - we sort on the spot and also set sorter for future data fetches.
        switch(sortSelected){
            case "Name - A-Z":
                Collections.sort(dataList,InspectionSorts.SORT_BY_NAME);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_NAME);
                break;
            case "Name - Z-A":
                Collections.sort(dataList,Collections.reverseOrder(InspectionSorts.SORT_BY_NAME));
                DataProvider.setSorterToUse(Collections.reverseOrder(InspectionSorts.SORT_BY_NAME));
                break;
            case "Score - Best to Worst":
                Collections.sort(dataList,InspectionSorts.SORT_BY_SCORE);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_SCORE);
                break;
            case "Score - Worst to Best":
                Collections.sort(dataList,Collections.reverseOrder(InspectionSorts.SORT_BY_SCORE));
                DataProvider.setSorterToUse(Collections.reverseOrder(InspectionSorts.SORT_BY_SCORE));
                break;
            case "Inspection Date - Most recent":
                Collections.sort(dataList,InspectionSorts.SORT_BY_INSPECTION_DATE_RECENT_FIRST);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_INSPECTION_DATE_RECENT_FIRST);
                break;

            case "Inspection Date - Oldest":
                Collections.sort(dataList,InspectionSorts.SORT_BY_INSPECTION_DATE_OLDEST_FIRST);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_INSPECTION_DATE_OLDEST_FIRST);
                break;

            case "Borough - A-Z":
                Collections.sort(dataList,InspectionSorts.SORT_BY_BORO);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_BORO);
                break;

            case "Borough - Z-A":
                Collections.sort(dataList,InspectionSorts.SORT_BY_BORO_DESC);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_BORO_DESC);
                break;

            case "Zip Code":
                Collections.sort(dataList,InspectionSorts.SORT_BY_ZIP_CODE);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_ZIP_CODE);
                break;

            case "Zip Code Desc":
                Collections.sort(dataList,InspectionSorts.SORT_BY_ZIP_CODE_DESC);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_ZIP_CODE_DESC);
                break;

            case "Grade":
                Collections.sort(dataList,InspectionSorts.SORT_BY_GRADE);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_GRADE);
                break;
            case "None":
                DataProvider.setSorterToUse(null);
                break;
        }
        PreferenceUtility.saveDefaultSharedPreferenceStrings(AppConstant.LAST_SORT_ORDER, sortSelected);
        dataViewerAdapter.notifyDataSetChanged();
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

    /*
    We are using this because the onClick gets blocked by descendants with click events (phone)
     */
    public void onClickItemLayout(View view) {
        AppUtility.getCamis(this,view,listViewData);
    }

    public void headerClicked(View view) {
        headerSorter.sortHeader(view,dataList,dataViewerAdapter);
    }


    public static void clearPreviousData(){
        dataList.clear();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","We are in on stop!");
        //Save this in case we come back into screen from next screen
//        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString("HEADER_TEXT",textViewHeader.getText().toString());
//        editor.apply();
        PreferenceUtility.saveDefaultSharedPreferenceStrings(AppConstant.SHARED_PREF_RECORDS_HEADER_TEXT, textViewHeader.getText().toString());
    }

    //Actually we aren't using that fragment, so not overriding...

    public void onSaveDataDialog(String userReportName) {
        if (!userReportName.isEmpty()) {
           // PreferenceUtility.saveUserViolationReports(this, code, userReportName.toUpperCase());
            if (Reports.getLastReportRun().getReportName().equals(Reports.USER_SELECTED_VIOLATIONS_SEARCH)) {
                PreferenceUtility.saveUserViolationReports(this, ViolationsActivity.aViolationCodes, userReportName.toUpperCase());
            }
            if (Reports.getLastReportRun().getReportName().equals(Reports.FIELD_SEARCH)) {
                if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">",Reports.getLastReportRun().toString());
                PreferenceUtility.saveUserViolationReports(this, ReportFieldBuilder.getaData(), userReportName.toUpperCase());
            }
        }
    }
}

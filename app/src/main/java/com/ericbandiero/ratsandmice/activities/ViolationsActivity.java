package com.ericbandiero.ratsandmice.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ericbandiero.librarymain.UtilsShared;
import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.AppUtility;
import com.ericbandiero.ratsandmice.DataProvider;
import com.ericbandiero.ratsandmice.HealthDataMainReceiver;
import com.ericbandiero.ratsandmice.PreferenceUtility;
import com.ericbandiero.ratsandmice.R;
import com.ericbandiero.ratsandmice.Reports;
import com.ericbandiero.ratsandmice.Violations;
import com.ericbandiero.ratsandmice.interfaces.ISetUpData;
import com.ericbandiero.ratsandmice.parent.ParentActivity;
import com.ericbandiero.ratsandmice.ReportFieldBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import healthdeptdata.DataWrapper;
import healthdeptdata.InspectionSorts;
import healthdeptdata.Inspections;


public class ViolationsActivity extends ParentActivity implements ISetUpData {

    public static final int REQUEST_CODE = 1001;
    public static final int REQUEST_CODE_VIOLATION_FLAG = 1002;
    public static final String REQUEST_MESSAGE = "Violation code";
    //public static String VIOLATION_CODE_SELECTED;
    public static String VIOLATION_CODE_DESC;


    private IntentFilter filter;

    private final List<Inspections> listInspectionViolations = new ArrayList<>();

    private ListView listView;

    //This will hold each code fror violation that user selects
    private final List<String> listCodes = new ArrayList<>();

    //List to hold violation codes we want to get
    public static String[] aViolationCodes;

    //private List<Inspections> listInspections;

    //We will use this for other date sets besides violations
    private boolean isViolationReport;

    private List<DataWrapper> listDataWrapper;

    private Map<String, Map<String, Integer>> mapDataAllDimesnions=new HashMap<>();

    //When we have report Cuisines by Current Filter we need to reset Reports last report run back to it because:
    //If user drills down, another report is run, erasing this last reort name with 'field_search'
    //When we come back to activity the right getview isn't used.
    private boolean needToResetReportName=false;
    private Reports savedLastReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violations);
        textViewHeader = (TextView) findViewById(R.id.textheader);

        textViewLabels = (TextView) findViewById(R.id.textmainlabels);
        textViewLabels.setVisibility(View.GONE);

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","In violations create");

        LayoutInflater inflater = (LayoutInflater) this.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(android.R.layout.simple_list_item_1, null);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);

        int padLeft = textView.getPaddingLeft();

        //If this is null we have probablyhad an error...

        if(Reports.getLastReportRun()==null){
            UtilsShared.toastIt(AppConstant.getApplicationContextMain(),"We had an error...", Toast.LENGTH_LONG);
            finish();
        }

        if (Reports.getLastReportRun().getReportName().equals(Reports.META_RESTAURANTS_BY_BORO)|Reports.getLastReportRun().getReportName().equals(Reports.META_RESTAURANTS_BY_FILTER)) {
          //  LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams params =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            // params.setMargins(30,10,10,10);
            //params.setMarginStart(padLeft);
            params.gravity = Gravity.CENTER;
            textViewHeader.setLayoutParams(params);
        }


        textViewHeader.setText(AppConstant.GETTING_DATA);
        textViewHeader.setVisibility(View.INVISIBLE);


        //=====This tells what broadcasts we want to receive.
        filter = new IntentFilter(AppConstant.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        //receiver = new HealthDataReceiver();
        receiver = new HealthDataMainReceiver(this);
        this.registerReceiver(receiver, filter);

        progressDialog = ProgressDialog.show(this, null,
                getResources().getString(R.string.progess_dialog), true);

        progressDialog.show();
//        if (DataProvider.getListViolationsDataSet() !=null| DataProvider.getListViolationsDataSet().size()==0){
//            setUpData();
//        }
        if (DataProvider.getListViolationsDataSet().size() > 0) {
            setUpData();
        }
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

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">", "In resume");


        if (needToResetReportName==true){
            Reports.setLastReportRun(savedLastReport);
        }

        String lastReportName = Reports.getLastReportRun().getReportName();

        //isViolationReport=Reports.getLastReportRun().getReportName().equals(Reports.VIOLATIONS_USER_FLAG_SET)|Reports.getLastReportRun().getReportName().equals(Reports.VIOLATIONS_USER_CREATE);

        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "Is violation report:" + isViolationReport);

 if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "Last report name in resume:" + lastReportName);


        switch (lastReportName) {
            case Reports.VIOLATIONS_USER_CREATE:
                setTitle("Violations");
                textViewLabels.setText(R.string.user_violation_create);
                break;
            case Reports.VIOLATIONS_USER_FLAG_SET:
                setTitle("Violations to Flag");
                textViewLabels.setText(R.string.user_violations_to_flag);
                break;
            case Reports.META_CUISINES_BY_BORO:
                setTitle("Cuisines");
                textViewLabels.setText(Reports.getLastReportRun().getReportName());
                break;

            case Reports.META_CUISINES_BY_FILTER:
                setTitle("Cuisines by filter");
                textViewLabels.setText(Reports.getLastReportRun().getReportName());
                break;
            case Reports.META_RESTAURANTS_BY_BORO:
                setTitle(Reports.getLastReportRun().getReportDesc());
                textViewLabels.setText(Reports.getLastReportRun().getReportName());
                break;
            case Reports.META_RESTAURANTS_BY_FILTER:
                setTitle(Reports.getLastReportRun().getReportDesc());
                textViewLabels.setText(Reports.getLastReportRun().getReportName());
                break;
            case Reports.META_RESTAURANTS_BY_ZIP_CODE:
                setTitle(Reports.getLastReportRun().getReportDesc());
                textViewLabels.setText(Reports.getLastReportRun().getReportName());
                break;
            default:
                setTitle("Data");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_violations, menu);
        MenuItem m = menu.findItem(R.id.violation_menu_cancel);
        MenuItem mSorter = menu.findItem(R.id.sorter_two_fields);

        if (Reports.getLastReportRun().getReportName().equals(Reports.META_CUISINES_BY_BORO)| Reports.getLastReportRun().getReportName().equals(Reports.META_CUISINES_BY_FILTER)) {
            mSorter.setTitle(getResources().getString(R.string.sort_by_cuisines_value));
        } else {
            mSorter.setVisible(false);
        }

        if (!isViolationReport) {
            m.setVisible(false);
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

        if (id == R.id.violation_menu_cancel) {
            finish();
            return true;
        }

        if (id == R.id.sorter_two_fields) {
            if (item.getTitle().equals(getResources().getString(R.string.sort_by_cuisines_value))) {
                Collections.sort(listDataWrapper, Collections.reverseOrder(DataWrapper.SORT_BY_VALUE_INTEGER));
                item.setTitle(getResources().getString(R.string.sort_by_cuisines_field));
            } else {
                Collections.sort(listDataWrapper, DataWrapper.SORT_BY_FIELD);
                item.setTitle(getResources().getString(R.string.sort_by_cuisines_value));

            }

            ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
            return true;
        }


        //Up arrow
        if (id == android.R.id.home) {
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "Count of items checked:" + (listView!=null?listView.getCheckedItemCount():-1));
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "Codes:" + listCodes.toString());
            if (isViolationReport) {
                getSelectedViolations();
            }
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setUpData() {

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        textViewHeader.setVisibility(View.VISIBLE);

        isViolationReport = Reports.getLastReportRun().getReportName().equals(Reports.VIOLATIONS_USER_FLAG_SET) | Reports.getLastReportRun().getReportName().equals(Reports.VIOLATIONS_USER_CREATE);


        listInspectionViolations.clear();
        listInspectionViolations.addAll(DataProvider.getListViolationsDataSet());
        Collections.sort(listInspectionViolations, InspectionSorts.SORT_BY_VIOLATION_DESC);

        final List<Violations> list_violations = new ArrayList<>();

        for (Inspections listInspectionViolation : listInspectionViolations) {
            list_violations.add(new Violations(listInspectionViolation.getViolation_code(), listInspectionViolation.getViolation_description()));
        }


        textViewHeader.setText(getString(R.string.result_term) + listInspectionViolations.size());

        listView = (ListView) findViewById(R.id.base_listView);
        //listView = new ListView(this);
        //ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1, android.R.id.text1, violations);
        //ViolationsAdapter arrayAdapter=new ViolationsAdapter(this,R.layout.view_row_items_violations_adapter,listInspectionViolations);
        //ArrayAdapter arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_checked,violations);

        // listView.setAdapter(arrayAdapter);


        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "Violations...last report:" + Reports.getLastReportRun().toString());

        if (Reports.getLastReportRun().getReportName().equals(Reports.VIOLATIONS_USER_FLAG_SET)) {
            //Set the selected flag in each violation

            Set<String> userViolationsToMark = PreferenceUtility.getUserViolationsToMark();

            for (Violations violation : list_violations) {
                if (userViolationsToMark.contains(violation.getCode())) {
                    violation.setSelected(true);
                }
            }

            Collections.sort(list_violations, Violations.SORT_BY_SELECTED);

            textViewHeader.setText(R.string.violations_to_be_marked);
        }


        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "In setUp:" + isViolationReport);

        if (isViolationReport) {

            listView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_checked, list_violations) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    //textView.setGravity(Gravity.FILL);

                    //textView.setTextColor(Color.RED);

                    //System.out.println("Violation:" + textView);

                    final View row = super.getView(position, convertView, parent);

                    //if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Row name:"+row.getClass().getName());

                    final CheckBox checkBox = (CheckBox) row.findViewById(android.R.id.checkbox);


                    Violations violation = (Violations) getItem(position);
                    if (violation.isSelected()) {
                        // checkBox.setChecked(true);
                        listView.setItemChecked(position, true);
                        // textView.sets
                    }
                   // if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Violation textview maxlines:"+textView.getMaxLines());


                    textView.setTextSize(12);

                    return textView;
                }
            });
        }


        //Bug - If use clicks to drill down, when we come back here the last report name Cuisine by Filter anymore
        if (Reports.getLastReportRun().getReportName().equals(Reports.META_CUISINES_BY_BORO)|Reports.getLastReportRun().getReportName().equals(Reports.META_CUISINES_BY_FILTER)) {

             listDataWrapper=new ArrayList<>();

            if (DataProvider.getMapDataHolder()!=null) {
                listDataWrapper = new ArrayList<>(DataProvider.getMapDataHolder().getLisDataWrapper());
                mapDataAllDimesnions = DataProvider.getMapDataHolder().getMapDataAllt();
                Collections.sort(listDataWrapper, DataWrapper.SORT_BY_FIELD);

            }
            //listInspections=new ArrayList<>(DataProvider.getListDataSet());
            // Collections.sort(listInspections, InspectionSorterPlus.SORT_BY_FOOD_TYPE);

            if (Reports.getLastReportRun().getReportName().equals(Reports.META_CUISINES_BY_FILTER)){
                textViewHeader.setText(getString(R.string.result_term) + listDataWrapper.size()+" - Filter:"+DataProvider.getHealthDataFilter().getFilterName());
            }
            else{
                textViewHeader.setText(getString(R.string.result_term) + listDataWrapper.size());
            }

           // textViewLabels.setText("Filter:"+DataProvider.getHealthDataFilter().getFilterName());
           // textViewLabels.setVisibility(View.VISIBLE);
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Datawrapper:"+listDataWrapper.toString());

            listView.setAdapter(new ArrayAdapter(this, R.layout.map_two_text_fields, listDataWrapper) {


                final int padding=20;

                //private int layoutResourceId=;

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    // LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();

                    ViewHolderCuisines viewHolderCuisines=new ViewHolderCuisines();
                    View row = convertView;
                    if (row == null) {
                        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        row = inflater.inflate(R.layout.map_two_text_fields, null);
                        viewHolderCuisines.textView = (TextView) row.findViewById(R.id.textViewField);
                        row.setTag(viewHolderCuisines);
                    }
                    else{

                        viewHolderCuisines = (ViewHolderCuisines) convertView.getTag();
                    }

                    viewHolderCuisines.textView.setTypeface(Typeface.MONOSPACE);

                    //TextView textView = (TextView) super.getView(position, convertView, parent);
                    //TextView textViewField = (TextView) row.findViewById(R.id.textViewField);
                    //TextView textViewValue = (TextView) row.findViewById(R.id.textViewMapValue);
                    //textViewField.setTypeface(Typeface.MONOSPACE);
                    //textViewValue.setTypeface(Typeface.MONOSPACE);
                    //row.setBackgroundColor(Color.YELLOW);
                    DataWrapper dataWrapper = listDataWrapper.get(position);


                    String keyField=dataWrapper.getField();
                    String field = dataWrapper.getField()+"("+dataWrapper.getValue()+")";

                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Map size:"+mapDataAllDimesnions.size());
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Map entry set:"+mapDataAllDimesnions.entrySet().toString());
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Field:"+field);


                    //Map<String, Integer> map = mapDataAllDimesnions.get(field);
                    SortedMap<String, Integer> map = new TreeMap<>(mapDataAllDimesnions.get(keyField));

                    map.put("TOTAL",Integer.parseInt(dataWrapper.getValue()));
                    field+=DataViewerActivity.NEWLINE;

                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Check last report run:"+Reports.getLastReportRun().getReportName());
                    if (Reports.getLastReportRun().getReportName().equals(Reports.META_CUISINES_BY_FILTER)){
                        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Clearing map!");
                        map.clear();
                    }

                    for (Map.Entry<String, Integer> stringIntegerEntry : map.entrySet()) {
                        field+=stringIntegerEntry.getKey()+":"+StringUtils.leftPad(Integer.toString(stringIntegerEntry.getValue()), padding - (stringIntegerEntry.getKey().length()));
                        field+=DataViewerActivity.NEWLINE;
                    }

                    viewHolderCuisines.textView.setText(field);


                    return row;
                }
            });

            //We were having a problem in ReportFieldBuilder when no filter is applied - out of memory.
            //Right now we will let this pass - so we put true. We apply the filter in ReportFieldBuilder
            if (true||Reports.getLastReportRun().getReportName().equals(Reports.META_CUISINES_BY_FILTER)) {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //We want to preserve current report for when we return
                        if (Reports.getLastReportRun().getReportName().equals(Reports.META_CUISINES_BY_FILTER)){
                            needToResetReportName=true;
                            savedLastReport=Reports.getLastReportRun();
                        }


                        //TODO Should centralize how this is done. Duplicate code from mainactivity - make builder
                        String field = ((DataWrapper) listView.getItemAtPosition(position)).getField();
                        if (AppConstant.DEBUG)
                            Log.d(this.getClass().getSimpleName() + ">", "Field:" + field);
                        if (AppConstant.DEBUG)
                            Log.d(this.getClass().getSimpleName() + ">", mapDataAllDimesnions.get(field).values().toString());
                        runFieldReport(field, "cuisine_description", field + " Cuisine");
                    }
                });
            }
        }


        if (Reports.getLastReportRun().getReportName().equals(Reports.META_RESTAURANTS_BY_ZIP_CODE)
                |Reports.getLastReportRun().getReportName().equals(Reports.META_RESTAURANTS_BY_BORO)
                |Reports.getLastReportRun().getReportName().equals(Reports.META_RESTAURANTS_BY_FILTER)) {

            final List<DataWrapper> listDataWrapper = new ArrayList<>();

            Map<String, Integer> stringIntegerMap = DataProvider.getStringIntegerMap();


            int largestStringField = 0;
            int largestStringValue = 0;
            int totalRestaurants = 0;

            //textViewHeader.setText(getString(R.string.result_term)+stringIntegerMap.size());


            textViewHeader.setBackgroundColor(Color.YELLOW);

            for (Map.Entry<String, Integer> stringIntegerEntry : stringIntegerMap.entrySet()) {
                totalRestaurants += stringIntegerEntry.getValue();
                DataWrapper dataWrapper = new DataWrapper();
                dataWrapper.setField(stringIntegerEntry.getKey());
                dataWrapper.setValue(String.valueOf(stringIntegerEntry.getValue()));
                largestStringField = Math.max(largestStringField, stringIntegerEntry.getKey().length());
                largestStringValue = Math.max(largestStringValue, String.valueOf(stringIntegerEntry.getValue()).length());
                listDataWrapper.add(dataWrapper);
            }

            DataWrapper dataWrapperTotal = new DataWrapper();
            dataWrapperTotal.setField("Total");
            dataWrapperTotal.setValue(String.valueOf(totalRestaurants));

            final float total = totalRestaurants;

            textViewHeader.setText(Reports.getLastReportRun().getReportDesc()+":"+totalRestaurants);

            int padSizeField = 2;

            final int startingPosition = largestStringField + padSizeField;
            final int padValue = largestStringValue;

            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "Largest field:" + largestStringField);
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "Starting position:" + startingPosition);

            //textViewHeader.setText("Total Restaurants:"+totalRestaurants);
            textViewLabels.setVisibility(View.GONE);
            //Sort by values
            Collections.sort(listDataWrapper, Collections.reverseOrder(DataWrapper.SORT_BY_VALUE_INTEGER));
            listDataWrapper.add(dataWrapperTotal);
            listView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, listDataWrapper) {

                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                                        int defaultColor = 0;

                                        ViewHolder viewHolder = new ViewHolder();

                                        //(TextView) super.getView(position, convertView, parent)
                                        //TextView textView = (TextView) super.getView(position, convertView, parent);
                                        if (null == convertView) {
                                            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
                                            viewHolder.textView = (TextView) super.getView(position, convertView, parent);
                                            viewHolder.textView.setText("");
                                            viewHolder.textView.setTextSize(12f);
                                            viewHolder.textView.setTypeface(Typeface.MONOSPACE);
                                            defaultColor = viewHolder.textView.getDrawingCacheBackgroundColor();
                                            convertView.setTag(viewHolder);
                                        } else {
                                            viewHolder = (ViewHolder) convertView.getTag();
                                        }


                                        String field = listDataWrapper.get(position).getField();
                                        String value = listDataWrapper.get(position).getValue();

                                        int valueInt = Integer.parseInt(value);
                                        float percentOfTotal = ((valueInt / total) * 100.0f);


                                        String percent = (total == 0) ? "0.00" : String.format(Locale.US, "%.2f", percentOfTotal);

                                        String rowText = (StringUtils.rightPad(field, field.length() + (startingPosition - field.length() + (padValue - value.length())), " ") + value) + StringUtils.leftPad(percent, 8) + "%";

                                        viewHolder.textView.setText(rowText);

                                        if (field.toLowerCase().equals("total")) {
                                            viewHolder.textView.setBackgroundColor(Color.YELLOW);
                                            //  listView.setDividerHeight(4);
                                        } else {
                                            viewHolder.textView.setBackgroundColor(defaultColor);
                                            //listView.setDividerHeight(defaultDividerHeight);
                                        }
                                        return convertView;
                                    }
                                }
            );

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Clicked:"+listDataWrapper.get(position).toString());
                    String field = ((DataWrapper) listView.getItemAtPosition(position)).getField();
                    if (field.toLowerCase().equals("total")){
                        return;
                    }
                    runFieldReport(field,"zipcode"," Restaurants in zipcode:"+field);
                }
            });

        }

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                onListViewClick(parent,view, position);
//            }
//        });
    }

    static class ViewHolder {
        public TextView textView;
       // public View divider;
    }

    static class ViewHolderCuisines {
        public TextView textView;
        //public View divider;
    }


    private void getSelectedViolations() {
        SparseBooleanArray sparseArray = listView.getCheckedItemPositions();

        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "Checked items:" + sparseArray.size());

        String code = null;
        String desc = null;

        listCodes.clear();

        for (int i = 0; i < sparseArray.size(); i++) {
            int key = sparseArray.keyAt(i);
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">", "key = " + key);
            // get the object by the key.
            //Object obj = sparseArray.;
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "checked or not = " + sparseArray.get(key));
            if (sparseArray.get(key)) {
                Violations violation = (Violations) listView.getItemAtPosition(key);
                //VIOLATION_CODE_SELECTED=violation.getCode();
                code = violation.getCode();
                VIOLATION_CODE_DESC = violation.getDescription();
                desc = violation.getDescription();
                listCodes.add(code);
            }
        }


        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "listCodes = " + listCodes.toString());
        Intent intent = new Intent();
        intent.putExtra(ViolationsActivity.REQUEST_MESSAGE, code);
        //VIOLATION_CODE_SELECTED=code;
        VIOLATION_CODE_DESC = desc;

        aViolationCodes = new String[listCodes.size()];
        listCodes.toArray(aViolationCodes);

        if (listCodes.size() > 1) {
            VIOLATION_CODE_DESC = "Multiple selected violations.";
        }

        if (listCodes.isEmpty()) {
            //This can be empty - means they dont want to flag any violation for watch.
            if (Reports.getLastReportRun().getReportName().equals(Reports.VIOLATIONS_USER_FLAG_SET)){
                setResult(RESULT_OK, intent);
            }
            else{
                setResult(RESULT_CANCELED, intent);
            }

        } else {
            setResult(RESULT_OK, intent);
        }
    }

    private  void runFieldReport(String valueToFind,String fieldNameToSearch,String description){
        ReportFieldBuilder reportFieldBuilder = new ReportFieldBuilder(valueToFind, fieldNameToSearch, description, getApplicationContext());
        reportFieldBuilder.runFieldReport();
        if (true){
            return;
        }
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), DataViewerActivity.class);
        Intent serviceToRun;
        Reports r = new Reports();
        Bundle bundle = new Bundle();
        serviceToRun = new Intent(getApplicationContext(), DataProvider.class);
        //Determines if filter is used
        if (Reports.getLastReportRun().getReportName().equals(Reports.META_CUISINES_BY_BORO)){
            serviceToRun.putExtra(DataProvider.STRICT_SEARCH_MUST_USE_FILTER,false);
        }
        else{
            serviceToRun.putExtra(DataProvider.STRICT_SEARCH_MUST_USE_FILTER,true);
        }
        r.setReportName(Reports.FIELD_SEARCH);
        r.setReportDesc(description);
        Reports.setLastReportRun(r);
        bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, r);
        // serviceToRun.putExtra("cuisine",query);

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Search query:"+valueToFind);
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","field:"+fieldNameToSearch);

        serviceToRun.putExtra("search_query",valueToFind);
        serviceToRun.putExtra("field",fieldNameToSearch);


        serviceToRun.putExtras(bundle);
        startService(serviceToRun);
        startActivity(intent);
    }
}

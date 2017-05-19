package com.ericbandiero.ratsandmice.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.DataProvider;
import com.ericbandiero.ratsandmice.PreferenceUtility;
import com.ericbandiero.ratsandmice.R;
import com.ericbandiero.ratsandmice.parent.ParentActivity;
import com.ericbandiero.librarymain.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import healthdeptdata.HealthDataFilter;
import healthdeptdata.HealthDataRestaurants;


public class FilterActivity extends ParentActivity implements AdapterView.OnItemLongClickListener{
    List<HealthDataFilter> allFilters;
    List<String> filterStrings;
    ListView listView;
    public static String currentFilterString;
    String currentFilterName;
    TextView selection;
    boolean userCancelledChanges=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lib_activity_filter);
       // setContentView(R.layout.lib_activity_chooser);
        listView=(ListView)findViewById(R.id.lib_list_filter);


        //This will have all the filters, including ser created ones
        allFilters=new ArrayList<>( new HealthDataRestaurants().getAllFilters()) ;

        //Get the filters user doesn't want to see

        final Set<String> userHideFilterChoices = PreferenceUtility.getUserHideFilterChoices();


        //We don't want user to be able to disable this filter.
        Iterator<HealthDataFilter> iterator = allFilters.iterator();
        while (iterator.hasNext()) {
            HealthDataFilter next =  iterator.next();
            if (next.getFilterName().equals("NONE")){
                iterator.remove();
            }
        }


        filterStrings = new ArrayList<>(allFilters.size());

        for (HealthDataFilter healthDataFilter : allFilters) {
            filterStrings.add(healthDataFilter.getFilterName());
        }

        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        currentFilterName = PreferenceUtility.getDefaultSharedPreferenceStrings(AppConstant.LAST_FILTER_NAME, "NONE");

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Previous filter:" + currentFilterName);

        currentFilterString = DataProvider.getHealthDataFilter().getFilterName();

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Current filter:" + currentFilterString);

        listView.setAdapter(new ArrayAdapter<String>(
                this,
                // android.R.layout.simple_expandable_list_item_1,
                android.R.layout.simple_list_item_checked,
                filterStrings) {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","In getview!");
                //TODO Create static holder for this?
                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                if (!userHideFilterChoices.contains(textView.getText())) {

                //    listView.setItemChecked(position, true);
                }
                // System.out.println(textView.getText());
                /*YOUR CHOICE OF COLOR*/
                if (currentFilterName.equals(textView.getText())) {
                    textView.setTextColor(Color.BLUE);
                }
                return view;
            }
        });


        listView.setOnItemLongClickListener(this);

        //We check that at least one is selected
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = " position:" + position + "  " + filterStrings.get(position);
                if (AppConstant.DEBUG)
                    Log.i(this.getClass().getSimpleName() + ">", "Filter name:" + allFilters.get(position).toString());
                if (listView.getCheckedItemCount()==0){
                    UtilsShared.toastIt(getApplicationContext(),"You must have at least one filter",Toast.LENGTH_SHORT);
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Clicked item position:"+position);
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Clicked item id:"+id);
                    listView.setItemChecked(position,true);
                }
                if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Checked item count:"+listView.getCheckedItemCount());
             }
        });
        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Count:" + listView.getAdapter().getView(1, null, null).getClass().toString());


        //Set the items to checked
        int count=listView.getAdapter().getCount();
        for (int i = 0; i <count ; i++) {
            if (!userHideFilterChoices.contains(listView.getItemAtPosition(i).toString())) {
                listView.setItemChecked(i, true);
            }
        }

    }


    private void saveUserFilterChoices(){
        if (!userCancelledChanges) {
            Set<String> hideFilters = new HashSet<>();
            int itemCount = listView.getAdapter().getCount();
            for (int i = 0; i < itemCount; i++) {
               // System.out.println("Filter:" + listView.getItemAtPosition(i).toString());
              //  System.out.println("Is checked:" + listView.isItemChecked(i));
                if (!listView.isItemChecked(i)) {
                    hideFilters.add(listView.getItemAtPosition(i).toString());
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Is checked will hide:" + listView.isItemChecked(i));
                }
            }
            PreferenceUtility.saveUserHideFilterChoices(hideFilters);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filter, menu);
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
        if (id == R.id.action_filter_cancel) {
            userCancelledChanges=true;
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//        String text = " position:" + position + "  " + filterStrings.get(position);
//        if (AppConstant.DEBUG)
//            Log.i(this.getClass().getSimpleName() + ">", "Filter name:" + allFilters.get(position).toString());
//        //selection.setText(text);
//        DataProvider.setHealthDataFilter(allFilters.get(position));
//        //finish();
//    }


    @Override
    protected void onPause() {
        super.onPause();
        saveUserFilterChoices();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //PreferenceUtility.saveLastFilterName(this);

        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","In stop");
    }

    /**
     * Callback method to be invoked when an item in this view has been
     * clicked and held.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need to access
     * the data associated with the selected item.
     *
     * @param parent   The AbsListView where the click happened
     * @param view     The view within the AbsListView that was clicked
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     * @return true if the callback consumed the long click, false otherwise
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","position:"+position);
        final String viewItem=listView.getItemAtPosition(position).toString();
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","viewItem = " + viewItem);

        Map<String, ?> userZipCodeFilters = PreferenceUtility.getUserZipCodeFilters();

        Set<String> stringUser = userZipCodeFilters.keySet();

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","stringUser.to = " + stringUser.toString());

        if (stringUser.contains(AppConstant.USER_PREF_ZIP_FILTER_PREFIX +viewItem)) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //  PreferenceUtility.deleteUserViolationReports(r.getReportDesc());
                            //  mainExpandAdapter.removeChildItem(groupPosition,childPosition);
//                                        finish();
                            //startActivity(getIntent());
                            PreferenceUtility.removeDefaultSharedPreferenceStrings(AppConstant.USER_PREF_ZIP_FILTER_PREFIX + viewItem);
                            HealthDataFilter.removeFilter(viewItem.toUpperCase());

                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("MenuChange",true);
                            startActivity(intent);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage("Delete custom filter " +viewItem+"?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

        }
        else{
            UtilsShared.toastIt(this,"You can't delete a built in filter - you can only hide it.",Toast.LENGTH_SHORT);
        }
        //We handled the long press, so onClick doesn't run
        return true;
    }
}

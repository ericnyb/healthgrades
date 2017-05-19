package com.ericbandiero.ratsandmice;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ericbandiero.librarymain.UtilsShared;
import com.ericbandiero.ratsandmice.activities.HelpActivity;
import com.ericbandiero.ratsandmice.activities.InspectionActivity;
import com.ericbandiero.ratsandmice.activities.MainActivity;
import com.ericbandiero.ratsandmice.interfaces.IListenToItemSelected;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import healthdeptdata.FiltersByLocation;
import healthdeptdata.HealthDataFilter;
import healthdeptdata.HealthDataRestaurants;
import healthdeptdata.InspectionSorts;
import healthdeptdata.Inspections;
import com.ericbandiero.myframework.Utility;


/**
 * Created by ${"Eric Bandiero"} on 3/2/2016.
 */
public class AppUtility {


    public static boolean isMapProviderInstalled;

    //TODO Move as many of these as we can to our aar UtilityShared class

    private static List<Inspections> headerList = new ArrayList<>(1);
    private static String className=AppUtility.class.getSimpleName();
    private static Set<String> lastKnownZipCode;


    public static void inspectionBuildAddress(Inspections inspect, TextView textview, boolean useAbbreviatedBoro) {

        //Ignore this for now
        useAbbreviatedBoro = false;

        textview.setText(inspect.getBuilding() + " " + inspect.getStreet()
                + (inspect.getBoro() == null ? "" : "," + (useAbbreviatedBoro ? AppUtility.getAbbreviatedBoro(inspect.getBoro()) : inspect.getBoro()))
                + (inspect.getZipcode() == null ? "" : "," + inspect.getZipcode()));
    }


    public static List<Reports> loadReports() {



        List<Reports> list_reports = new ArrayList<>();
        list_reports.add(new Reports("Latest Inspection Scores", "Latest", 1, false));
        list_reports.add(new Reports("New Restaurants", "New", 2, false));
        list_reports.add(new Reports("Worst Scores This Year", "Worst_This_Year", 3, false));
        list_reports.add(new Reports("Closed By DOHMH Within Last Year", "Closed_This_Year", 4, false));
        list_reports.add(new Reports("Closed by DOHMH On Last Inspection", "Closed_Last_Inspection", 5, false));
        list_reports.add(new Reports("No Grade Sign Posted This Year", "NO SIGN POSTED", 6, false));
        list_reports.add(new Reports("Grade A - Never Received Less Than an A", "GRADE A", 7, false));
        list_reports.add(new Reports("Never an A", "Score greater than C", 9, false));
        list_reports.add(new Reports("Rats and Mice in Last Year", "RATS", 8, false));

        return list_reports;
    }


    public static void main(String[] args) {
        List<Reports> repHeader = new ArrayList<>();
        HashMap<Reports, List<Reports>> repChildren = new HashMap<>();


        Reports.loadMainExpandData(repHeader, repChildren);

        for (Iterator<Reports> iterator = repHeader.iterator(); iterator.hasNext(); ) {
            Reports next = iterator.next();
            if (AppConstant.DEBUG) Log.d(AppUtility.class.getSimpleName()+">","Name:" + next.getReportName());
        }

        if (AppConstant.DEBUG) Log.d(AppUtility.class.getSimpleName()+">","Size:" + repChildren.size());

    }

    public static void setLastFilterInDataProvider(Context context) {
        if (AppConstant.DEBUG) Log.i(className + ">", context.toString());

        List<HealthDataFilter> allFilters = new HealthDataRestaurants().getAllFilters();

        String lastSavedFilterName = PreferenceUtility.getDefaultSharedPreferenceStrings(AppConstant.LAST_FILTER_NAME, "NONE");

        HealthDataFilter healthDataFilter = null;

        //A filter may have been deleted - so we need the else as a backup.
        for (HealthDataFilter allFilter : allFilters) {
            if (allFilter.getFilterName().equals(lastSavedFilterName)) {
                healthDataFilter = allFilter;
            }
        }
        if (healthDataFilter != null) {
            DataProvider.setHealthDataFilter(healthDataFilter);
        }
        else{
            if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Last filter was not found");

            DataProvider.setHealthDataFilter(HealthDataFilter.NONE);
        }
    }

    public static void setUpFilterGeographySpinner(final AppCompatActivity appCompatActivity, Spinner spinner) {
    //TODO Need to set filter clause for custom zip filter
        List<FiltersByLocation> userFilters=new ArrayList<>();
        List<HealthDataFilter> allFilters=new ArrayList<>();

        //This is adding to allfilters via constructor
        addUserFilters();

        allFilters.addAll(new HealthDataRestaurants().getAllFilters());

        //Hide the filters that user doesn't want to see
        hideFilters(allFilters);

        //System.out.println("Users filter:"+userFilters.toString());
        final ArrayAdapter<String> adapter = new ArrayAdapter(appCompatActivity, android.R.layout.simple_spinner_item, allFilters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinnerFilters = spinner;
        spinnerFilters.setAdapter(adapter);

        if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Activity name:"+appCompatActivity.getClass().getName());

        spinnerFilters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                     @Override
                                                     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                                         //We only do this is MainActivity - if in search the load will happen on MainActivity return resume
                                                         if(appCompatActivity instanceof MainActivity){
                                                             if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","action is mainactivity");
                                                             ((MainActivity)appCompatActivity).loadReportsData();
                                                             // ((MainActivity)appCompatActivity).removeMenuItem();
                                                         }

                                                         onItemSelectedAction(parent, position, appCompatActivity);
                                                         if (appCompatActivity instanceof IListenToItemSelected) {
                                                             ((IListenToItemSelected) appCompatActivity).itemSelectedListenCustom(parent);
                                                         }
                                                     }

                                                     @Override
                                                     public void onNothingSelected(AdapterView<?> parent) {

                                                     }
                                                 }
        );

        //spinnerFilters.getAdapter().get
        spinnerFilters.setSelection(getLastFilterInDataProvider(appCompatActivity));
        AppUtility.setLastFilterInDataProvider(appCompatActivity.getApplicationContext());
    }

    private static void hideFilters(List<HealthDataFilter> allFilters) {
        //Get the set of filters that the user didn't want to see anymore
        Set<String> userHideFilterChoices = PreferenceUtility.getUserHideFilterChoices();
        if (userHideFilterChoices==null){
            if (AppConstant.DEBUG) Log.i("AppUtility:"+">","Was no saved hidden filter");
            return;
        }
        Iterator<HealthDataFilter> iterator = allFilters.iterator();
        while (iterator.hasNext()) {
            HealthDataFilter next =  iterator.next();
           // if (next.getFilterName().equals("QUEENS")){
            if (userHideFilterChoices.contains(next.getFilterName())){
                iterator.remove();
            }
        }
    }

    private static void addUserFilters() {
        //We will make the name upper case - stripping of the prefix we use to identify user created filter.
        //NOTE: When we add a new filter it gets added to Healthfilter allfilters list.
        List<FiltersByLocation> filtersByLocations=new ArrayList<>();
        List<String> listOfFilterNamesAlreadyAddedUnModified=HealthDataFilter.returnAllFilterNames();
        List<String> listOfFilterNamesAlreadyAdded=new ArrayList<>(listOfFilterNamesAlreadyAddedUnModified);

        Set <String> set1=new HashSet<>();
        set1.add("");

        String whereForZipFilter1 = createWhereForZipFilter((HashSet<String>) set1);

        if (!listOfFilterNamesAlreadyAdded.contains(AppConstant.CURRENT_ZIP_CODE_FILTER_NAME)){
            filtersByLocations.add(new FiltersByLocation(AppConstant.CURRENT_ZIP_CODE_FILTER_NAME ,whereForZipFilter1));
            listOfFilterNamesAlreadyAdded.add(AppConstant.CURRENT_ZIP_CODE_FILTER_NAME);
        }

        Map<String, ?> userZipCodeFilters = PreferenceUtility.getUserZipCodeFilters();

        for (Map.Entry<String, ?> entry : userZipCodeFilters.entrySet()) {
            //Upper case
            String filterName=entry.getKey().substring(AppConstant.USER_PREF_ZIP_FILTER_PREFIX.length()).toUpperCase();
            String whereForZipFilter = createWhereForZipFilter((HashSet<String>) entry.getValue());
            if (!listOfFilterNamesAlreadyAdded.contains(filterName)){
                filtersByLocations.add(new FiltersByLocation(filterName,whereForZipFilter));
            }
        }
    }

    public static String createWhereForZipFilter(Set<String> zips ){
        // and (zipcode ='10023' or zipcode='10024' or zipcode='10025')
        //10022, 10021

        if (zips==null ||zips.isEmpty()){
            //assert zips!=null:"Should not have a null!";
            if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","There was no last know zip code");
            return "and zipcode=''";
        }
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(" and (");
        for (String zip : zips) {
            stringBuilder.append("zipcode=");
            stringBuilder.append(Utility.quoteStringsForWhere(zip));
            stringBuilder.append(" or ");
        }
        stringBuilder.setLength(stringBuilder.length()-3);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public static String createWhereForZipFilter(String oneZipCode ){
        Set<String> set=new HashSet();
        set.add(oneZipCode);
        return createWhereForZipFilter((HashSet)set);
    }

    private static void onItemSelectedAction(AdapterView<?> parent, int position, AppCompatActivity actionBarActivity) {
        //adapter.notifyDataSetChanged();
        //This only changes the color of the selected item. Keeps dropdowns the same.
        if (parent.getChildAt(0) != null) {
            ((TextView) parent.getChildAt(0)).setTextColor(actionBarActivity.getResources().getColor(R.color.app_color_spinner_filter_selected_item));
            ((TextView) parent.getChildAt(0)).setTextSize(14);
        }
        DataProvider.setHealthDataFilter((HealthDataFilter) parent.getItemAtPosition(position));
        PreferenceUtility.saveLastFilterName(actionBarActivity);
    }


    private static int getLastFilterInDataProvider(AppCompatActivity actionBarActivity) {
        //getAllFilters returns an unmodifiable collection - we put it in new list.


        List<HealthDataFilter> allFilters = new ArrayList<>(new HealthDataRestaurants().getAllFilters());

        hideFilters(allFilters);

        //We need to make sure it wasn't one that they have decided they want hidden
        Set<String> userHideFilterChoices = PreferenceUtility.getUserHideFilterChoices();

        String lastSavedFilterName = PreferenceUtility.getDefaultSharedPreferenceStrings(AppConstant.LAST_FILTER_NAME, "n/a");

        //No filter was ever saved - we need to set default - we will use Manhattan
        if (lastSavedFilterName.equals("n/a")){
            for (int i = 0; i <allFilters.size() ; i++) {
                if (allFilters.get(i).getFilterName().equals("MANHATTAN")){
                    return i;
                }
            }
        }

        if (AppConstant.DEBUG) Log.i(className+">","last filter:"+lastSavedFilterName);

        //Right now this return value isn't being used.
        int defaultIfWeCantUseLastFilter=0;

        //Last filter saved is subsequently hidden by user
        if (userHideFilterChoices!=null && userHideFilterChoices.contains(lastSavedFilterName)){
            return defaultIfWeCantUseLastFilter;
        }

        if (AppConstant.DEBUG)
            Log.i(className + ">", "Previous filter:" + lastSavedFilterName);

        HealthDataFilter healthDataFilter = null;

        int position =defaultIfWeCantUseLastFilter-1;
        for (HealthDataFilter allFilter : allFilters) {
            position++;
            if (allFilter.getFilterName().equals(lastSavedFilterName)) {
                return position;
            }
        }
        //Probably should never get here.
        return defaultIfWeCantUseLastFilter;
    }


    public static String getAbbreviatedBoro(String boro){

        switch (boro.toUpperCase()){
            case "MANHATTAN":
                return "MN";
            case "QUEENS":
                return "QN";
            case "STATEN ISLAND":
                return "SI";
            case "BRONX":
                return "BX";
            case "BROOKLYN":
                return "BK";
            default:
                return  "N/A";
        }
    }

    public static void getCamis(Context context,View view,ListView listViewData){
        //One way of getting data

        String dba=((Inspections)listViewData.getItemAtPosition(listViewData.getPositionForView(view))).getDba();
        String camis=((Inspections)listViewData.getItemAtPosition(listViewData.getPositionForView(view))).getCamis();

        if (AppConstant.DEBUG) Log.i(className+">","Camis:"+camis);

        //Another way of getting data
        //TextView c = (TextView) view.findViewById(R.id.txt_dba);


        //We want to pass in the previous report - will be used in analytics
        Intent intent = new Intent(context,InspectionActivity.class);
        intent.putExtra("CAMIS",camis);
        intent.putExtra("DBA",dba);

        //We want to see if last report was favorites

        if(Reports.getLastReportRun().getReportName().equals(Reports.FAVORITE_RESTAURANTS)){
            if (AppConstant.DEBUG) Log.i(className+">","last report was favorite");
            intent.putExtra("FAVORITES",true);
        }


        context.startActivity(intent);

        //Need to pass this in as bundle - just a cipher - we
        Reports r=new Reports();
        r.setReportName(Reports.GET_BY_CAMIS);
        Bundle bundle=new Bundle();
        bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA,r);

        Intent serviceToRun;
        serviceToRun =new Intent(context, DataProvider.class);
        serviceToRun.putExtra("DataToGet","CAMIS");
        serviceToRun.putExtra("CAMIS",camis);
        //Maybe get rid of this and just use the data to get
        serviceToRun.putExtras(bundle);

        context.startService(serviceToRun);
    }

    public static TextView getTextDialogTitle(Context context,String titleText){
        TextView title = new TextView(context);
        title.setText(titleText);
        // title.setId(Integer.valueOf(100));
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER); // this is required to bring it to center.
        title.setTextSize(22);
        title.setBackgroundColor(context.getResources().getColor(R.color.Aqua));
        title.setTextColor(context.getResources().getColor(R.color.Black));
        return title;
    }

    public static boolean checkNetwork(){
        return UtilsShared.checkConnection(AppConstant.getApplicationContextMain());
    }

    public static void setSavedSortOrder(){
        String lastSort=PreferenceUtility.getDefaultSharedPreferenceStrings(AppConstant.LAST_SORT_ORDER, "None");
        switch(lastSort){
            case "Name - A-Z":
                //Collections.sort(dataList, InspectionSorts.SORT_BY_NAME);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_NAME);
                break;
            case "Name - Z-A":
                //Collections.sort(dataList,Collections.reverseOrder(InspectionSorts.SORT_BY_NAME));
                DataProvider.setSorterToUse(Collections.reverseOrder(InspectionSorts.SORT_BY_NAME));
                break;
            case "Score - Best to Worst":
                //Collections.sort(dataList,InspectionSorts.SORT_BY_SCORE);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_SCORE);
                break;
            case "Score - Worst to Best":
                //Collections.sort(dataList,Collections.reverseOrder(InspectionSorts.SORT_BY_SCORE));
                DataProvider.setSorterToUse(Collections.reverseOrder(InspectionSorts.SORT_BY_SCORE));
                break;
            case "Inspection Date - Most recent":
                //Collections.sort(dataList,InspectionSorts.SORT_BY_INSPECTION_DATE_RECENT_FIRST);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_INSPECTION_DATE_RECENT_FIRST);
                break;

            case "Inspection Date - Oldest":
                //Collections.sort(dataList,InspectionSorts.SORT_BY_INSPECTION_DATE_OLDEST_FIRST);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_INSPECTION_DATE_OLDEST_FIRST);
                break;

            case "Grade":
                //Collections.sort(dataList,InspectionSorts.SORT_BY_GRADE);
                DataProvider.setSorterToUse(InspectionSorts.SORT_BY_GRADE);
                break;
            case "None":
                DataProvider.setSorterToUse(null);
                break;
        }

    }


    public static void helpActivityStart(Context context,MenuItem item){
        Intent intent = new Intent(context, HelpActivity.class);

        //String [] titleAndHeader=item.getTitle().toString().split("~",2);


        //if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">",titleAndHeader[0]);
        //if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">",titleAndHeader[1]);
        //String menu_resource_name=context.getIntent().getStringExtra("menu_resource_name");







        intent.putExtra("menu",item.getItemId());
        intent.putExtra("title",item.getTitle());
        intent.putExtra("heading","");
        intent.putExtra("menu_resource_name",context.getResources().getResourceEntryName(item.getItemId()));






      /*  String [] resourceEntryName = context.getResources().getResourceEntryName(menuId).split("\\/");

        for (int i = 0; i < resourceEntryName.length; i++) {
            String s = resourceEntryName[i];
            if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Menu:"+s);
        }

*/
        context.startActivity(intent);
    }

    public static void mapRestaurant(Context context,String locationName){
        //Temp
        // Create a Uri from an intent string. Use the result to create an Intent.
        //40.782747,-73.9862107
        //Uri gmmIntentUri = Uri.parse("geo:40.7826324,-73.9858272 ?q=" + Uri.encode("328 w 76th street, New York"));
        //Uri gmmIntentUri = Uri.parse("geo:?q=" + Uri.encode("328 w 76th street, New York"));
         Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(locationName));
        //Uri gmmIntentUri = Uri.parse("geo:0,0?q="+"40.782747,-73.984022(Eric)");
      //  Uri gmmIntentUri = Uri.parse("geo:0,0?q=-33.8666,151.1957(Google+Sydney)");

// Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
// Make the Intent explicit by setting the Google Maps package
//        Intent intent = mapIntent.setPackage("com.google.android.apps.maps");
//
//        if (intent==null){
//            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">",);
//        }
// Attempt to start an activity that can handle the Intent
        // if (mapIntent.resolveActivity(getPackageManager()) != null) {

        PackageManager packageManager = context.getPackageManager();
        List activities = packageManager.queryIntentActivities(mapIntent,
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;
        if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Activities that can handle this:"+activities.get(0).toString());
        if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Activities that can handle this:"+activities.size());
        if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Can we handle this intent:"+isIntentSafe);


        context.startActivity(mapIntent);
        // }
    }

    public static String buildLocationNameFromInspection(Inspections inspections){
        return inspections.getBuilding()+" "+ inspections.getStreet()+" New York "+inspections.getZipcode();
    }





    public static Set getLastKnownZipCode() {
        return lastKnownZipCode;
    }

    public static void setLastKnownZipCode(Set<String> setLastKnownZipCodes) {
        if (setLastKnownZipCodes.size()>1) {
            UtilsShared.toastIt(AppConstant.getApplicationContextMain(),"Multiple zips:" + setLastKnownZipCodes.toString(), Toast.LENGTH_LONG);
        }
       lastKnownZipCode = setLastKnownZipCodes;
    }

    /**
     * We will always return a list - it can have 0 items in it. It never will be null
     * @param context Context
     * @param latitude latitude
     * @param longitude longitude
     * @param addressesToReturn Number of address object to return
     * @return A list of Address objects - can be 0 size, but never null
     */

    public static List<Address> getAddressesFromGeoCoder(Context context,double latitude,double longitude,int addressesToReturn) {
        List<Address> listOfAddresses = null;
        Geocoder gc=new Geocoder(context);
        try {
            listOfAddresses = gc.getFromLocation(latitude, longitude, addressesToReturn);

            //This is a test of more than one zip code - will give us 10023 & 10024
            //listOfAddresses = gc.getFromLocation(40.782891, -73.983085, addressesToReturn);
        } catch (IOException e) {
             Log.e(new Object() { }.getClass().getEnclosingClass()+">",e.getMessage());
        }
        catch (IllegalArgumentException e) {
             Log.e(new Object() { }.getClass().getEnclosingClass()+">",e.getMessage());
        }
        //Could be empty or null - we don't want to return null
        if (listOfAddresses==null){
            listOfAddresses=new ArrayList<>();
        }
        return listOfAddresses;
    }


    public static void getZipCodesFromAddressList(List<Address> listOfAddresses, Set<String> zips) {
        for (Address address : listOfAddresses) {
            if (address.getPostalCode() != null) {
                zips.add(address.getPostalCode());
            }
        }
    }


}

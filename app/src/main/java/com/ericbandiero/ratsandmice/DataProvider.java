package com.ericbandiero.ratsandmice;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ericbandiero.ratsandmice.activities.SearchActivity;
import com.ericbandiero.ratsandmice.activities.ViolationsActivity;

import com.sun.jersey.api.client.ClientHandlerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import healthdeptdata.DataWrapper;
import healthdeptdata.HealthDataFilter;
import healthdeptdata.HealthDataRestaurants;
import healthdeptdata.InspectionSorts;
import healthdeptdata.Inspections;
import healthdeptdata.MapDataHolder;
import healthdeptdata.MatchType;
import com.ericbandiero.myframework.Utility;


/**
 * We will use this class to gather data from our health inspection dataset.
 * Created by ${"Eric Bandiero"} on 11/16/2015.
 */
public class DataProvider extends IntentService {
    //TODO In cuisines break out the multiple types Cuban/Latin/Chicken/
    private HealthDataRestaurants healthDataRestaurants = new HealthDataRestaurants();
    private List<Inspections> listInspections = new ArrayList<>();
    private String dataToGetFromExtra = "";

    //We initialize here because we want these to start fresh.
    //Data activities check size to see if they need to re-draw data to screen.
    private static List<Inspections> listDataSet = new ArrayList<>();
    private static List<Inspections> listDetailDataSet = new ArrayList<>();
    private static List<Inspections> listSearchDataSet = new ArrayList<>();
    private static List<Inspections> listViolationsDataSet = new ArrayList<>();

    //Used for some meta data reports
    private static Map<String, Integer> stringIntegerMap=new HashMap<>();

    private static MapDataHolder mapDataHolder;

    //Used to get data for last inspection
    private static List<Inspections> listMainDataSet = new ArrayList<>();


    public static final String REQUEST_STRING = "myRequest";
    public static final String RESPONSE_STRING = "myResponse";
    public static final String RESPONSE_MESSAGE = "myResponseMessage";

    public static final String SEARCH_STRING_PASSED_IN = "searchTextPassedIn";


    public static final String STRICT_SEARCH_MUST_USE_FILTER = "strict_search";

    public static String REPORT_DESCRIPTION;

    private static Comparator sorterToUse;

    private boolean weHadAnError=false;

    public static String errorMessage;

    //This will get set when user changes filter or at startup in MainActivity.
    public static HealthDataFilter healthDataFilter;



    ProgressDialog progressDialog;
    Context context;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DataProvider() {
        super("data");
    }

    public static List<Inspections> getListMainDataSet() {
        return listMainDataSet;
    }

    public static void setListMainDataSet(List<Inspections> listMainDataSet) {
        DataProvider.listMainDataSet = listMainDataSet;
    }

    public static Comparator getSorterToUse() {
        return sorterToUse;
    }

    public static void setSorterToUse(Comparator sorterToUse) {
        DataProvider.sorterToUse = sorterToUse;
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        //We want to handle a case when there is no network connection.
        if (!AppUtility.checkNetwork()) {
            return;
        }


        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Clearing previous data");
        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Health data filter:" + healthDataFilter.getFilterString());
        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Current zip code:" + AppUtility.getLastKnownZipCode());

        //We do this on the fly:
        if (healthDataFilter.getFilterName().contains(AppConstant.CURRENT_ZIP_CODE_FILTER_NAME)) {
            healthDataFilter.setFilterString(AppUtility.createWhereForZipFilter(AppUtility.getLastKnownZipCode()));
        }

        getListDataSet().clear();
        getListDetailDataSet().clear();

        if (listInspections!=null){
            listInspections.clear();
        }

        errorMessage="";
        weHadAnError=false;

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Getting data from data provider");



        //This is the actual camis number
        String camisToGetFromExtra = intent.getStringExtra("CAMIS");

        //When called from MainActivity this holds a real report
        //When called from others the pass in report to run
        Reports repFromParcel = (Reports) intent.getParcelableExtra(Reports.PARCEBLABLE_TEXT_FOR_EXTRA);

        String dataSetWanted = repFromParcel.getReportName();

        String searchText = intent.getStringExtra(SEARCH_STRING_PASSED_IN);

        boolean strictSearch = intent.getBooleanExtra(STRICT_SEARCH_MUST_USE_FILTER, false);



        REPORT_DESCRIPTION = repFromParcel.getReportDesc();

        //healthDataRestaurants.fetchMetaData_All_Food_Types();

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "From parcel item:" + repFromParcel.toString());

        //We want to catch errors
        try {
            switch (dataSetWanted) {

                case Reports.LATEST_INSPECTION_SCORES:
                    //We will get just a week back
                    listInspections = healthDataRestaurants.fetchQueryDataLastInspection(healthDataFilter, 7);
                    break;
                case Reports.LATEST_INSPECTION_SCORES_MONTH:
                    //We will get just a week back
                    listInspections = healthDataRestaurants.fetchQueryDataLastInspection(healthDataFilter, 30);
                    break;
                case Reports.ALL_PLACES_FOR_ONE_ZIP_CODE:
                    //We will get just a week back
                    listInspections = healthDataRestaurants.fetchQueryDataLastInspection(healthDataFilter, 365 * 2);
                    break;
                case Reports.NEW_RESTAURANTS:
                    listInspections = healthDataRestaurants.fetchQueryDataNewPlaces(healthDataFilter);
                    break;
                case Reports.WORST_150_SCORES_IN_PAST_YEAR:
                    List<Inspections> temp1 = healthDataRestaurants.fetchQueryDataWorstScoreThisYear(healthDataFilter);
                    listInspections = new ArrayList<Inspections>(temp1.subList(0, temp1.size() > 149 ? 150 : temp1.size()));
                    break;
                case Reports.WORST_25_SCORES_EVER:
                    List<Inspections> temp = healthDataRestaurants.fetchQueryDataWorstScoreEver(healthDataFilter);
                    listInspections = new ArrayList<Inspections>(temp.subList(0, temp.size() > 24 ? 25 : temp.size()));
                    break;
                case Reports.CLOSED_BY_DOHMH_WITHIN_LAST_YEAR:
                    listInspections = healthDataRestaurants.fetchQueryDataClosedInPastYear(healthDataFilter);
                    break;
                case Reports.CLOSED_BY_DOHMH_ON_LAST_INSPECTION:
                    listInspections = healthDataRestaurants.fetchDataClosedLastInspection(healthDataFilter);
                    break;
                case Reports.NO_GRADE_SIGN_POSTED_IN_PAST_YEAR:
                    listInspections = healthDataRestaurants.fetchQueryDataNoSignPosted(healthDataFilter);
                    break;
                case Reports.GRADE_A_NEVER_RECEIVED_LESS_THAN_AN_A:
                    listInspections = healthDataRestaurants.fetchQueryDataGradeA(healthDataFilter);
                    break;
                case Reports.C_YOU_LATER_NEVER_AN_A_OR_B:
                    listInspections = healthDataRestaurants.fetchQueryData_Never_Got_Less_Than_C(healthDataFilter, false);
                    break;
                case Reports.NEVER_GOT_AN_A:
                    listInspections = healthDataRestaurants.fetchQueryData_Never_Got_Grade_A(healthDataFilter, false);
                    break;
                case Reports.RATS_OR_MICE_IN_PAST_YEAR:
                    listInspections = healthDataRestaurants.fetchQueryDataRatsAndMiceInLastYear(healthDataFilter);
                    break;
                case Reports.GET_BY_CAMIS:
                    if (AppConstant.DEBUG)
                        Log.i(this.getClass().getSimpleName() + ">", "Getting camis data...");
                    listInspections = healthDataRestaurants.fetchQueryDataGetAllDataByCamis(camisToGetFromExtra);
                    break;
                case Reports.OLDEST_INSPECTIONS:
                    listInspections = healthDataRestaurants.fetchQueryDataOldestInspections();
                    break;
                case Reports.ROACHES:
                    //We can set the report to ever by setting this to true.
                    //healthDataRestaurants.setIgnoreYearsBack(true);
                    listInspections = healthDataRestaurants.fetchQueryDataRoachesInLastYear(healthDataFilter);
                    break;
                case Reports.HAND_ISSUES:
                    //We can set the report to ever by setting this to true.
                    //healthDataRestaurants.setIgnoreYearsBack(true);
                    listInspections = healthDataRestaurants.fetchQueryDataHandIssues(healthDataFilter);
                    break;
                case Reports.CALORIE_SIGN_ISSUES:
                    //We can set the report to ever by setting this to true.
                    //healthDataRestaurants.setIgnoreYearsBack(true);
                    listInspections = healthDataRestaurants.fetchQueryDataCalorieInfoMissing(healthDataFilter);
                    break;
                case Reports.SMOKE_SIGN_ISSUES:
                    //We can set the report to ever by setting this to true.
                    //healthDataRestaurants.setIgnoreYearsBack(true);
                    listInspections = healthDataRestaurants.fetchQueryDataSmokingInfoMissing(healthDataFilter);
                    break;
                case Reports.ALLERGY_SIGN_ISSUES:
                    //We can set the report to ever by setting this to true.
                    //healthDataRestaurants.setIgnoreYearsBack(true);
                    listInspections = healthDataRestaurants.fetchQueryDataAllergySignIssues(healthDataFilter);
                    break;


                case Reports.DBA_SEARCH:
                    if (AppConstant.DEBUG)
                        Log.i(this.getClass().getSimpleName() + ">", "Searching for dba...");
                    if (strictSearch) {
                        healthDataRestaurants.addHealthDataFilter(healthDataFilter);
                    }
                    listInspections = healthDataRestaurants.fetchQueryDataByDBA(searchText, SearchActivity.matchtype);
                   /*
                    for (Inspections listInspection : listInspections) {
                        if (AppConstant.DEBUG)
                            Log.d(this.getClass().getSimpleName() + ">", "listInspection.getInspection_date() = " + listInspection.getInspection_date());
                        if (AppConstant.DEBUG)
                            Log.d(this.getClass().getSimpleName() + ">", "Score) = " + listInspection.getScore());
                    }
                   */
                    break;
                case Reports.VIOLATIONS_USER_CREATE:
                    if (getListViolationsDataSet() == null || getListViolationsDataSet().size() == 0) {
                        if (AppConstant.DEBUG)
                            Log.i(this.getClass().getSimpleName() + ">", "Getting all violations...");
                        listInspections = healthDataRestaurants.fetchMetaDataViolations();
                    } else {
                        listInspections.addAll(getListViolationsDataSet());
                    }
                    break;

                case Reports.VIOLATIONS_USER_FLAG_SET:
                    if (getListViolationsDataSet() == null || getListViolationsDataSet().size() == 0) {
                        if (AppConstant.DEBUG)
                            Log.i(this.getClass().getSimpleName() + ">", "Getting all violations for flag set");
                        listInspections = healthDataRestaurants.fetchMetaDataViolations();
                        if (AppConstant.DEBUG)
                            Log.d(this.getClass().getSimpleName() + ">", "Violations found:" + listInspections.size());
                    } else {
                        listInspections.addAll(getListViolationsDataSet());
                    }
                    break;

                case Reports.USER_SELECTED_VIOLATIONS_SEARCH:
                    if (AppConstant.DEBUG)
                        Log.d(this.getClass().getSimpleName() + ">", "Length:" + ViolationsActivity.aViolationCodes.length);
                    listInspections = healthDataRestaurants.fetchQueryDataByViolationCode(ViolationsActivity.aViolationCodes, healthDataFilter);
                    break;
                case Reports.FIELD_SEARCH:

                    String field = intent.getStringExtra("field");
                    String query = intent.getStringExtra("search_query");
                    if (AppConstant.DEBUG)
                        Log.d(this.getClass().getSimpleName() + ">", "Search query:" + query);
                    MatchType match;
                    //For now we will always use contains...makes more sense than starts with for food types.
                    if (field.toLowerCase().equals("cuisine_description")) {
                        match = MatchType.MATCH_NAME_CONTAINS;
                        if (AppConstant.DEBUG)
                            Log.i(this.getClass().getSimpleName() + ">", "Cuisine type:" + intent.getStringExtra("cuisine"));
                    } else {
                        match = SearchActivity.matchtype!=null?SearchActivity.matchtype:MatchType.MATCH_NAME_EXACT;
                    }

                    listInspections = healthDataRestaurants.fetchQueryDataSearchByField(field, query,
                            strictSearch ? healthDataFilter : HealthDataFilter.NONE, match);

                    if (AppConstant.DEBUG)
                        Log.d(this.getClass().getSimpleName() + ">", "Field search records returned:" + listInspections.size());
                    break;

                case Reports.LAST_INSPECTION_DATE:
                    //Inspections inspections=new Inspections();
                    listInspections = healthDataRestaurants.fetchMetaDataLastInspectionDate();
                    //inspections.setDba(lastInspectionDate);
                    //if (AppConfig.DEBUG) Log.i(this.getClass().getSimpleName()+">","Last inspection date:"+lastInspectionDate);
                    ////listInspections.add(inspections);
                    break;
                case Reports.A_GRADE_LAST_INSPECTION:

                    listInspections = healthDataRestaurants.fetchDataLastScoreWasGradeBC("A", healthDataFilter);
                    break;

                case Reports.C_GRADE_LAST_INSPECTION:

                    listInspections = healthDataRestaurants.fetchDataLastScoreWasGradeBC("C", healthDataFilter);
                    break;

                case Reports.B_GRADE_LAST_INSPECTION:
                    listInspections = healthDataRestaurants.fetchDataLastScoreWasGradeBC("B", healthDataFilter);
                    break;

                case Reports.FAVORITE_RESTAURANTS:
                    Map<String, ?> allPreferenecesStrings = PreferenceUtility.getAllPreferenecesStrings(PreferenceUtility.FAVORITE_RESTAURANT);

                    String[] strings = allPreferenecesStrings.keySet().toArray(new String[0]);

                    String camisArray = Utility.buildCommaSeparatedStringInParenthesis(strings);

                    if (AppConstant.DEBUG)
                        Log.i(this.getClass().getSimpleName() + ">", "Camis:" + camisArray);

                    List<Inspections> inspections1 = healthDataRestaurants.fetchQueryDataUserWhereClause(" and camis in" + camisArray, HealthDataFilter.NONE);
                    listInspections = healthDataRestaurants.getLatestInspectionRecordForRestaurant(inspections1);
                    break;

                case Reports.CLOSED_MOST_TIMES_PAST_YEAR:
                    listInspections = healthDataRestaurants.fetchDataClosedMostTimesInPastYear(healthDataFilter);
                    break;

                case Reports.CLOSED_MOST_TIMES_EVER:
                    listInspections = healthDataRestaurants.fetchDataClosedMostTimesEver(healthDataFilter);
                    break;

                case Reports.META_CUISINES_BY_BORO:
                    // listInspections = healthDataRestaurants.fetchMetaDataCuisneCountOnePass();
                    mapDataHolder = healthDataRestaurants.fetchMetaDataCuisineCountOnePass(HealthDataFilter.NONE);
                    break;

                case Reports.META_CUISINES_BY_FILTER:
                    // listInspections = healthDataRestaurants.fetchMetaDataCuisneCountOnePass();
                    mapDataHolder = healthDataRestaurants.fetchMetaDataCuisineCountOnePass(healthDataFilter);
                    break;

                case Reports.META_CHAINS:
                    stringIntegerMap.clear();
                    stringIntegerMap = healthDataRestaurants.fetchMetaData_Most_Chains();
                    break;

                case Reports.META_RESTAURANTS_BY_BORO:
                    stringIntegerMap.clear();
                    stringIntegerMap = healthDataRestaurants.fetchMetaDataCamisByBoroCount();
                    break;

                case Reports.META_RESTAURANTS_BY_FILTER:
                    stringIntegerMap.clear();
                    stringIntegerMap = healthDataRestaurants.fetchMetaDataCamisByFilter(healthDataFilter);
                    break;
                case Reports.META_RESTAURANTS_BY_ZIP_CODE:
                    stringIntegerMap.clear();
                    List<DataWrapper> listdatawrapper = healthDataRestaurants.fetchMetaDataCAmisByZipCode(healthDataFilter);
                    Collections.sort(listdatawrapper,DataWrapper.SORT_BY_FIELD);
                    for (java.util.Iterator iterator = listdatawrapper.iterator(); iterator.hasNext(); ) {
                        DataWrapper next = (DataWrapper) iterator.next();
                        stringIntegerMap.put(next.getField(),Integer.parseInt(next.getValue()));
                    }

                    break;

                //TODO Move these up to case?
                default:

                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Report id:"+repFromParcel.getReportId());

                    //We check to see if this is a user saved report - code 99
                    if (repFromParcel.getReportId() == Reports.REPORTS_USER_REPORT_ID) {
                        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Will run user report!");

                        //listInspections = healthDataRestaurants.fetchQueryDataByViolationCode(repFromParcel.getCodeArray(), healthDataFilter);

                        String[] aDataForReport = repFromParcel.getCodeArray();
                        boolean isFieldReport=false;

                        //Sort these - field reports we need correct order - sets are unordered
                        //For user violation it doesn't matter
                        //For user field search we reply on data being in a certain order
                        //Preferences saves a set - we never know what order it will be
                        //So ReportFieldBuilder gives each array member a prefix
                        Arrays.sort(aDataForReport);

                        //See if this is a field_search
                        String fieldReport=ReportFieldBuilder.removePrefix(aDataForReport[0]);

                        isFieldReport=fieldReport.equals("field_report");

                        if (isFieldReport){
                            String valueToGet=ReportFieldBuilder.removePrefix(aDataForReport[1]);
                            String fieldToSearch=ReportFieldBuilder.removePrefix(aDataForReport[2]);
                            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","field_report:"+fieldReport);
                            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","value:"+valueToGet);
                            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","field to search:"+fieldToSearch);
                            listInspections = healthDataRestaurants.fetchQueryDataSearchByField(fieldToSearch,valueToGet,healthDataFilter,MatchType.MATCH_NAME_EXACT);
                        }
                        else{
                            //User violation saved report
                            listInspections = healthDataRestaurants.fetchQueryDataByViolationCode(repFromParcel.getCodeArray(), healthDataFilter);
                        }
                    }

                    if (repFromParcel.getReportId() == Reports.REPORTS_VIOLATION_CODES_QUERY) {
                        //Right now this is just one code in the array
                        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Going  to run violation report...");
                        listInspections = healthDataRestaurants.fetchQueryDataByViolationCode(repFromParcel.getCodeArray(), healthDataFilter);


                    } else{
                    //This was awalys being called...except for report violation codes query
//                        if (AppConstant.DEBUG)
//                            Log.i(this.getClass().getSimpleName() + ">", "None was chosen");

                    }
            }
        }
        //This would be caught within app
        catch(OutOfMemoryError e){
            Log.e(this.getClass().getSimpleName()+">","Out of memory:"+e.getMessage());
            weHadAnError=true;
            errorMessage="Out of Memory";
            //AppUtility.toastIt("Could not run report:"+e.getMessage(), Toast.LENGTH_LONG);
            //Toast.makeText(AppConstant.getApplicationContextMain(),"Hello!",Toast.LENGTH_LONG).show();
            //return;
        }
        //This can be thrown from HealthData jar - we do it from fetchQueryDataNewPlaces
        catch(ClientHandlerException e){
            Log.e(this.getClass().getSimpleName()+">","Client handle:"+e.getMessage());
            weHadAnError=true;
            errorMessage=e.getMessage();
            //AppUtility.toastIt("Could not run report:"+e.getMessage(), Toast.LENGTH_LONG);
            //Toast.makeText(AppConstant.getApplicationContextMain(),"Hello!",Toast.LENGTH_LONG).show();
            //return;
        }
        catch(Exception ex){
            Log.e(this.getClass().getSimpleName()+">","Caught error:"+ex.getMessage());
            weHadAnError=true;
            errorMessage=ex.getMessage();
            ex.printStackTrace();
            //AppUtility.toastIt("Could not run report:"+ex.getMessage(), Toast.LENGTH_LONG);
            //return;
        }
            if (listInspections == null && stringIntegerMap.isEmpty()) {
                return;
            }


            //If the user selected a sort order we honor it here.
            if (getSorterToUse() != null) {
                Collections.sort(listInspections, getSorterToUse());
            } else {
                switch (dataSetWanted) {
                    case Reports.WORST_25_SCORES_EVER:
                    case Reports.WORST_150_SCORES_IN_PAST_YEAR:
                        Collections.sort(listInspections, Collections.reverseOrder(InspectionSorts.SORT_BY_SCORE));
                        break;
                    case Reports.CLOSED_MOST_TIMES_PAST_YEAR:
                        Collections.sort(listInspections, Collections.reverseOrder(InspectionSorts.SORT_BY_SCORE));
                        break;
                    case Reports.CLOSED_MOST_TIMES_EVER:
                        Collections.sort(listInspections, Collections.reverseOrder(InspectionSorts.SORT_BY_SCORE));
                        break;

                    default:
                        Collections.sort(listInspections, InspectionSorts.SORT_BY_NAME);
                }

            }


            //What collections to put these into.

            switch (dataSetWanted) {
                case Reports.DBA_SEARCH:
                    getListSearchDataSet().clear();
                    getListSearchDataSet().addAll(listInspections);
                    break;
                case Reports.FIELD_SEARCH:
                    if (AppConstant.DEBUG)
                        Log.d(this.getClass().getSimpleName() + ">", "Field search adding data to getListSearchDataSet");
                    getListSearchDataSet().clear();
                    getListSearchDataSet().addAll(listInspections);
                    getListDataSet().addAll(listInspections);
                    break;
                case Reports.GET_BY_CAMIS:
                    getListDetailDataSet().addAll(listInspections);
                    break;
                case Reports.VIOLATIONS_USER_CREATE:
                    if (getListViolationsDataSet() == null || getListViolationsDataSet().isEmpty()) {
                        getListViolationsDataSet().addAll(listInspections);
                    }
                    break;

                case Reports.VIOLATIONS_USER_FLAG_SET:
                    if (getListViolationsDataSet() == null || getListViolationsDataSet().isEmpty()) {
                        getListViolationsDataSet().addAll(listInspections);
                    }
                    break;

                case Reports.LAST_INSPECTION_DATE:
                    getListMainDataSet().clear();
                    getListMainDataSet().addAll(listInspections);
                    break;

                case Reports.META_CHAINS:
                    getListMainDataSet().clear();
                    //getListMainDataSet().addAll(listInspections);
                    break;

                case Reports.META_RESTAURANTS_BY_BORO:
                    getListMainDataSet().clear();
                    //getListMainDataSet().addAll(listInspections);
                    break;
                default:
                    getListDataSet().addAll(listInspections);
            }

            //Done with this - clear some memory.
            listInspections.clear();

            //We send an broadcast - if we setAction to something else receiver will ignore it
            //if its filter is not set for that action.
            Intent broadcastIntent = new Intent();


            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "Getting ready to run broadcast!");

            //See if we put a map into extra
            broadcastIntent.putExtra("Map", (java.io.Serializable) stringIntegerMap);
            broadcastIntent.setAction(AppConstant.PROCESS_RESPONSE);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);


            broadcastIntent.putExtra(AppConstant.ERROR_GETTING_DATA, weHadAnError);

            broadcastIntent.putExtra(RESPONSE_MESSAGE, REPORT_DESCRIPTION);
            broadcastIntent.putExtra(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, intent.getParcelableExtra(Reports.PARCEBLABLE_TEXT_FOR_EXTRA));
            sendBroadcast(broadcastIntent);
        }

    private void getdata() {
        healthDataRestaurants.setLimit(-1);
        listInspections = healthDataRestaurants.fetchQueryDataLastInspection(healthDataFilter,7);
    }

    private void massageData() {
        List<Inspections> list_new = new ArrayList<>();

        //We will just get those with Not Yet Graded
        for (Inspections inspection : listInspections) {
            if (inspection.getGrade().contains("Not Yet Graded")) {
                list_new.add(inspection);
            }

        }
        getListDataSet().addAll(list_new);
        //System.out.println("Data set size:"+listDataSet.size());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //listDataSet.clear();
        if (listInspections!=null) {
            listInspections.clear();
        }
    }


    public static Map<String, Integer> getStringIntegerMap() {
        return stringIntegerMap;
    }


    public static List<Inspections> getListDataSet() {
        return listDataSet;
    }

    public static void setListDataSet(List<Inspections> listDataSet) {
        DataProvider.listDataSet = listDataSet;
    }

    public static List<Inspections> getListDetailDataSet() {

        return listDetailDataSet;
    }

    public static void setListDetailDataSet(List<Inspections> listDetailDataSet) {
        DataProvider.listDetailDataSet = listDetailDataSet;
    }

    public static List<Inspections> getListSearchDataSet() {
        return listSearchDataSet;
    }

    public static void setListSearchDataSet(List<Inspections> listSearchDataSet) {
        DataProvider.listSearchDataSet = listSearchDataSet;
    }

    public static List<Inspections> getListViolationsDataSet() {
        return listViolationsDataSet;
    }

    public static void setListViolationsDataSet(List<Inspections> listViolationsDataSet) {
        DataProvider.listViolationsDataSet = listViolationsDataSet;
    }

    public static MapDataHolder getMapDataHolder() {
        return mapDataHolder;
    }


    public static HealthDataFilter getHealthDataFilter() {
        return healthDataFilter;
    }

    public static void setHealthDataFilter(HealthDataFilter healthDataFilter) {
        DataProvider.healthDataFilter = healthDataFilter;
    }
}
